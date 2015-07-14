(ns wareblog.embedded-test
  (require [clojure.edn :as edn :refer [read-string]]
           [clojure.test :refer [deftest is testing use-fixtures]]
           [wareblog.embedded :as main]
           [wareblog.system :as system]
           [org.httpkit.client :as http-client :refer [get]]))

(def system
  (system/wareblog-system {:http-port 3002}))

(defn server-setup-and-teardown [f]
  (alter-var-root #'system main/start-system)
  (f)
  (alter-var-root #'system main/stop-system))

(use-fixtures :once server-setup-and-teardown)

(deftest handle-a-request
    (testing "We will do some REST API testing here"
      (is (= 1 1))
      (let [{:keys [status body headers] :as resp} @(http-client/get "http://localhost:3002/index.html")]
                 (is (= (str status) "200"))
                 (is (= (:server headers) "http-kit")))
      ))

;(edn/read-string {:readers edn-readers} (slurp (:body @(http-client/get "http://localhost:3000/articles/abc"))))
