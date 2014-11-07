I made the switch to [Clojure](http://clojure.org/ "Clojure") a little over a year ago, and since then have become obsessed with pulling as much of the development process into the Clojure world as practical.  While I have been looking to get into [ClojureScript](https://github.com/clojure/clojurescript "ClojureScript") for a long time I have always had a lot of trouble getting started.  Often I would block off some time to build a sample app and spend almost all of my time struggling to get the repl to connect to the browser, and be too exhausted to actually build anything.  Fortunately I have finally gotten everything behaving, today I am going to walk you through doing the same.

A little about my setup, in my Clojure development environment I run TTD with [Speclj](http://www.speclj.com "Speclj"), and live coding in [Emacs](https://www.gnu.org/software/emacs/ "Emacs") with the excellent [Emacs-Live](https://github.com/overtone/emacs-live "Emacs-Live") package.  This allow for an instant feedback cycle for both running code snippet, in emacs documentation, unit tests, and the full end to end tests.  It was very important to me to have the same degree of control and be able to use the same tools for my ClojureScript development environment.  Lucky for me I discovered [Chestnut](https://github.com/plexus/chestnut "Chestnut").

Chestnut is a Clojure(Script) template that out of the box gives you [Ring](https://github.com/ring-clojure/ring "Ring") support for web application support, [Figwheel](https://github.com/bhauman/lein-figwheel "Figwheel") to push ClojureScript changes to the client, [Weasel](https://github.com/tomjakubowski/weasel "Weasel") for the ClojureScript Browser REPL connection via WebSocket, and the powerful [Om](https://github.com/swannodette/om "Om") to give us the latest and greatest from Facebook’s [React](https://github.com/facebook/react "React") library.  

Note: On my system I am running [Leiningan](https://github.com/technomancy/leiningen "Leiningen") 2.5.0 on [Java](https://www.java.com/en/ "Java") 1.8.0_05 Java HotSpot(TM) 64-Bit Server VM, I have not tested this on any other platform, YMMV.

Getting started with Chestnut is as simple as:

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
(defproject cljs_hit_the_ground_running "0.1.0"
  :description "Sample App Showing off Speclj and Om in CLJS"
  :url "http://www.example.com/cljs_hit_the_ground_running"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src/clj" "src/cljs"]
  :test-paths ["spec/clj"]  ;; where the clj test files live
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2371" :scope "provided"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]  ;; always a nice touch
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

  :cljsbuild {:builds {:app {:source-paths ["src/cljs"] 
                             :compiler {:output-to     "resources/public/js/app.js"
                                        :output-dir    "resources/public/js/out"
                                        :source-map    "resources/public/js/out.js.map"
                                        :preamble      ["react/react.min.js"]
                                        :externs       ["react/externs/react.js"]
                                        :optimizations :none
                                        :pretty-print  false}}
                        ;; speclj looks for the dev when running so we need to create a whole new build for it
                        :dev {:source-paths ["src/cljs"  "spec/cljs"]  ;; add the spec folder to run the test upon autocompile
                             :compiler {:output-to     "resources/public/js/app_spec.js"
                                        :output-dir    "resources/public/js/spec"
                                        :source-map    "resources/public/js/spec.js.map"
                                        :preamble      ["react/react.min.js"]
                                        :externs       ["react/externs/react.js"]
                                        :optimizations :whitespace
                                        :pretty-print  false}
                              :notify-command ["phantomjs"  "bin/speclj" "resources/public/js/app_spec.js"] ; notify the test results on auto-comile of the dev build
                              }}
              :test-commands {"test" ["phantomjs" "bin/speclj" "resources/public/js/app_spec.js"]} ;; initialize the specljs tests with phantomjs
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
```

To run the cljs tests we are going to need [PhantomJs](http://phantomjs.org/ "PhantomJS") installed.  If you use [Homebrew](http://brew.sh/ "Homebrew") it is as simple as `$ brew install phantomjs`.  Once that is ready to go create runner file for PhantomJs:

`bin/speclj`

```javascript
#! /usr/bin/env phantomjs

var fs = require("fs");
var p = require('webpage').create();
var sys = require('system');
var url = phantom.args[0];

p.onConsoleMessage = function (x) {
    fs.write("/dev/stdout", x, "w");
};

p.injectJs("resources/public/js/polyfill.js");
// note the polyfill file, this is added to play nice with React

p.injectJs(phantom.args[0]);

var result = p.evaluate(function () {
    speclj.run.standard.armed = true;
    return speclj.run.standard.run_specs(
        cljs.core.keyword("color"), true
    );
});

phantom.exit(result);
```

Since PhantomJs is an older JavaScript runtime we need to add in some new cutting edge JS to make it play nice with React.

`resources/public/js/polyfill.js`

```javascript
if (!Function.prototype.bind) {
  Function.prototype.bind = function(oThis) {
    if (typeof this !== 'function') {
      // closest thing possible to the ECMAScript 5
      // internal IsCallable function
      throw new TypeError('Function.prototype.bind - what is trying to be bound is not callable');
    }

    var aArgs   = Array.prototype.slice.call(arguments, 1),
        fToBind = this,
        fNOP    = function() {},
        fBound  = function() {
          return fToBind.apply(this instanceof fNOP && oThis
                 ? this
                 : oThis,
                 aArgs.concat(Array.prototype.slice.call(arguments)));
        };

    fNOP.prototype = this.prototype;
    fBound.prototype = new fNOP();

    return fBound;
  };
}
```

Now drop in some stubs for the test files:

`spec/clj/cljs_hit_the_ground_running/server_spec.clj`

```clojure
(ns cljs-hit-the-ground-running.server-spec
  (:require [speclj.core :refer :all]
            [cljs-hit-the-ground-running.server :refer :all]))

(describe "Web Tests"
          (it "It fails FIXME"
              (should= 0 1)))
```

`spec/cljs/cljs_hit_the_ground_running/core_spec.cljs`

```clojure
(ns cljs-hit-the-ground-running.core-spec
  (:require-macros [speclj.core :refer [describe it should= run-specs]]
                   [clojure.core.async :refer [go]])
  (:require [speclj.core :as spec]
            [cljs-hit-the-ground-running.core]
            [om.dom :as dom :include-macros true]))

(describe "A ClojureScript test"
          (it "fail. Fix it!"
              (should= 0 1)))

;; (println "Hi Mom!")

;; Tutorial Tests:

#_(.log js/console (. js/document (getElementById "app"))
      ;(dom/getElementById "query")
      )
;; Shows the dom element named #query

;; indicates a click
#_(let [clicks (listen (dom/getElement "search") "click")]
  (go (while true
        (.log js/console (<! clicks)))))

;; test the jsonp query
;; (go (.log js/console (<! (jsonp (query-url "cats")))))

```

Update the default app state for our hello world moment:

`src/cljs/cljs_hit_the_ground_running/core_spec.cljs`

```clojure
(ns cljs-hit-the-ground-running.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defonce app-state (atom {:text "Hello Moo Cow!"})) ;; Changing this :text will change the h1 element displays in the browser

(defn main []
  (om/root
    (fn [app owner]
      (reify
        om/IRender
        (render [_]
          (dom/h1 nil (:text app)))))
    app-state
    {:target (. js/document (getElementById "app"))}))
```

If you do not plan on using Emacs-Live you can skip the next two sections, otherwise read on. 

Now is a good time to double check the version of [Cider](https://github.com/clojure-emacs/cider "Cider") being run by your `M-x cider-jack-in`.  At the time of this writing the default Emacs-Live installation had cider 0.6.0, which does not play well with with the cljs repl.  The easiest way to update this is change your ~/.lein/profiles.clj to look like so.  Also if you already have a profiles.clj, you can just add the two plug-ins below and it should take care of it.

`~/.lein/profiles.clj`

```clojure
{:user {:plugins [
    [cider/cider-nrepl "0.7.0"]
    [lein-localrepo "0.5.3"]]}}
```

Additionally, now that we have cider 0.7.0 the Emacs plug-in [Auto-Complete-Mode](http://cx4a.org/software/auto-complete/ "Auto-Complete-Mode") will start causing all sorts of hanging and trouble.  I wish I had a non-hacky way to solve this but sadly the intricacies of the Emacs-Live package system are still opaque to me.  The hacky solution is in emacs `M-x package-install company` which will give us [Company-Mode](https://company-mode.github.io/ "Company-Mode") a non-buggy replacement for Auto-Complete-Mode.  Then whenever you open a clj do `M-x auto-complete-mode` to disable it.  I know, its really hacky but for now this is the best I have so let's just get to the good part.

With the heavy lifting complete let's confirm we are good to go.

Step one, Speclj auto-run testing:

```bash
$ lein spec -a
# let's hope you see something similar to the below:

----- Thu Nov 06 15:07:15 EST 2014 -------------------------------------------------------------------
took 0.10117 to determine file statuses.
reloading files:
  /.../cljs_hit_the_ground_running/spec/clj/cljs_hit_the_ground_running/server_spec.clj

Web Tests
- fail. Fix it! (FAILED)

Failures:

  1) Web Tests fail. Fix it!
     Expected: 0
          got: 1 (using =)
     /.../cljs_hit_the_ground_running/spec/clj/cljs_hit_the_ground_running/server_spec.clj:7

Finished in 0.00084 seconds
1 examples, 1 failures
```

Step two, Specljs auto-run and auto-compile testing
Ideally this would run in the repl/cider-jack-in, but I couldn't figure out a clean way to do it.  Fortunately we can just run an separate cljsbuild in the terminal to do that auto-testing for us.

```bash
$ lein cljsbuild auto dev

Compiling "resources/public/js/app_spec.js" from ["src/cljs" "spec/cljs"]...
F

Failures:

  1) A ClojureScript test fail. Fix it!
     Expected: 0
          got: 1 (using =)
     at eval_characteristic (resources/public/js/app_spec.js:2278)

Finished in 0.00100 seconds
1 examples, 1 failures
Successfully compiled "resources/public/js/app_spec.js" in 14.851 seconds.
```

Step three, this one is for all the cookies.  This can be done either way depending on your preference.

Straight from the repl

```bash
$ lein repl
cljs-hit-the-ground-running.server=> (run)
cljs-hit-the-ground-running.server=> (browser-repl)
```

In Emacs

```emacs
M-x cider-jack-in
# C-c M-j for the cool kids
# also not because emacs cider doesn't default to the user name space we
# need to fully qualify (run) and (browser-repl)
clojure.core> (cljs-hit-the-ground-running.server/run)
clojure.core>  (cljs-hit-the-ground-running.dev/browser-repl)
```

Now open your favorite web browser to `localhost:10555`. If everything went well and we haven't fallen out of the development gods' favor, then you should see "Hello Moo Cow!" smiling down at you.

Finally, our last step is to inform the browser you are its new master by issuing it a command:

```clojure
> (js/alert "There is a new sheriff in town.")
```

Note: if the command gave you an error like this, `java.io.IOException: No client connected to Websocket` try refreshing `localhost:10555` and it should now be connected.

With that you should now have:
1. An auto-run Speclj service alerting you if your .clj test pass after every save
2. An auto-run auto-compile Specljs service alerting you for your .cljs tests
3. A repl connection allowing you to interact live with the browser

I have, left the making the test pass up to the reader.  Once this is done you should be ready to get hacking on an Om.  For next steps I would recommend David Nolan's [basic tutorial](https://github.com/swannodette/om/wiki/Basic-Tutorial "Basic Tutorial") to get you started with the Om's core concepts.  Though it is light table centric, there is plenty to be gained from any development environment.  The full project source is available here: `https://github.com/neone4373/cljs_hit_the_ground_running`.

Thanks for reading.

