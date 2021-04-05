(ns clojurescript7.helper.arithmetic-parser
  (:require
   [clojure.string :as s]))

(def ^:const tokenizer #"([/*-+^])|([A-Z]{1,2}[0-9]{1,4})*")
(def  tokenizer2 #"[[-]?[0-9]?\.?[0-9]+]*|[/*-+^]|[[A-Z]{1,2}[1-9]{1,4}]*")
; (re-seq tokenizer2 "-3 + A3 / Z99 * Y1 + -.000923")
(def test-expr-1 "-3 + A3 / Z99 * Y1 + -.000923")
; (+ (+ -3 (* (/ A3 Z99) Y1)) -.000923)
(def ^:const operators {"*" {:fn * :precedence 2}
                        "/" {:fn / :precedence 2}
                        "+" {:fn + :precedence 1}
                        "-" {:fn - :precedence 1}})

(defn strip-whitespace [input-str]
  (s/replace input-str #"\s" ""))

(defn tokenize [input-str]
  (re-seq tokenizer2 (strip-whitespace input-str)))

(defn precedence [v]
  (or (:precedence (operators v)) 0))

