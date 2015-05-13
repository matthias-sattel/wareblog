(ns wareblog.core
  (require [clojure.edn :as edn]
           [liberator.core :refer [resource defresource]]
           [ring.middleware.params :refer [wrap-params]]
           [bidi.ring :refer [make-handler]]
           [org.httpkit.server :as server])
  (:gen-class))

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

(def edn-readers
  {'wareblog/author str
   'wareblog/abbreviation #(str "<strong>" (:name (%1 abbreviations)) "</strong>")
   'wareblog/abstract #(reduce str %1)
   'wareblog/paragraph #(first %1)
   ;'inst str
   })

(def some-edn
  (edn/read-string
   {:readers edn-readers}
               "{:header \"My first experience with edn\"
                :created-on #inst \"2015-05-05T21:56:00Z\"
:created-by #wareblog/author {:given-name \"Matthias\" :name \"Sattel\"}
:content (#wareblog/abstract (\"This is actually the very first time that I am using \" #wareblog/abbreviation :edn \". In normal situation I would simply use an existing solution for markup of a blog, because there are already good solutions out there, e.g. \" #wareblog/abbreviation :md \". But in this case I want to learn new stuff and thus I will reinvent the wheel.\")
	 #wareblog/paragraph (\"Starting with something new is exciting, but can turn soon into frustration. Let's see how it works with \" #wareblog/abbreviation :edn))
}"))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!" (:created-on some-edn)))

(defresource article [id]
  :available-media-types ["text/html"]
  :handle-ok (fn [id] (str "<html>" id "</html>")))

(def handler
  (make-handler ["/" {"index.html" (resource :available-media-types ["text/html"]
                           :handle-ok "<html>Hello, Internet.</html>")
                      ["articles/" :id "/article.html"] #(article)}]))
;["articles/" :id "/article.html"] :article}]))

(def wrap-handler
  (-> handler
      wrap-params))

(defonce server (atom nil))

(defn start-server []
  (reset! server (server/run-server #'wrap-handler {:port 3000 :join? false})))

(defn stop-server []
  (when-not (nil? server)
    (@server :timeout 100)
    (reset! server nil)))

