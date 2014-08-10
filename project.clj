(defproject diamond "0.1.0-SNAPSHOT"
  :description "Diamond: "
  :url "https://github.com/dackerman/diamond"
  :license {:name "No license"
            :url ""}
  :source-paths ["src/clj" "src/cljs"]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [ring "1.3.0"]
                 [compojure "1.1.8"]
                 [http-kit "2.1.16"]
                 [com.thinkaurelius.titan/titan-all "0.4.4"]
;                 [com.sleepycat/je "5.0.73"]
                 [org.clojure/clojurescript "0.0-2280"]]
  :main ^:skip-aot diamond.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}
  :plugins [[lein-cljsbuild "1.0.3"]
            [lein-ring "0.8.11"]]
  :ring {:handler diamond.core/handler}
  :cljsbuild {:builds
              [{;; CLJS source code path
                :source-paths ["src/cljs"]

                ;; Google Closure (CLS) options configuration
                :compiler {:externs ["externs/d3_externs_min.js"]
                           ;; CLS generated JS script filename
                           :output-to "resources/public/js/core.js"

                           ;; minimal JS optimization directive
                           :optimizations :whitespace

                           ;; generated JS code prettyfication
                           :pretty-print true}}]})
