(ns clojurescript7.prod
  (:require
    [clojurescript7.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
