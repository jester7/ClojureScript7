(ns clojurescript7.components.generic.progress)

;; Arguments: the desired id, the numeric progress atom, and optional class name
;; returns a wrapper div with inner div which is the progress meter
;; the inner progres bar's width is tied to the progress atom passed in
(defn progress-bar
  ([id prog-atom] (progress-bar id prog-atom "progress")) ; default class progress if 3rd argument omitted 
  ([id prog-atom class]
  [:div {:id id :class class}
   [:div.progress-inner {:style {:width (str @prog-atom "%")}} ]]))
