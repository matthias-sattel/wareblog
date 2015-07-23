(ns wareblog.atom-storage-test
  (require [clojure.test :refer [deftest is testing use-fixtures]]
           [wareblog.atom-storage :as atom-storage]
           [wareblog.article-storage :as storage]
           [com.stuartsierra.component :as component]))

(def component
  (atom-storage/new-storage))

(defn component-startup-and-teardown [f]
  (component/start component)
  (f)
  (component/stop component))

(use-fixtures :once component-startup-and-teardown)

(deftest get-by-id
  (testing "Get some article by id"
    (is
     (=
      "\"My first experience with edn. A format for data exchange.\""
      (:header (storage/get-by-id component :abc))))
    ))
