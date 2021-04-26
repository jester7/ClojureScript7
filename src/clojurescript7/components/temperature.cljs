(ns clojurescript7.components.temperature
  (:require
   [reagent.core :as r]
   [clojurescript7.helper.math :as math]
   [clojurescript7.components.title :refer [title]]))

(def temp-c (r/atom 0))
(def temp-f (r/atom 32))
(defn temp-validate [input-str]
  (or (math/numeric? input-str) (= "-" input-str) (= "" input-str) (= "." input-str) (= "-." input-str)))
(set-validator! temp-c temp-validate)
(set-validator! temp-f temp-validate)

; converts degrees C to deg. F
(defn c->f [c]
  (+ (* c (/ 9 5)) 32))

; converts degrees F to deg. C
(defn f->c [f]
  (* (- f 32) (/ 5 9)))

; temperature component
(defn temperature []
  [:div#temperature.applet
   [title "Temperature Converter" "🌡️"]
   [:div.row.wrapper
    [:input {:id "temp-input-c"
             :class "fixed-width-font bold"
             :value @temp-c
             :on-change
             (fn [e]
               (try
                 (reset! temp-c (-> e .-target .-value))
                 ; round the conversion to avoid JavaScript floating point issues
                 (reset! temp-f (math/round (c->f (-> e .-target .-value)) 2))
                 (catch :default e
                   ; for this simple example, just catch validator exception and ignore
                   (js/console.log "non numeric input detected"))))}]
    [:span "°C"]]
   [:div.row.wrapper
    [:input {:id "temp-input-f"
             :class "fixed-width-font bold"
             :value @temp-f
             :on-change
             (fn [e]
               (try
                 (reset! temp-f (-> e .-target .-value))
                 (reset! temp-c (math/round (f->c (-> e .-target .-value)) 2))
                 (catch :default e
                   ; for this simple example, just catch validator exception and ignore
                   (js/console.log "non numeric input detected"))))}]
    [:span "°F"]]])
