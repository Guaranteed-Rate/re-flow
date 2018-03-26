(defproject guaranteed-rate/re-flow "0.8.0"
  :description "A library that adds tools for building and executing workflows in re-frame applications"
  :url "https://github.com/Guaranteed-Rate/re-flow"
  :license {:name "MIT License"}
  :min-lein-version "2.5.2"

  :dependencies [[org.clojure/clojure "1.8.0"]]

  :plugins [[lein-codox "0.10.3"]]

  :codox {:language :clojurescript
          :metadata {:doc/format :markdown}
          :source-uri "https://github.com/Guaranteed-Rate/re-flow/blob/v{version}/{filepath}#L{line}"
          :doc-files []}

  :source-paths ["src/clj" "src/cljc" "src/cljs"]
  :test-paths ["test/cljc"]

  :clean-targets ["target"]

  :profiles {:provided {:dependencies [[org.clojure/clojurescript "1.9.908"]
                                       [re-frame "0.10.5"]
                                       [aysylu/loom "1.0.0"]]}
             :dev {:dependencies [[org.clojure/test.check "0.9.0"]
                                  [day8.re-frame/test "0.1.5"]]
                   :plugins [[lein-cljsbuild "1.1.7"]
                             [lein-doo "0.1.10"]]}
             :1.8 {:dependencies [[clojure-future-spec "1.9.0-beta4"]]}
             :1.9 {:dependencies [[org.clojure/clojure "1.9.0"]]}}

  :cljsbuild
  {:builds [{:id "test"
             :source-paths ["src/cljc" "src/cljs" "test/cljs" "test/cljc"]
             :incremental? true
             :compiler {:output-to "target/unit-test.js"
                        :output-dir "target"
                        :main re-flow.test-runner
                        :optimizations :none
                        :pretty-print true}}]}

  :aliases {"test-cljs" ["doo" "phantom" "test" "once"]
            "test-cljs-auto" ["doo" "phantom" "test"]
            "test-clj" ["with-profile" "1.8:1.9" "test"]
            "test-all" ["do" ["test-clj"] ["test-cljs"]]})
