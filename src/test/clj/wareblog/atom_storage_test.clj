(ns wareblog.atom-storage-test
  (require [clojure.test :refer [deftest is testing use-fixtures]]
           [wareblog.atom-storage :as atom-storage]
           [wareblog.article-storage :as storage]
           [com.stuartsierra.component :as component]
           [clojure.java.io :as io]))

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
      (:title (storage/get-by-id component (keyword "1")))))
    ))

(def example-edn
  {:title "\"My first experience with edn. A format for data exchange.\""
   :created-on "#inst \"2015-05-05T21:56:00Z\""
   :created-by "#wareblog/author {:given-name \"Matthias\" :name \"Sattel\"}"
   :abstract "#wareblog/abstract [\"This is the very first time that I am using \" #wareblog/abbreviation :edn \". In normal situation I would simply use an existing solution for markup of a blog, because there are already good solutions out there, e.g. \" #wareblog/abbreviation :md \". But in this case I want to learn new stuff and thus I will reinvent the wheel.\"]"
   :content "#wareblog/content [#wareblog/paragraph [\"Starting with something new is exciting, but can turn soon into frustration. Let's see how it works with \" #wareblog/abbreviation :edn]
   #wareblog/paragraph [\"The goal of my little project is to use \" #wareblog/abbreviation :edn \" for markup. I will use it to store articles and then render it to html using custom edn-readers.\"]]"})

(deftest test-import-from-file
  (testing "Import from a file"
    (storage/import-from-file component "./testdata.edn") 
    (is
     (=
      (count (storage/get-all component))
      3))))
