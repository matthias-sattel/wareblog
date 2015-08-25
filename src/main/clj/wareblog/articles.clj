(ns wareblog.articles
  (require [clojure.edn :as edn]
           [taoensso.timbre :as timbre]
           [com.stuartsierra.component :as component]
           [infrastructure.endpoint :as endpoint]
           [liberator.core :as liberator :refer [resource defresource]]
           [selmer.parser :refer [render render-file set-resource-path!]]
           [environ.core :refer [env]]
           [bidi.ring :as bidi-ring :refer [make-handler resources resources-maybe]]
           [wareblog.article-storage :as article-storage]))

;Provide alias for logging with timbre
(timbre/refer-timbre)

(def abbreviations
  {:edn {:label "edn"
         :name "Extensible Data Notation"
         :uri "https://github.com/edn-format/edn"
         }
   :md {:label "md"
        :name "Markdown"
        :uri "http://daringfireball.net/projects/markdown/"
        }
   })

(defn- parse-paragraph [part & more];should accept multiple args
  (str "<p>" part more "</p>"))

(def edn-readers
  {'wareblog/author str
   'wareblog/abbreviation #(str "<span class=\"abbreviation\">" (:name (%1 abbreviations)) "</span>")
   'wareblog/abstract #(reduce str %1)
   'wareblog/content #(reduce str %1)
   'wareblog/paragraph #(str "<p>" (reduce str %1) "</p>")
   ;'inst str
   })

(defn- article-to-html [article]
  {:title (edn/read-string
           {:readers edn-readers}
           (:title article)),
   :abstract (edn/read-string
              {:readers edn-readers}
              (:abstract article)),
   :content (edn/read-string
             {:readers edn-readers}
             (:content article))}
  )

(liberator/defresource article [storage]
  :available-media-types ["application/edn" "text/html"]
  :allowed-methods [:get :options]
  :handle-ok #(let [media-type (get-in % [:representation :media-type])
                    id (get-in % [:request :route-params :id])]
                (do
                  (info (str "Requesting article with id " id " from storage " storage))
                  (condp = media-type
                    "application/edn" (article-storage/get-by-id storage (keyword id))
                    "text/html" ;(let [article-as-html-map (get-article-as-html (keyword id))]
                                  (let [article-as-html-map (article-to-html (article-storage/get-by-id storage (keyword id)))]
                                    (debug "Article as html map" article-as-html-map)
                                    (render-file "templates/article.html" {:dev (env :wareblog-dev), :title (:title article-as-html-map), :article-title (:title article-as-html-map), :article-abstract (:abstract article-as-html-map), :article-content (:content article-as-html-map)})))
                  )))

(liberator/defresource comment-article [storage]
  :available-media-types ["text/html"]
  :allowed-methods [:get :options :post]
  :handle-ok (fn [ctx] (let [id (get-in ctx [:request :route-params :id])]
                         (article-storage/get-by-id storage (keyword id)))))

(liberator/defresource home
  :available-media-types ["text/html"]
  :handle-ok (render-file "templates/home.html" {:name "World"}))


(defrecord Articles [state storage]

  component/Lifecycle

  (start [component]
    (info "Starting the articles component using storage " storage)
    (assoc component :state "started"))

  (stop [component]
    (info "Stopping the articles component")
    (assoc component :storage nil :state "stopped"))

  endpoint/Endpoint

  (get-routes [this]
      ["/" {"" home
             "index.html" home
             "articles/" {[:id] (article storage)
                          [:id "/comment"] comment-article}
             "resources/" (resources-maybe {:prefix "public/"})}])
)

(defn new-articles-component []
  (map->Articles {}))
