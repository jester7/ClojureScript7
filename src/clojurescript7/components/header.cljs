(ns clojurescript7.components.header)

(defn header []
  [:header.applet
   [:h2 {:class "title" :id "site-title"} "ClojureScript" [:span.number "7"]]
   [:span {:class "subheading fixed-width-font bold" :id "site-subheading"} "An implementation of the 7GUIs in ClojureScript & Reagent"]
   [:ul.applet-menu
    [:li.menu-item [:a {:href "#counter"} "🧮"]]
    [:li.menu-item [:a {:href "#temperature"} "🌡"]]
    [:li.menu-item [:a {:href "#flight"} "✈️"]]
    [:li.menu-item [:a {:href "#timer"} "⏲"]]
    [:li.menu-item [:a {:href "#crud"} "🗄"]]
    [:li.menu-item [:a {:href "#circles"} "🖍"]]
    [:li.menu-item [:a {:href "#cells"} "📒"]]]])