(ns clojurescript7.components.timer
  (:require
   [reagent.core :as r]
   [clojurescript7.components.generic.progress :refer [progress-bar]]
   [clojurescript7.components.title :refer [title]]))

(def timer-progress (r/atom 0))
(def elapsed-time (r/atom 0))
(def end-time (r/atom 15))

(defn timer-update! [interval]
  (swap! elapsed-time + 0.1)
  (when (>= @elapsed-time @end-time)
    (js/clearInterval @interval)
    (reset! interval nil))
  (reset! timer-progress (* (/ @elapsed-time @end-time) 100)))

(defn timer-reset! [interval]
  (when (and (> @end-time @elapsed-time) (nil? @interval))
    ; timer resolution 100ms so that progress bar animation only lags 100ms behind timer increment
    ; the progress bar animation is done through css transition property on the width, duration 0.1s
    (reset! interval (js/setInterval #(timer-update! interval) 100))))

(def interval (r/atom (js/setInterval #(timer-update! interval) 100))) ; timer resolution 100ms to reduce prog bar lag

(defn timer []
  [:div#timer.applet
   [title "Timer" "⏲️"]
   [:div#progress-row.row.wrapper
    [progress-bar "timer-progress" timer-progress]]
   [:div.row.wrapper
    [:div#elapsed.large-label (Math/floor @elapsed-time) "s"]] ; timer resolution is 100 ms, so we need to discard decimals
   [:div.row.wrapper
    [:div#duration.info "Duration: " @end-time "s"]]
   [:div.row.wrapper
    [:div#timer-slider-wrapper
     [:input#timer-slider.slider
      {:type "range" :min 5 :max 600 :value @end-time
       :on-change
       (fn [e]
         (reset! end-time (-> e .-target .-value))
         (timer-reset! interval))}]]]
   [:div.row.wrapper
    [:button#reset-button
     {:type "button"
      :on-click (fn []
                  (reset! elapsed-time 0)
                  (timer-reset! interval))}
     "Reset"]]])