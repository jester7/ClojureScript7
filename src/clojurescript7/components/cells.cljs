(ns clojurescript7.components.cells
  (:require
   [clojurescript7.components.cells.utility :as util :refer
    [max-rows max-cols cells-map current-formula update-selection!]]
   [clojurescript7.components.cells.parser :as parser]
   [clojurescript7.components.title :refer [title]]))

(def ^:const empty-cell {:formula "" :format "" :value ""})


(defn cells []
  [:div#cells.applet
   [title "Cells" "📒"]
   [:div#cells-toolbar.row.wrapper
    [:span.toolbar-label "f(x)"]
    [:input#formula-input {:value @current-formula
                           :on-change #()}]
    [:div#cells-info.info "Work in progress but formula parsing working fully: start with '=' to enter a formula. Other than the standard mathematical operators, SUM, AVG, and ROUND are the available functions and can be used with cell ranges like A1:C3"]]
   [:div#spreadsheet.wrapper (util/keyboard-navigation)
    (doall (for [row (range 0 max-rows)]
             [:div.row.wrapper {:key (str "row" row)}
              [:span.row-label row]
              (doall (for [col (range 1 max-cols)]
                       (if (= row 0)
                         [:span.col-label {:key (str "col-label-" (char (+ col 64)))} (char (+ col 64))]
                         [:input {:default-value (util/recursive-deref (:value (@cells-map (util/cell-ref row col))))
                                  :read-only true
                                  :key (str
                                        (util/cell-ref row col) "-"
                                        (util/recursive-deref (:value (@cells-map (util/cell-ref row col)))))
                                  :data-row row :data-col col :id (util/cell-ref row col)
                                  :on-change
                                  #(util/changed! (.-target %1))
                                  :on-click
                                  #(update-selection! (.-target %1))
                                  :on-double-click
                                  (fn [e]
                                    (update-selection! (.-target e) true)
                                    (set! (-> e .-target .-readOnly) false))
                                  :on-blur
                                  #(util/handle-cell-blur (.-target %1) parser/parse-formula)}])))]))]])