(ns wareblog.core
  (require [wareblog.articles :refer [get-article get-article-as-html]]
           [liberator.core :refer [resource defresource]]
           [ring.middleware.params :refer [wrap-params]]
           [bidi.ring :refer [make-handler]]
           [org.httpkit.server :as server]
           [environ.core :refer [env]])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args] 
  (println "Hello, World!"))

;(defresource article
;  :available-media-types ["text/html"]
;  :allowed-methods [:get :options]
                                        ;  :handle-ok (fn [ctx] (str "Id of the request: " (get-in ctx [:request :route-params :id]))))

(def default-http-port
  3000)

(def http-port
  (let [ext-http-port (env :wareblog-http-port)]
    (if (nil? ext-http-port) default-http-port (Integer/valueOf ext-http-port))))

(defresource article
  :available-media-types ["application/edn" "text/html"]
  :allowed-methods [:get :options]
  :handle-ok #(let [media-type
                    (get-in % [:representation :media-type])
                    id (get-in % [:request :route-params :id])]
                (condp = media-type
                  "application/edn"  (get-article (keyword id))
                  "text/html" (get-article-as-html (keyword id)))))

(defresource comment-article
  :available-media-types ["text/html"]
  :allowed-methods [:get :options :post]
  :handle-ok (fn [ctx] (let [id (get-in ctx [:request :route-params :id])]
                         (get-article (keyword id)))))

(def handler
  (make-handler ["/" {"index.html" (resource :available-media-types ["text/html"]
                           :handle-ok "<html>Hello, Internet.</html>")
                      "articles/" {[:id] article
                                   [:id "/comment"] comment-article}}]))

(def wrap-handler
  (-> handler
      wrap-params))

(defonce server (atom nil))

(defn start-server []
  (reset! server (server/run-server #'wrap-handler {:port http-port :join? false})))

(defn stop-server []
  (when-not (nil? server)
    (@server :timeout 100)
    (reset! server nil)))
