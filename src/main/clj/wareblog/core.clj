(ns wareblog.core
  (require [wareblog.articles :refer [get-article get-article-as-html get-article-header]]
           [liberator.core :as liberator :refer [resource defresource]]
           [ring.middleware.params :refer [wrap-params]]
           [bidi.ring :as bidi-ring :refer [make-handler]]
           [org.httpkit.server :as httpkit-server :refer [run-server]]
           [environ.core :refer [env]]
           [taoensso.timbre :as timbre]
           [selmer.parser :refer [render render-file set-resource-path!]])
  (:gen-class))

;Provide alias for logging with timbre
(timbre/refer-timbre)

;(set-resource-path! (clojure.java.io/resource "./resources/"))

;(defresource article
;  :available-media-types ["text/html"]
;  :allowed-methods [:get :options]
                                        ;  :handle-ok (fn [ctx] (str "Id of the request: " (get-in ctx [:request :route-params :id]))))

(def default-http-port
  3000)

(def http-port
  (let [ext-http-port (env :wareblog-http-port)]
    (if (nil? ext-http-port) default-http-port (Integer/valueOf ext-http-port))))

(liberator/defresource article
  :available-media-types ["application/edn" "text/html"]
  :allowed-methods [:get :options]
  :handle-ok #(let [media-type
                    (get-in % [:representation :media-type])
                    id (get-in % [:request :route-params :id])]
                (do
                  (debug (get-article (keyword id)))
                  (condp = media-type
                  "application/edn"  (get-article (keyword id))
                  "text/html" (render-file "templates/article.html" {:title (get-article-header (keyword id)), :article (get-article-as-html (keyword id))})))))

(liberator/defresource comment-article
  :available-media-types ["text/html"]
  :allowed-methods [:get :options :post]
  :handle-ok (fn [ctx] (let [id (get-in ctx [:request :route-params :id])]
                         (get-article (keyword id)))))

(liberator/defresource home
  :available-media-types ["text/html"]
  :handle-ok (render-file "templates/home.html" {:name "World"}))

(def handler
  (bidi-ring/make-handler ["/" {"" home
                                "index.html" home
                                "articles/" {[:id] article
                                             [:id "/comment"] comment-article}}]))

(def wrap-handler
  (-> handler
      wrap-params))

(defonce server (atom nil))

(defn start-server []
  (do
    (info "Starting the server at port " http-port)
    (reset! server (httpkit-server/run-server #'wrap-handler {:port http-port :join? false}))))

(defn stop-server []
  (when-not (nil? server)
    (@server :timeout 100)
    (reset! server nil)))

(defn -main
  "Just start a http-kit server."
  [& args] 
  (start-server))
