(ns clojurescript7.components.cells.parser
  (:require
   [clojurescript7.helper.math :as m]
   [reagent.core :as r]
   [clojure.string :as s]
   [clojurescript7.components.cells.utility :as util :refer
    [cells-map cell-value-for row-col-for-cell-ref]]))

;; -------------- refactored, moved code below from clojurescript7.helper.arithmetic-parser to this namespace

;; TODO handle RANGES of cells (example A1:A10 or B2:C5) by expanding them into individual tokens
;; -- first pass, tokenize ranges by expanding to individual cell refs 
;; -- then: replace single range token with expanded tokens for each cell in range, A1, A2, A3, etc
;; -- finally: eval cell references by looking up value
;; TODO handle vararg functions, so that above is possible

(def  tokenize-re #"[[A-Z]{1,2}[1-9]{0,4}[\:][A-Z]{1,2}[1-9]{0,4}]*|[[0-9]?\.?[0-9]+]*|[\/*\-+^\(\)]|[[A-Z]{1,2}[1-9]{1,4}]*")

; dead end: was going to try handling unary minus with a regex
; but there is some kind of regex bug
; abandoned and wrote the function swap-unary-minus instead
(def unary-minus-re #"^(-)[0-9a-zA-Z]*[\(]?") ; can't use this regex
; bug in cljs? https://ask.clojure.org/index.php/8329/caret-character-differences-with-re-seq-in-cljs-vs-clj

(def test-expr-1 "-3 + A3 / Z99 * Y1 + -.000923")
(def test-expr-2 "1.2 + 3.141 / 99 * 2")
(def test-expr-3 "(1.2 + 3.141) / 99 * 2")
(def test-expr-4 "1+3/9*2")
(def test-unary "-(-3 - -2)")
(def test-unary-exp "-3^-2")

(def ^:const left-p "(")
(def ^:const right-p ")")
(def ^:const exp "^")
(def ^:const minus "-")

(def ^:const operators {"^" {:fn Math/pow :precedence 3}
                        "*" {:fn * :precedence 2}
                        "/" {:fn / :precedence 2}
                        "+" {:fn + :precedence 1}
                        "-" {:fn - :precedence 1}}) ; TODO handle functions (sum, average, etc)

(defn operator? [token-str]
  (not (nil? (operators token-str))))

(defn operand? [token-str]
  (and (not= left-p token-str) (and (not= right-p token-str) (nil? (operators token-str)))))

(defn cell-range? [token]
  (if (string? token)
    (not (nil? (re-seq #"^[A-Z]{1,2}[1-9]{0,4}[\:][A-Z]{1,2}[1-9]{0,4}$" token)))
    false))

(defn expand-cell-range [range-str]
  (cond
    (cell-range? range-str)
    (let [matches (re-matches #"^([A-Z]{1,2}[1-9]{0,4})[\:]([A-Z]{1,2}[1-9]{0,4})$" range-str)
          start-cell (matches 1) end-cell (matches 2)
          start (row-col-for-cell-ref start-cell)
          end (row-col-for-cell-ref end-cell)]
      ;(prn start end)
      (first (for [col (range (.charCodeAt (:col start)) (inc (.charCodeAt (:col end))))]
               (for [row (range (:row start) (inc (:row end)))]
                 (str (char col) row)))))
    :else
    nil))

(defn strip-whitespace [input-str] ; discards whitespace, used before tokenizing
  (s/replace input-str #"\s" ""))

;;; Turns an algebraic expression string into a sequence of strings with individual tokens 
(defn tokenize-as-str [expression-str]
  (re-seq tokenize-re (s/upper-case (strip-whitespace expression-str))))

(defn sublist [l start end] ; orphaned function, no longer used: TODO move out of here or delete
  (drop start (drop-last (- (dec (count l)) end) l)))

;;; Scans tokens for minus signs and determines if the minus sign should
;;; be treated as a subtraction operator or a unary negation operator.
;;; If the latter it replaces the token with multiplication by -1 and surrounds
;;; with parentheses. This effectively makes unary minus the highest priority
;;; operator (same as Excel, Numbers and Google Sheets).
(defn swap-unary-minus [infix-tokens]
  (loop [original-tokens (seq infix-tokens)
         prev-token nil ; nil at start of loop
         prev-token-unary? false ; false at start of loop
         new-tokens []] ; as tokens are processed they are added to vector
    (if original-tokens
      (let [token (first original-tokens)
            replace?
            (if (= minus token)
              (cond
                (nil? prev-token) true ; if minus is at beginning, is unary minus
                (operator? prev-token) true ; if previous token is an operator, assume unary -
                (= left-p prev-token) true ; if prev token is left (, must be unary -
                :else false) ; if current token is minus but none of above conditions match, it can't be a unary operator
              false)] ; if it isn't a minus sign, don't replace
        (recur (next original-tokens) token replace?
               (if replace?
                 (conj new-tokens "(" "-1" "*") ; if above conditions set the replace flag, add -1 multiplication
                 (if prev-token-unary? ; else check unary flag for previous token
                   (conj new-tokens token ")") ; if the previous token was unary -, conj token and closing parenthesis
                   (conj new-tokens token))))) ; else just add the token and nothing extra
      (seq new-tokens))))

;;; Looks up the precedence value from the operators map. Returns 0 if not found.
(defn precedence [v]
  (or (:precedence (operators v)) 0))

(defn cell-ref? [val]
  (cond
    (= "" val) false
    (number? val) false
    :else
    (not (nil? (re-seq #"^[A-Z]{1,2}[0-9]{1,4}$" val)))))


;;; Returns the number value from a numeric string but it doesn't do
;;; error checking so the return value could be NaN.
(defn eval-number [val]
  (cond
    (number? val) val
    (nil? val) 0
    :else
    (if (re-seq #"\." val) (js/parseFloat val) (js/parseInt val))))

(defn eval-cell-ref
  ([cell-ref] (eval-cell-ref cell-ref true))
  ([cell-ref when-not-cell-ref-return-nil?]
   (if (cell-ref? cell-ref)
     (r/track! #(let [cell-data (cell-value-for cell-ref)]
                  (if (m/numeric? cell-data) (eval-number cell-data) cell-data)))
     (if when-not-cell-ref-return-nil? nil cell-ref))))

;;; Takes a token in string format and returns the corresponding function (if an operator)
;;; or the text in the cell (nil if empty) or the numeric value.
(defn eval-token [token current-cell]
  (cond
    ; If it's an operator, return the function
    (:fn (operators token)) ; (operators token) returns nil if not found
    (:fn (operators token))

    ; If cell ref, evaluate and return 
    (cell-ref? token)
    (util/recursive-deref (eval-cell-ref token))
    ;;(let ;; watchers work in progress...
    ;;[cell-ref-val (eval-cell-ref token)] 
    ;;  (add-watch cell-ref-val (keyword (str token "-" current-cell))
    ;;             (fn [key atom previous updated]
    ;;               (prn (str "watch fn called for cell " token))
    ;;               (swap! cells-map update-in [current-cell :update-count] inc)))
      ;(swap! watchers assoc token (conj (@watchers token) current-cell)) 
      ;;(util/recursive-deref cell-ref-val))

    ; must be a number then
    :else
    (eval-number token)))

;;; Needed to handle parentheses swapping for expression reversal
(defn swap-parentheses [expression] ; turns "(" into ")" and vice versa
  (let [new-expression (atom [])]
    (dotimes [i (count expression)]
      (let [token (nth expression i)]
        (cond
          (= token left-p) (swap! new-expression assoc i right-p)
          (= token right-p) (swap! new-expression assoc i left-p)
          :else (swap! new-expression assoc i token))))
    (into () @new-expression))) ; reverses when going from vector to list


;;; Pops the operator stack while the predicate function evaluates to true and
;;; pushes the result to the output/operand stack. Used by infix-expression-eval
(defn pop-stack-while! [predicate op-stack out-stack current-cell]
  (while (predicate)
    (reset! out-stack
            (conj
             (pop (pop @out-stack))
             ((eval-token (peek @op-stack) current-cell)
              (or (eval-token  (first @out-stack) current-cell) 0) ; or is used to treat empty cells as 0
              (or (eval-token (nth @out-stack 1) current-cell) 0))))
    (swap! op-stack pop)))

;;; Parses any infix algebraic expression string into individual tokens and
;;; evaluates the expression.
;;; TODO catch exceptions and return error msg or throw exception
(defn infix-expression-eval [infix-expression current-cell] ; converts infix to prefix and evals, returns numeric result
  (let [reversed-expr (swap-parentheses (swap-unary-minus (tokenize-as-str infix-expression)))
        op-stack (atom ())
        out-stack (atom ())]
    (dotimes [i (count reversed-expr)]
      (let [token (nth reversed-expr i)]
        (cond
          ; if operand, adds it to the operand stack
          (operand? token) (swap! out-stack conj token)

          ; left parenthesis 
          (= left-p token) (swap! op-stack conj token)

          ; right parenthesis
          (= right-p token)
          (do
            (pop-stack-while!
             #(not= left-p (peek @op-stack)) op-stack out-stack current-cell)
            (swap! op-stack pop))

          ; if token is an operator and is the first one found in this expression
          (and (operator? token) (empty? @op-stack))
          (swap! op-stack conj token)

          ; handles all other operators when not the first one
          (operator? token)
          (do
            (pop-stack-while!
             #(or (< (precedence token) (precedence (peek @op-stack)))
                  (and (<= (precedence token) (precedence (peek @op-stack)))
                       (= exp token)))
             op-stack out-stack current-cell)
            (swap! op-stack conj token)))))
    ;; Once all tokens have been processed, pop and eval the stacks while op stack is not empty.
    (pop-stack-while! #(seq @op-stack) op-stack out-stack current-cell)
    ;; Assuming the expression was a valid one, the last item is the final result.
    (eval-token (peek @out-stack) false))) ; handle edge case where formula is a single cell reference


(defn parse-formula [formula-str current-cell]
  (r/track! #(infix-expression-eval formula-str current-cell)))
