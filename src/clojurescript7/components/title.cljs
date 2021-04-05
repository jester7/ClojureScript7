(ns clojurescript7.components.title)

(defn title
  ([applet-title]
   [:h2.applet-title applet-title])
  ([applet-title emoji-icon]
   [:h2.applet-title applet-title
    [:span.icon emoji-icon]]))
;    [:span.icon [:a {:href "#counter"} emoji-icon]]]))
