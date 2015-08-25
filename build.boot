(set-env!
	:source-paths #{"src/main/clj","src/test/clj","src/main/resources"}
	:resource-paths #{"src/main/resources","src/test/resources"}	
	:dependencies '[
			[adzerk/boot-test "1.0.4"]
			[adzerk/boot-cljs "0.0-2814-4"]
			[org.clojure/clojure "1.7.0-RC1"]
			[environ "1.0.0"]
			[ring/ring-core "1.3.2"]
			[http-kit "2.1.18"]
			[bidi "1.18.11"]
			[liberator "0.12.2"]
			[com.taoensso/timbre "3.4.0"];logging
			[selmer "0.8.2"];templates
			[com.stuartsierra/component "0.2.3"]
			])

(require '[adzerk.boot-cljs :refer :all])
(require '[adzerk.boot-test :refer :all])

(task-options!
	pom {
	    :project 'wareblog
	    :version "0.1.0-SNAPSHOT" }
	;aot {:all true}
	aot {:namespace '#{wareblog.embedded}}
	jar {
	    :manifest {"description" "Software for creating a weblog"}
	    :main 'wareblog.embedded })

(deftask build
	 "Building the project"
	 []
	 (comp
		(aot)
		(pom)
		(uber)
		(jar)
		(install)))