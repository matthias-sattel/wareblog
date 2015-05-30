(ns wareblog.articles
  (require [clojure.edn :as edn]
           [taoensso.timbre :as timbre]))

;Provide alias for logging with timbre
(timbre/refer-timbre)

(def abbreviations
  {:edn {:label "edn"
         :name "Extensible Data Notation"
         :uri "https://github.com/edn-format/edn"
         }
   :md {:label "md"
        :name "Markdown"
        :uri "http://daringfireball.net/projects/markdown/"
        }
   })

(defn- parse-paragraph [part & more];should accept multiple args
  (str "<p>" part more "</p>"))

(def edn-readers
  {'wareblog/author str
   'wareblog/abbreviation #(str "<strong>" (:name (%1 abbreviations)) "</strong>")
   'wareblog/abstract #(reduce str %1)
   'wareblog/paragraph #(str "<p>" (reduce str %1) "</p>")
   ;'inst str
   })

(def some-edn-string
   "{:header \"My first experience with edn\"
                :created-on #inst \"2015-05-05T21:56:00Z\"
:created-by #wareblog/author {:given-name \"Matthias\" :name \"Sattel\"}
:content (#wareblog/abstract (\"This is actually the very first time that I am using \" #wareblog/abbreviation :edn \". In normal situation I would simply use an existing solution for markup of a blog, because there are already good solutions out there, e.g. \" #wareblog/abbreviation :md \". But in this case I want to learn new stuff and thus I will reinvent the wheel.\")
	 #wareblog/paragraph (\"Starting with something new is exciting, but can turn soon into frustration. Let's see how it works with \" #wareblog/abbreviation :edn)
  #wareblog/paragraph (\"The goal of my little project is to use \" #wareblog/abbreviation :edn \" for markup. I will use it to store articles and then render it to html using custom edn-readers.\"))
}")

(defn- article-to-html [article]
  (do
    ;(debug "Render article " article " to HTML ," (reduce str (:content
    ;                                               (edn/read-string
    ;                                                {:readers edn-readers}
    ;                                                article))))
    (reduce str
            (:content
             (edn/read-string
              {:readers edn-readers}
              article)))))

(def articles
  {:abc some-edn-string})

(defn get-article [id]
  (id articles))

(defn get-article-header [id]
  (:header
   (edn/read-string
    {:readers edn-readers}
    (get-article id))))

(defn get-article-as-html [id]
  (article-to-html (id articles)))


