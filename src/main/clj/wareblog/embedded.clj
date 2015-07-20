(ns wareblog.embedded
  (require [com.stuartsierra.component :as component]
           [taoensso.timbre :as timbre]
           [wareblog.system :as system])
  (:gen-class))

;Provide alias for logging with timbre
(timbre/refer-timbre)

;(set-resource-path! (clojure.java.io/resource "./resources/"))

(defn start-system [system]
  (component/start system))

(defn stop-system [system]
  (component/stop system))

(defn -main
  "Startup the embedded http server and the other components."
  [& args] 
  ;(start-server)
  (let [system (system/wareblog-system {})]
    (do
      (info "Try to start wareblog system " system)
      (component/start system))))
