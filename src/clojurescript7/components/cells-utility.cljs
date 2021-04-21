(ns clojurescript7.components.cells.utility
  (:require
   [reagent.core :as r]
   [clojurescript7.helper.dom :as dom :refer [$]]))

(def ^:const cells-parent-id "spreadsheet")
(def ^:const max-cols 27)
(def ^:const max-rows 101)

(def cells-map (r/atom {}))
(def current-selection (r/atom ""))
(def current-formula (r/atom ""))

(defn is-formula? [str]
  (= (get str 0) "="))

(defn cell-ref
  ([cell] (cell-ref (:row cell) (:col cell))) ; if single arg, assumes map with :row and :col
  ([row col]
   (str (char (+ col 64)) row)))

(defn el-by-cell-ref [cell-ref]
  ($ (str "#" cell-ref)))


(defn has-formula? [cell-ref]
  (let [data (@cells-map cell-ref)
        val (-> (el-by-cell-ref cell-ref) .-value)]
    (or (not (nil? (:formula data)))
        (is-formula? val))))

(defn scroll-to-cell ; TODO needs work, check offsets of all parent els, scrolling not quite right
  ([cell-ref] (scroll-to-cell cell-ref false true)) ; default just scroll, no range check, smooth yes
  ([cell-ref check-if-out-of-range?] (scroll-to-cell cell-ref check-if-out-of-range? true))
  ([cell-ref check-if-out-of-range? smooth-scroll?]
   (let [parent-el ($ (str "#" cells-parent-id))
         child-el (el-by-cell-ref cell-ref)
         child-offset-l (-> child-el .-offsetLeft)
         child-offset-t (-> child-el .-offsetTop)
         parent-offset-l (-> parent-el .-offsetLeft)
         parent-offset-t (-> parent-el .-offsetTop)
         smoothness (if smooth-scroll? "smooth" "auto")
         scroll-to-info {:left (- child-offset-l parent-offset-l)
                         :top (- child-offset-t parent-offset-t)
                         :behavior smoothness}]
     (if check-if-out-of-range?
       (when (or
              (> (- child-offset-l parent-offset-l) (.-clientWidth parent-el))
              (> (- child-offset-t parent-offset-t) (.-clientHeight parent-el)))
         (.scrollTo parent-el (clj->js scroll-to-info)))
       (.scrollTo parent-el (clj->js scroll-to-info))))))

(defn selection-cell-ref []
  ($ (str "#" cells-parent-id " input.selected")))

(defn row-col-for-el [el]
  {:row (js/parseInt (-> el .-dataset .-row))
   :col (js/parseInt (-> el .-dataset .-col))})

(defn row-col-for-cell-ref [cell-ref]
  (let [matches (re-matches #"^([A-Z]{1,2})([1-9]{0,4})" cell-ref)]
    {:row (js/parseInt (matches 2)) :col (matches 1)}))

(defn col-labels [] ; TODO / not being used yet, move col labels out of main spreadsheet div and sync scrolling 
  (for [col (range 1 max-cols)]
    [:span.col-label {:key (str "col-label-" (char (+ col 64)))} (char (+ col 64))]))

(defn cell-ref-for-input [input-el]
  (cell-ref (js/parseInt (-> input-el .-dataset .-row)) (js/parseInt (-> input-el .-dataset .-col))))

(defn cell-data-for
  ([cell-ref] (@cells-map cell-ref))
  ([row col] (@cells-map (cell-ref row col))))

(defn update-selection!
  ([el] (update-selection! el false))
  ([el get-formula?]
   (when (not= @current-selection "") (dom/remove-class ($ (str "#" @current-selection)) "selected"))
   (dom/add-class-name el "selected")
   (reset! current-selection (cell-ref-for-input el))
   (.focus el)
   (let [rc (row-col-for-el el)
         data (cell-data-for (:row rc) (:col rc))
         formula (:formula data) ;(or (:formula data) (:value data))
         value (or (:value data) data)]
     (js/console.log (cell-ref rc))
     (js/console.log "data: " data)
     (js/console.log "formula: " formula)
     (js/console.log "value: " value)
     (js/console.log "===========================")
     (if formula
       (reset! current-formula formula)
       (reset! current-formula value))
     (when get-formula?
       (set! (-> el .-value) formula)))))

(defn keyboard-navigation [] ; TODO getting too long, split into more functions
  {:on-key-down
   (fn [e]
     (let [curr-cell (selection-cell-ref)]
       (case (.-key e)
         "ArrowUp" (doall (let [rc (row-col-for-el curr-cell)
                                rc-new {:row (dec (:row rc)) :col (:col rc)}]
                            (when (> (:row rc) 1)
                              (scroll-to-cell (cell-ref rc-new) true)
                              (update-selection! ($ (str "#" (cell-ref (:row rc-new) (:col rc-new))))))))
         "ArrowDown" (doall (let [rc (row-col-for-el curr-cell)
                                  rc-new {:row (inc (:row rc)) :col (:col rc)}]
                              (when (< (:row rc) (dec max-rows))
                                (scroll-to-cell (cell-ref rc-new) true)
                                (update-selection! ($ (str "#" (cell-ref (:row rc-new) (:col rc-new))))))))
         "ArrowLeft" (doall (let [rc (row-col-for-el curr-cell)
                                  rc-new {:row (:row rc) :col (dec (:col rc))}]
                              (when (> (:col rc) 1)
                                (scroll-to-cell (cell-ref rc-new) true)
                                (update-selection! ($ (str "#" (cell-ref (:row rc-new) (:col rc-new))))))))
         "ArrowRight" (doall (let [rc (row-col-for-el curr-cell)
                                   rc-new {:row (:row rc) :col (inc (:col rc))}]
                               (when (< (:col rc) (dec max-cols))
                                 (scroll-to-cell (cell-ref rc-new) true)
                                 (update-selection! ($ (str "#" (cell-ref (:row rc-new) (:col rc-new))))))))
         "Enter" (doall (set! (.-readOnly curr-cell) false)
                        (.focus curr-cell))
         "Escape" (js/console.log "escape")
         (set! (-> e .-target .-readOnly) false)
                 ;(set! (.-value (.-target e)) (.-key e))
                 ;(.focus curr-cell)
         )))})