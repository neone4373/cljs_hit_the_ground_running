I made the switch to [Clojure](http://clojure.org/ "Clojure") a little over a year ago, and since then have became obsessed with pulling as much of the development process into the Clojure world as possible.  While I have been looking to get into [ClojureScript]( ) for a long time I have always had a lot of trouble getting started.  Often I would block off some time to build a sample app and spend almost all of my time trying to get the repl to connect to the browser, and be too exhausted to actually build anything.  Fortunately I have finally gotten everything working very well, I today I am going to walk you through getting this setup working.

A little about my setup, in my Clojure development environment I run TTD with [Speclj](http://www.speclj.com “Speclj”), and live coding with the excellent [Emacs-Live](https://github.com/overtone/emacs-live “”Emacs-Live”).  This allow for an instant feedback cycle for both running code snippet, in emacs documentation, unit tests, and the full end to end tests.  It was very important to me to have the same degree of control and being able to use the same tools for my ClojureScript development environment.  Lucky for me I discovered [Chestnut](https://github.com/plexus/chestnut “Chestnut”).

Chestnut is a Clojure(Script) template that out of the box gives you [Ring](https://github.com/ring-clojure/ring “Ring”) support for web application support, [Figwheel](https://github.com/bhauman/lein-figwheel “Figwheel”) to push ClojureScript changes to the client, [Weasel](https://github.com/tomjakubowski/weasel “Weasel”) for the ClojureScript Browser REPL iver WebSocket, and the powerful [Om](https://github.com/swannodette/om “Om”) to give us the latest and greatest from Facebook’s [React](https://github.com/facebook/react “React”) library.  

Note: On my system I am running [Leiningan](https://github.com/technomancy/leiningen “Leiningen”) 2.5.0 on [Java](https://www.java.com/en/ “Java”) 1.8.0_05 Java HotSpot(TM) 64-Bit Server VM, I have not tested this on any other platform, so YMMV.

Getting started is as simple as:

```bash
$ lein new chestnut cljs_hit_the_ground_running
```

This provides us with the following:

```bash
$ tree
├── LICENSE
├── Procfile
├── README.md
├── env
│   ├── dev
│   │   └── cljs
│   │       └── cljs_hit_the_ground_running
│   │           └── dev.cljs
│   └── prod
│       └── cljs
│           └── cljs_hit_the_ground_running
│               └── prod.cljs
├── project.clj
├── resources
│   ├── index.html
│   └── public
│       └── css
│           └── style.css
├── src
│   ├── clj
│   │   └── cljs_hit_the_ground_running
│   │       ├── dev.clj
│   │       └── server.clj
│   └── cljs
│       └── cljs_hit_the_ground_running
│           └── core.cljs
└── system.properties
```

Taking a look at the project.clj we can see that a lot of the dependencies are already there for us.  If any of this is unfamiliar to you I would urge you to invest the time to understand what is going on here, it can really save a lot of debugging time down the road.

While this is an amazing start we still are going to need to do a bit of work to get everything running smoothly.  First lets start by adding some dependencies that I have found to make everything run more smoothly, and our Speclj dependencies.  This is what my project.clj file looks like after adding in those changes.

```clojure
(defproject cljs_hit_the_ground_running "0.1.0"            ;; Updated away from snapshot
  :description "Sample App Showing off D3 and Om in CLJS"  ;; added a custom descrition
  :url "http://cljsd3om.fixitwithcode.com"                 ;; where the project lives
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src/clj" "src/cljs"]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2371" :scope "provided"]
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
            [lein-environ "1.0.0"]]
  :min-lein-version "2.5.0"
  :uberjar-name "cljs_hit_the_ground_running.jar"
  :cljsbuild {:builds {:app {:source-paths ["src/cljs"]
                             :compiler {:output-to     "resources/public/js/app.js"
                                        :output-dir    "resources/public/js/out"
                                        :source-map    "resources/public/js/out.js.map"
                                        :preamble      ["react/react.min.js"]
                                        :externs       ["react/externs/react.js"]
                                        :optimizations :none
                                        :pretty-print  true}}}}
  :profiles {:dev {:repl-options {:init-ns cljs-hit-the-ground-running.server
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
                   :plugins [[lein-figwheel "0.1.4-SNAPSHOT"]]
                   :figwheel {:http-server-root "public"
                              :port 3449
                              :css-dirs ["resources/public/css"]}
                   :env {:is-dev true}
                   :cljsbuild {:builds {:app {:source-paths ["env/dev/cljs"]}}}}
             :uberjar {:hooks [leiningen.cljsbuild]
                       :env {:production true}
                       :omit-source true
                       :aot :all
                       :cljsbuild {:builds {:app
                                            {:source-paths ["env/prod/cljs"]
                                             :compiler
                                             {:optimizations :advanced
                                              :pretty-print false}}}}}})
```

To finish off the testing suit you need to make sure you have [Phantomjs](http://phantomjs.org/ "PhantomJS") installed.  If you use [Homebrew](http://brew.sh/ "Homebrew") it is as simple as `$ brew install phantomjs`.  Once that is ready to go create the directory and file `bin/speclj`:

```sh
#! /usr/bin/env phantomjs

var fs = require("fs");
var p = require('webpage').create();
var sys = require('system');

p.onConsoleMessage = function (x) {
    fs.write("/dev/stdout", x, "w");
};

p.injectJs(phantom.args[0]);

var result = p.evaluate(function () {
  speclj.run.standard.armed = true;
  return speclj.run.standard.run_specs(
     cljs.core.keyword("color"), true
  );
});

phantom.exit(result);
```

Now drop in some stubs for the test files, I will leave it to the ready to make the test pass:

`spec/clj/cljs_hit_the_ground_running/server_spec.clj`

```clojure
(ns cljs-hit-the-ground-running.server-spec
  (:require [speclj.core :refer :all]
            [cljs-hit-the-ground-running.server :refer :all]))

(describe "Web Tests"
          (it "It has app"
              (should-not run)))
```

`spec/cljs/cljs_hit_the_ground_running/core_spec.cljs`

```clojure
(ns cljs-hit-the-ground-running.core-spec
  (:require-macros [speclj.core :refer [describe it should=]])
  (:require [speclj.core :refer :all]
            [cljs-hit-the-ground-running.core :refer :all]))

(describe "A ClojureScript test"
  (it "fail. Fix it!"
    (should= 0 1)))


;; Tutorial Tests:

;; (.log js/console (dom/getElement "query"))
;; Shows the dom element named #query

;; indicates a click
#_(let [clicks (listen (dom/getElement "search") "click")]
  (go (while true
        (.log js/console (<! clicks)))))

;; test the jsonp query
;; (go (.log js/console (<! (jsonp (query-url "cats")))))
```

If you do not plan on using Emacs-Live you can skip this section, otherwise read on. Now is a good time to double check the version of cider your version of cider.  At the time of these writing the default Emacs-Live installation had 










