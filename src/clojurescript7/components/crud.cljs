(ns clojurescript7.components.crud
  (:require
   [reagent.core :as r]
   [clojurescript7.components.crud.data :as d :refer [all-the-names]]
   [clojurescript7.helper.dom :as dom :refer [$]]
   [clojurescript7.components.title :refer [title]]))


(def filter-text (r/atom ""))
(def selection (r/atom 0))
(def fname (r/atom (get (@all-the-names 0) :fname)))
(def surname (r/atom (get (@all-the-names 0) :surname)))
(def photo (r/atom (get (@all-the-names 0) :photo)))


(defn lookup! [value]
  (js/console.log value (get @all-the-names value))
  (let [person (get @all-the-names value)]
    (reset! fname (person :fname))
    (reset! surname (person :surname))
    (reset! photo (person :photo)))
  (dom/remove-class ($ "#update-button") "disabled")
  (dom/remove-class ($ "#delete-button") "disabled"))


(defn filter-list [text coll]
  (filter (fn [name]
            (let [match (re-find (re-pattern (str "(?i)^" text ".*")) (get name :surname))]
              (not (nil? match)))) (vals coll)))

(defn names-as-html []
  (map (fn [name]
         [:option {:value (get name :key) :key (get name :key)} (str (get name :surname) ", " (get name :fname))]) (filter-list @filter-text @all-the-names)))

(defn CRUD []
  [:div#crud.applet
   [title "CRUD" "🗄️"]
   [:div#crud-left
    [:div#filter-row.row.wrapper
     [:label.info {:for "filter-text"} "Filter prefix:"]
     [:input#filter-text
      {:type "text"
       :value @filter-text
       :on-change (fn [e]
                    (reset! filter-text (-> e .-target .-value))
                    (reset! selection (get (get (first (names-as-html)) 1) :value))
                    (lookup! @selection))}]]
    [:div#list-row.row.wrapper
     [:select#listbox
      {:size 6
       :value @selection
       :on-change
       (fn [e]
         (reset! selection (js/parseInt (-> e .-target .-value)))
         (lookup! @selection))}
      (names-as-html)]]]
   [:div#crud-right
    [:div.row.wrapper]
    [:div.row.wrapper
     [:img {:src @photo}]
     [:label.info {:for "crud-name"} "Name:"]
     [:input#crud-name {:type "text" :value @fname
                        :on-change (fn [e] (reset! fname (-> e .-target .-value)))}]]
    [:div.row.wrapper
     [:label.info {:for "crud-surname"} "Surname:"]
     [:input#crud-surname {:type "text" :value @surname
                           :on-change (fn [e] (reset! surname (-> e .-target .-value)))}]]]
   [:div.row.wrapper]
   [:div.row.wrapper
    [:button#create-button
     {:type "button"
      :on-click (fn []
                  (reset! all-the-names (conj @all-the-names {@d/next-key {:key @d/next-key :fname @fname :surname @surname}}))
                  (reset! selection @d/next-key)
                  (swap! d/next-key inc))}
     "Create"]
    [:button#update-button.disabled
     {:type "button"
      :on-click (fn []
                  (reset! all-the-names (assoc @all-the-names @selection {:key @selection :fname @fname :surname @surname})))}
     "Update"]
    [:button#delete-button.disabled
     {:type "button"
      :on-click (fn []
                  (when (not (nil? @selection))
                    (reset! all-the-names (dissoc @all-the-names @selection))
                    (reset! selection (get (get (first (names-as-html)) 1) :value))
                    (lookup! @selection)))}
     "Delete"]]])
