(ns clojurescript7.components.circles
  (:require
   [reagent.core :as r]
   [clojurescript7.components.title :refer [title]]
   [clojurescript7.helper.dom :as dom :refer [$]]))

(def ^:const default-radius 50)

;; Stores circle creation and radius adjustment history; used by render function and for undo/redo functionality.
(def actions (r/atom []))
;; The current position in actions vector; incremented by circle drawing or r adjustment actions, dec/inc by undo/redo.
(def current-idx (r/atom 0))
;; Diameter slider value that is used for radius adjustment in currently selected circle.
(def diameter-slider-val (r/atom default-radius))
;; Stores slider label text; changes to diameter value for current circle.
(def circle-slider-info (r/atom "Draw then select a circle to adjust the diameter"))
(def selection-last-r (r/atom default-radius)) ; the previous r of the currently selected circle

;; Returns true if the radius of the current selection has actually changed.
(defn selection-changed? []
  (let [current-selected ($ ".selected-circle")]
    (if current-selected
      (not= (.getAttribute current-selected "r") @selection-last-r)
      false)))

;; Gets the id of the currently selected circle element.
(defn selection-id []
  (let [current-selected ($ ".selected-circle")]
    (if current-selected
      (.-id current-selected)
      "")))

;; Update the actions vector 
(defn actions-update! [actions-v insert-at-idx action]
  (reset! actions-v (subvec @actions-v 0 @insert-at-idx))
  (swap! insert-at-idx inc)
  (swap! actions-v conj action))

;; Commits a radius adjustment action to the actions vector when necessary
(defn adjust-action-commit! []
  (let [current-selected ($ ".selected-circle")]
    (when current-selected
      (when (selection-changed?)
        (actions-update!
         actions
         current-idx
         {:action :adjust :id (.-id current-selected)
          :idx (js/parseInt (.getAttribute current-selected "idx")) :r (/ @diameter-slider-val 2) :previous-r @selection-last-r}))
      (dom/remove-class current-selected "selected-circle"))))

;; Sets the selection to to the specified element.
(defn current-selection-set! [el] ; el must be a DOM element
  (adjust-action-commit!)
  (dom/add-class-name el "selected-circle")
  (dom/input-enable ($ "#circle-slider"))
  (reset! selection-last-r (.getAttribute el "r"))
  (reset! diameter-slider-val (* 2 (js/parseInt (.getAttribute el "r"))))
  (reset! circle-slider-info (str "Diameter: " @diameter-slider-val "px")))

;; Selects the most recently drawn circle.
(defn select-last! []
  (let [circles (dom/$$ "#circles-canvas circle")]
    (when (> (count circles) 0)
      ;; Calls current-selection-set! with the last circle element found
      (current-selection-set! (last circles)))))

;; Attaches event handler functions to the circle attributes map passed in.
(defn circle-ev-handlers [attributes]
  (merge
   attributes
   {:on-click ; used to be attaching more handlers but moved everything to CSS
    (fn [e]
      (current-selection-set! (.-target e)))}))

(defn radius-adjust!
  ([new-r] (.setAttribute ($ ".selected-circle") "r" new-r))
  ([new-r circle-el]
   (.setAttribute circle-el "r" new-r)))

(defn toggle-buttons [actions-v curr-idx]
  (cond ; test val of current index to set state of undo & redo buttons
    (= curr-idx 0) (dom/input-disable ($ "#undo-button") true)
    (> curr-idx 0) (dom/input-enable ($ "#undo-button") true))
  (cond
    (< curr-idx (count actions-v)) (dom/input-enable ($ "#redo-button") true)
    (= curr-idx (count actions-v)) (dom/input-disable ($ "#redo-button") true)))


;; Loops through actions vector and adds circles to an svg group.
(defn render-shapes [actions-v curr-idx]
  (r/after-render #(toggle-buttons actions-v curr-idx))
  (into [:g]
        (map-indexed
         (fn [idx action]
           (when (< idx curr-idx)
             (when (= (:action action) :circle)
               [:circle (circle-ev-handlers (merge action {:r default-radius :stroke "black" :stroke-width 2 :fill "transparent"}))])))
         actions-v)))

;; Loops through actions vector and resizes circles when adjustment actions are found.
(defn adjust-shapes [actions-v curr-idx]
  (map-indexed
   (fn [idx action]
     (when (< idx curr-idx)
       (when (not= (:id action) (selection-id)) ; skip over the currently selected shape that is possibly being modified
         ;; First re-adjust r for all circles to default, then make
         ;; any adjustments. This step is necessary because undo/redo could leave r's
         ;; inconsistently adjusted depending on adjustment history.
         (when (= (:action action) :circle)
           (r/after-render #(radius-adjust! default-radius (dom/el-by-id (:id action)))))
         (when (= (:action action) :adjust)
           (r/after-render #(radius-adjust! (:r action) (dom/el-by-id (:id action))))))))
   actions-v))

(defn circles []
  [:div#circles.applet
   [title "Circle Drawer" "🖍️"]
   [:div#undo-row.row.wrapper
    [:button#undo-button.disabled
     {:type "button"
      :on-click (fn [] (swap! current-idx dec))}
     "Undo"]
    [:button#redo-button.disabled
     {:type "button"
      :on-click (fn [] (swap! current-idx inc))}
     "Redo"]
    [:div#circle-slider-wrapper
     [:span#circle-slider-info.info @circle-slider-info]
     [:input#circle-slider.slider
      {:type "range" :min 10 :max 300 :value @diameter-slider-val :disabled true
       :on-change
       (fn [e]
         (reset! diameter-slider-val (-> e .-target .-value))
         (reset! circle-slider-info (str "Diameter: " @diameter-slider-val "px"))
         (radius-adjust! (/ @diameter-slider-val 2) ($ ".selected-circle")))}]]]
   [:svg#circles-canvas
    {:stroke "black"
     :stroke-width 2
     :on-context-menu (dom/ctx-menu-disable)
     :on-mouse-down (fn [e]
                      (let [mouse-info (dom/mouse-info e ($ "#circles-canvas"))]
                        (when (:left? mouse-info)
                          ;; First commit the current :adjust action when necessary, then add new circle
                          (adjust-action-commit!)
                          (actions-update! actions current-idx
                                           {:action :circle :id (str "circle-" @current-idx) :idx @current-idx :cx (:x mouse-info) :cy (:y mouse-info)})
                          (r/after-render select-last!))
                        (when (:right? mouse-info)
                          (js/console.log "right click"))))}
    (render-shapes @actions @current-idx) ; first pass on actions vector adds circles to DOM
    (adjust-shapes @actions @current-idx) ]]); second pass on actions vector to implement any adjustments
