(ns wareblog.system
  (require [com.stuartsierra.component :as component]
           [wareblog.http-component :as http-component]
           [wareblog.atom-storage :as atom-storage]
           [wareblog.articles :as articles]
           [environ.core :refer [env]]
           [taoensso.timbre :as timbre]))

;Provide alias for logging with timbre
(timbre/refer-timbre)

(def default-http-port
  3000)

(defn http-port [port]
  (let [ext-http-port (env :wareblog-http-port)]
    (if (not (nil? port))
      port
      (if (nil? ext-http-port) default-http-port (Integer/valueOf ext-http-port)))))

(defn wareblog-system [config-options]
  (do 
    (info "Building wareblog system")
    (-> (component/system-map
         :article-storage (atom-storage/new-storage)
         :articles (articles/new-articles-component)
         :http (http-component/new-http-server (http-port (:http-port config-options))))
        (component/system-using {:articles {:storage :article-storage}
                                 :http {:storage :article-storage
                                        :articles :articles}}))
    ))
