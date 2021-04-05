(ns clojurescript7.components.flight
  (:require
   [reagent.core :as r]
   [clojurescript7.helper.dom :as dom :refer [$]]
   [clojurescript7.helper.dates :as dates]
   [clojurescript7.components.title :refer [title]]))

;; set default to one way flight
(def booking-type (r/atom "1"))
;; set default departure date to today
(def start-date (r/atom (dates/today)))
;; set default return day to same as start date
(def return-date (r/atom @start-date))
;; info message after valid flight booking
(def flight-info (r/atom "Enter your desired flight date(s)."))

; simple builder function for successful booking message
(defn flight-info-message []
  (if (= @booking-type "1")
    (str "You've booked a one-way flight on " @start-date)
    (str "You've booked a round trip flight on " @start-date
         " returning on " @return-date)))

(defn validate-flight-dates! [date-input-element]
  (when (= @booking-type "1")
    (if (and (dates/date-valid? @start-date) (dates/date>= @start-date (dates/today)))
            ; proper date: remove invalid class from input if present and enable the book button
      (do (dom/input-enable ($ "#book-button") true)
          (dom/remove-class ($ "#start-date") "invalid")
          (dom/remove-class ($ "#return-date") "invalid"))
            ; else - improper date; add invalid class to input, disabled to button
      (do (dom/input-disable ($ "#book-button") true)
          (dom/add-class-name date-input-element "invalid"))))
  (when (= @booking-type "2")
    (if (and (dates/date-valid? @start-date) (dates/date-valid? @return-date)
             (dates/date>= @start-date (dates/today))
             (dates/date<= @start-date @return-date))
      (do (dom/input-enable ($ "#book-button") true)
          (dom/remove-class ($ "#start-date") "invalid")
          (dom/remove-class ($ "#return-date") "invalid"))
            ; else - improper date; add invalid class to input, disabled to button
      (do (dom/input-disable ($ "#book-button") true)
          (dom/add-class-name date-input-element "invalid")))))

(defn flight []
  [:div#flight.applet
   [title "Flight Booker" "✈️"]
   [:div.row.wrapper
    [:div#flight-combo-box.row.wrapper
     [:select#booking-type
      {:value @booking-type
       :on-change
       (fn [e]
         (reset! booking-type (-> e .-target .-value)) ; first, reset atom for booking-type
         (when (= @booking-type "1") ; one way flight
           ; disable the return-date input
           (dom/input-disable ($ "#return-date") true)
           ; revalidate start-date input since pass/fail checks for booking type 2 are different to type 1
           (validate-flight-dates! ($ "#start-date")))
         (when (= @booking-type "2") ; round trip flights
           (dom/input-enable ($ "#return-date") true) ; enables return-date input if disabled
           ; revalidate date inputs after booking type change
           (validate-flight-dates! ($ "#start-date")) ;
           (validate-flight-dates! ($ "#return-date"))))}
      [:option {:value "1"} "One Way"]
      [:option {:value "2"} "Round Trip"]]]]
   [:div.row.wrapper
    [:input#start-date.date.enabled
     {:value @start-date
      :on-change
      (fn [e]
        (reset! start-date (-> e .-target .-value))
        (validate-flight-dates! (.-target e)))}]]
   [:div.row.wrapper
    [:input#return-date.date.disabled
     {:value @return-date :disabled true
      :on-change
      (fn [e]
        (reset! return-date (-> e .-target .-value))
        (validate-flight-dates! (.-target e)))}]]
   [:div#flight-info-row.row.wrapper
    [:span#flight-info.info @flight-info]]
   [:div.row.wrapper
    [:button#book-button
     {:type "button"
      :on-click (fn []
                  (reset! flight-info (flight-info-message)))}
     "Book It!"]]])