(ns clojurescript7.components.cells
  (:require
   [clojurescript7.components.cells.utility :as util :refer
    [max-rows max-cols cells-map current-formula update-selection! has-formula?]]
   [clojurescript7.components.cells.parser :as parser]
   [clojurescript7.components.title :refer [title]]))
 
(def ^:const empty-cell {:formula "" :format "" :value ""})


(defn cells []
  [:div#cells.applet
   [title "Cells" "📒"]
   [:div#cells-toolbar.row.wrapper
    [:span.toolbar-label "f(x)"]
    [:input#formula-input {:value @current-formula
                           :on-change #()}]]
   [:div#spreadsheet.wrapper (util/keyboard-navigation)
    (doall (for [row (range 0 max-rows)]
             [:div.row.wrapper {:key (str "row" row)}
              [:span.row-label row]
              (doall (for [col (range 1 max-cols)]
                       (if (= row 0)
                         [:span.col-label {:key (str "col-label-" (char (+ col 64)))} (char (+ col 64))]
                         [:input {:read-only true :key (util/cell-ref row col) :data-row row :data-col col :id (util/cell-ref row col)
                                  :on-click
                                  #(update-selection! (.-target %1))
                                  :on-double-click
                                  (fn [e]
                                    (update-selection! (.-target e) true)
                                    (set! (-> e .-target .-readOnly) false))
                                  :on-blur
                                  (fn [e]
                                    (set! (-> e .-target .-readOnly) true) ; set back to readonly
                                    (let [val (-> e .-target .-value)
                                          row (js/parseInt (-> e .-target .-dataset .-row))
                                          col (js/parseInt (-> e .-target .-dataset .-col))
                                          cell-ref (util/cell-ref row col)]
                                      (when (not= val "") ; don't update the map if value is empty string
                                        (if (has-formula? cell-ref)
                                          (do ; TODO problem is here
                                            (let [c-map {:formula (if (util/is-formula? val) val (:formula (@cells-map cell-ref)))  ;(or (:formula (@cells-map cell-ref)) val)
                                                         :format ""
                                                         :value (if (util/is-formula? val) (parser/parse-formula val) val)}]
                                              (set! (-> e .-target .-value) (:value c-map))
                                              (reset! cells-map
                                                      (assoc @cells-map (util/cell-ref row col) c-map))))
                                          (do
                                            (reset! cells-map
                                                    (assoc @cells-map (util/cell-ref row col) val)))))))}])))]))]
                                                    [:div#clippy]])