(ns wareblog.embedded-test
  (require [clojure.edn :as edn :refer [read-string]]
           [clojure.test :refer [deftest is testing use-fixtures]]
           [wareblog.embedded :as main]
           [wareblog.system :as system]
           [org.httpkit.client :as http-client :refer [get]]))

(def port
  3002)

(def system
  (system/wareblog-system {:http-port port}))

(defn server-setup-and-teardown [f]
  (alter-var-root #'system main/start-system)
  (f)
  (alter-var-root #'system main/stop-system))

(use-fixtures :once server-setup-and-teardown)

(deftest handle-a-request
  (testing "We will do some REST API testing here"
    (is (= 1 1))
    (let [{:keys [status body headers] :as resp} @(http-client/get (str "http://localhost:" port "/index.html"))]
      (is (= (str status) "200"))
      (is (= (:server headers) "http-kit")))
    ))

(deftest get-an-article-by-id
  (testing "Get an article by id"
    (let [{:keys [status body headers] :as resp} @(http-client/get (str "http://localhost:" port "/articles/1"))
          article (read-string (slurp body))]
      (println "Body of the request " article)
      (println "Title of the request " (:title article))
      (println "Headers of the request " headers)
      (is (= (str status) "200"))
      (is (not (empty? article)))
      (is (= (:title article) "\"My first experience with edn. A format for data exchange.\""))
      )))

;(edn/read-string {:readers edn-readers} (slurp (:body @(http-client/get "http://localhost:3000/articles/abc"))))
