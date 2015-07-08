(ns wareblog.http-component
  (require [org.httpkit.server :as httpkit-server :refer [run-server]]
           [taoensso.timbre :as timbre]
           [com.stuartsierra.component :as component]
           ))

;Provide alias for logging with timbre
(timbre/refer-timbre)

(defrecord Http [http-port handler server]

  component/Lifecycle

  (start [component]
    (info "Starting the server at port " http-port)
    
    (let [server (httpkit-server/run-server handler {:port http-port :join? false})]
      (assoc component :server server)))

  (stop [component]
    (info "Stopping the server at port " http-port)
    (when-not (nil? server)
      (server :timeout 100)
      (assoc component :server nil)))

  )

(defn new-http-server [http-port handler]
  (map->Http {:http-port http-port :handler handler}))
  
(defn example-system [http-port handler]
  (-> (component/system-map
       :http (new-http-server http-port handler))
      (component/system-using
       {:http []})))
