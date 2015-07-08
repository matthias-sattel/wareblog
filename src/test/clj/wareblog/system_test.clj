(ns wareblog.system-test
  (require [clojure.edn :as edn]
           [clojure.test :refer [deftest is testing use-fixtures]]
           [wareblog.system :as system]
           [com.stuartsierra.component :as component]
           [org.httpkit.client :as http-client]))

(def system
  (system/wareblog-system {:http-port 3005}))

(defn server-setup-and-teardown [f]
  (component/start system)
  (f)
  (component/stop system))

(use-fixtures :once server-setup-and-teardown)

(deftest handle-a-request
    (testing "We will do some REST API testing here"
      (is (= 1 1))
      (let [{:keys [status body headers] :as resp} @(http-client/get "http://localhost:3005/index.html")]
                 (is (= (str status) "200"))
                 (is (= (:server headers) "http-kit"))
      ))
    (testing "Find blog article by id"
      
      ))

;(edn/read-string {:readers edn-readers} (slurp (:body @(http-client/get "http://localhost:3000/articles/abc"))))
