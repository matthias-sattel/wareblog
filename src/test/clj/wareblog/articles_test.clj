(ns wareblog.articles-test
  (require [clojure.test :refer [deftest is testing use-fixtures]]
           [wareblog.articles :as articles]
           [infrastructure.endpoint :as endpoint]
           [com.stuartsierra.component :as component])
  )

(def mocked-storage
  nil)

(def component
  (-> (articles/new-articles-component)
      (assoc :storage mocked-storage)))

(defn component-startup-and-teardown [f]
  (component/start component)
  (f)
  (component/stop component))

(use-fixtures :once component-startup-and-teardown)

(deftest get-routes
  (testing "Get the default routes"
    (is
     (=
      "/"
      (first (endpoint/get-routes component))))
    (is
     (=
      ""
      (-> (endpoint/get-routes component)
                                  rest
                                  first
                                  first
                                  key)))
    ))
