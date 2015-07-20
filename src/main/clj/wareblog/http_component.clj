(ns wareblog.http-component
  (require [org.httpkit.server :as httpkit-server :refer [run-server]]
           [taoensso.timbre :as timbre]
           [com.stuartsierra.component :as component]
           [bidi.ring :as bidi-ring :refer [make-handler resources resources-maybe]]
           [ring.middleware.params :refer [wrap-params]]
           [infrastructure.endpoint :as endpoint]
           ))

;Provide alias for logging with timbre
(timbre/refer-timbre)

(defn handler [routes]
  (bidi-ring/make-handler routes))

(defn wrap-handler [routes]
  (-> (handler routes)
      wrap-params))


(defrecord Http [http-port storage articles server state]

  component/Lifecycle

  (start [component]
    (info "Starting the server at port " http-port)
    (let [handler (wrap-handler (endpoint/get-routes articles))
                   server (httpkit-server/run-server handler {:port http-port :join? false})]
      (info "The server was started " server)
      (info "The articles component is " articles)
      (info "The storage is " storage)
      (assoc component :state "started" :server server)))

  (stop [component]
    (info "Stopping the server at port " http-port)
    (info "The http-component started server " server)
    (info "The state of the http-component is " state)
    (info (:server component))
    (info (:state component))
    (info "http-component uses storage " storage)
    (when-not (nil? server)
      (do 
        (server :timeout 100)
        (info "storage")
        (assoc component :server nil))))

  )

(defn new-http-server [http-port]
  (map->Http {:http-port http-port}))
