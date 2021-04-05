(ns clojurescript7.core
  (:require
   [reagent.dom :as d]
   [reagent.core :as r]
   [clojurescript7.helper.dom :as helper-dom :refer [$]]
   [clojurescript7.components.header :refer [header]]
   [clojurescript7.components.counter :refer [counter]]
   [clojurescript7.components.temperature :refer [temperature]]
   [clojurescript7.components.flight :refer [flight]]
   [clojurescript7.components.timer :refer [timer]]
   [clojurescript7.components.crud :refer [CRUD]]
   [clojurescript7.components.circles :refer [circles]]
   [clojurescript7.components.cells :refer [cells]]))

;; Views

(defn app []
  [:div.wrapper
   [header]
   [counter]
   [temperature]
   [flight]
   [timer]
   [CRUD]
   [circles]
   [cells]])

;; Initialize app

(defn mount []
  (d/render [app] ($ "#app")))

(defn init! []
  (mount))
