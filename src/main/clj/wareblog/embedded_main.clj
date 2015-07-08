(ns wareblog.embedded-main
  (require [wareblog.articles :refer [get-article get-article-as-html get-article-header]]
           [com.stuartsierra.component :as component]
           [taoensso.timbre :as timbre]
           [wareblog.system :as system])
  (:gen-class))

;Provide alias for logging with timbre
(timbre/refer-timbre)

;(set-resource-path! (clojure.java.io/resource "./resources/"))

;(defresource article
;  :available-media-types ["text/html"]
;  :allowed-methods [:get :options]
                                        ;  :handle-ok (fn [ctx] (str "Id of the request: " (get-in ctx [:request :route-params :id]))))

(defn start-system [system]
  (component/start system))

(defn stop-system [system]
  (component/stop system))

(defn -main
  "Just start a http-kit server."
  [& args] 
  ;(start-server)
  (let [system (system/wareblog-system {})]
    (do
      (info "Try to start wareblog system")
      (start-system (system)))))
