(ns cljs-hit-the-ground-running.core-spec
  (:require-macros [speclj.core :refer [describe it should=]])
  (:require [speclj.core]
              [cljs-hit-the-ground-running.core]))

(describe "A ClojureScript test"
          (it "fail. Fix it!"
              (should= 1 1)))


;; Tutorial Tests:

;; (.log js/console (dom/getElement "query"))
;; Shows the dom element named #query

;; indicates a click
#_(let [clicks (listen (dom/getElement "search") "click")]
  (go (while true
        (.log js/console (<! clicks)))))

;; test the jsonp query
;; (go (.log js/console (<! (jsonp (query-url "cats")))))
