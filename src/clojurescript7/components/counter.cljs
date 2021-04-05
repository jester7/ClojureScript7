(ns clojurescript7.components.counter
  (:require
   [reagent.core :as r]
   [clojurescript7.helper.dom :as cljs7-dom]
   [clojurescript7.components.title :refer [title]]))

(def click-count (r/atom 0))

(defn counter []
  [:div#counter.applet
   [title "Counter" "🧮"]
   [:div#counter-label-row.row.wrapper
    [:div.huge-label {:id "counter-label"} @click-count]]
   [:div.row.wrapper
    [:button#counter-button
     {:on-click (fn []
                  (cljs7-dom/animate-then
                   "counter-label-row" @click-count #(swap! click-count inc)))}
     (if (cljs7-dom/touch-device?) "Tap Me" "Click Me")]]])