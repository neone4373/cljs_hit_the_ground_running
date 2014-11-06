(defproject cljs_hit_the_ground_running "0.1.0"
  :description "Sample App Showing off D3 and Om in CLJS"
  :url "http://cljsd3om.example.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src/clj" "src/cljs"]
  :test-paths ["spec/clj"]  ;; where the clj test files live
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2371" :scope "provided"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [ring "1.3.1"]
                 [compojure "1.2.0"]
                 [enlive "1.1.5"]
                 [om "0.7.3"]
                 [figwheel "0.1.4-SNAPSHOT"]
                 [environ "1.0.0"]
                 [com.cemerick/piggieback "0.1.3"]
                 [weasel "0.4.0-SNAPSHOT"]
                 [leiningen "2.5.0"]]
  :plugins [[lein-cljsbuild "1.0.3"]
            [lein-environ "1.0.0"]
            [speclj "3.1.0"]         ;; speclj dependency
            [lein-ancient "0.5.4"]]  ;; running lein ancient will give you a list of your out of date dependencies

  :min-lein-version "2.5.0"
  :uberjar-name "cljs_hit_the_ground_running.jar"

  :cljsbuild {:builds {:app {:source-paths ["src/cljs"]  ;; add the spec files to run the test upon autocompile
                             :compiler {:output-to     "resources/public/js/app.js"
                                        :output-dir    "resources/public/js/out"
                                        :source-map    "resources/public/js/out.js.map"
                                        :preamble      ["react/react.min.js"]
                                        :externs       ["react/externs/react.js"]
                                        :optimizations :none
                                        :pretty-print  false}}
                        ;; speclj looks for the dev when running so we need to create a whole new build for it
                        :dev {:source-paths ["src/cljs"  "spec/cljs"]  ;; add the spec files to run the test upon autocompile
                             :compiler {:output-to     "resources/public/js/app_spec.js"
                                        :output-dir    "resources/public/js/spec"
                                        :source-map    "resources/public/js/spec.js.map"
                                        :preamble      ["react/react.min.js"]
                                        :externs       ["react/externs/react.js"]
                                        :optimizations :whitespace
                                        :pretty-print  false}
                              :notify-command ["phantomjs"  "bin/speclj" "resources/public/js/app_spec.js"] ; notify the test results on auto-comile of the dev build
                              }}
              :test-commands {"test" ["phantomjs" "bin/speclj" "resources/public/js/app_spec.js"]} ;; initialize the specljs test with phantom
              }

  :profiles {:dev {:repl-options {:init-ns          cljs-hit-the-ground-running.server
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
                   :plugins      [[lein-figwheel "0.1.4-SNAPSHOT"]]
                   :figwheel     {:http-server-root "public"
                                  :port             3449
                                  :css-dirs         ["resources/public/css"]}
                   :env          {:is-dev true}
                   :dependencies [[org.clojure/clojurescript "0.0-2371"]
                                  [javax.servlet/servlet-api "2.5"] ;; testing the web server
                                  [ring-mock "0.1.5"]               ;; for testing ring responses
                                  [speclj "3.1.0"]]                 ;; dev testing dependencies
                   :cljsbuild    {:builds {:app {:source-paths   ["env/dev/cljs"]}}}
             :uberjar {:hooks [leiningen.cljsbuild]
                       :env {:production true}
                       :omit-source true
                       :aot :all
                       :cljsbuild {:builds {:app
                                            {:source-paths ["env/prod/cljs"]
                                             :compiler
                                             {:optimizations :advanced
                                              :pretty-print false}}}}}}})
