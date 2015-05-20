(set-env!
	:source-paths #{"src","test"}
	:dependencies '[
			[adzerk/boot-test "1.0.4"]
			[adzerk/boot-cljs "0.0-2814-4"]
			[org.clojure/clojure "1.6.0"]
			[environ "1.0.0"]
			[ring/ring-core "1.3.2"]
			[http-kit "2.1.18"]
			[bidi "1.18.11"]
			[liberator "0.12.2"]
			[com.taoensso/timbre "3.4.0"]])

(require '[adzerk.boot-cljs :refer :all])
(require '[adzerk.boot-test :refer :all])

(task-options!
	pom {
	    :project 'wareblog
	    :version "0.1.0-SNAPSHOT" }
	aot {:all true}
	jar {
	    :manifest {"description" "Software for creating a weblog"}
	    :main 'wareblog.core })

(deftask build
	 "Building the project"
	 []
	 (comp
		(aot)
		(pom)
		(uber)
		(jar)
		(install)))