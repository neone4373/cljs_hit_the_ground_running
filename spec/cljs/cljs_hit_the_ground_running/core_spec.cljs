(ns cljs-hit-the-ground-running.core-spec
  (:require-macros [speclj.core :refer [describe it should= run-specs]]
                   [clojure.core.async :refer [go]])
  (:require [speclj.core :as spec]
            [cljs-hit-the-ground-running.core]
            [om.dom :as dom :include-macros true]))



(describe "A ClojureScript test"
          (it "fail. Fix it!"
              (should= 0 1)))

(println "Hi Mom!")

;; Tutorial Tests:

(.log js/console (. js/document (getElementById "app"))
      ;(dom/getElementById "query")
      )
;; Shows the dom element named #query

;; indicates a click
#_(let [clicks (listen (dom/getElement "search") "click")]
  (go (while true
        (.log js/console (<! clicks)))))

;; test the jsonp query
;; (go (.log js/console (<! (jsonp (query-url "cats")))))

;; (run-specs)
