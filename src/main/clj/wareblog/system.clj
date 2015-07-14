(ns wareblog.system
  (require [wareblog.articles :refer [get-article get-article-as-html get-article-header]]
           [com.stuartsierra.component :as component]
           [wareblog.http-component :as http-component]
           [wareblog.article-storage-test-component :as article-storage-test-component]
           [wareblog.articles :as articles]
           [liberator.core :as liberator :refer [resource defresource]]
           [ring.middleware.params :refer [wrap-params]]
           [bidi.ring :as bidi-ring :refer [make-handler resources resources-maybe]]
           [org.httpkit.server :as httpkit-server :refer [run-server]]
           [environ.core :refer [env]]
           [taoensso.timbre :as timbre]
           [selmer.parser :refer [render render-file set-resource-path!]]))

;Provide alias for logging with timbre
(timbre/refer-timbre)

(def default-http-port
  3000)

(defn http-port [port]
  (let [ext-http-port (env :wareblog-http-port)]
    (if (not (nil? port))
      port
      (if (nil? ext-http-port) default-http-port (Integer/valueOf ext-http-port)))))

(liberator/defresource article
  :available-media-types ["application/edn" "text/html"]
  :allowed-methods [:get :options]
  :handle-ok #(let [media-type (get-in % [:representation :media-type])
                    id (get-in % [:request :route-params :id])]
                (do
                  (debug (str "Requesting article with id " id))
                  (condp = media-type
                    "application/edn"  (get-article (keyword id))
                    "text/html" (let [article-as-html-map (get-article-as-html (keyword id))]
                                  ;(debug article-as-html-map)))
                                  (render-file "templates/article.html" {:dev (env :wareblog-dev), :title (:header article-as-html-map), :article-title (:header article-as-html-map), :article-abstract (:abstract article-as-html-map), :article-content (:content article-as-html-map)})))
                  )))

(liberator/defresource comment-article
  :available-media-types ["text/html"]
  :allowed-methods [:get :options :post]
  :handle-ok (fn [ctx] (let [id (get-in ctx [:request :route-params :id])]
                         (get-article (keyword id)))))

(liberator/defresource home
  :available-media-types ["text/html"]
  :handle-ok (render-file "templates/home.html" {:name "World"}))

(defn handler []
  (bidi-ring/make-handler ["/" {"" home
                                "index.html" home
                                "articles/" {[:id] article
                                             [:id "/comment"] comment-article}
                                "resources/" (resources-maybe {:prefix "public/"})}]))

(defn wrap-handler []
  (-> (handler)
      wrap-params))

 (defn wareblog-system [config-options]
  (do 
    (info "Building wareblog system")
    (-> (component/system-map
         :article-storage (article-storage-test-component/new-storage)
         :articles (articles/new-articles-component)
         :http (http-component/new-http-server (http-port (:http-port config-options)) (wrap-handler)))
        (component/system-using {:articles {:storage :article-storage}
                                 :http {:storage :article-storage
                                        :articles :articles}}))
    ))



;(def system
;  (wareblog-system {:http-port (http-port) :handler wrap-handler}))

(defonce server (atom nil))

(defn start-server []
  (do
    (info "Starting the server at port " (http-port))
    (reset! server (httpkit-server/run-server #'wrap-handler {:port (http-port) :join? false}))))

(defn stop-server []
  (when-not (nil? server)
    (@server :timeout 100)
    (reset! server nil)))
