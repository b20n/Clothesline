(ns clothesline.util
  (:use [clj-time.format :only [parse formatter unparse]]))

(defn get-with-key [map key]
  (when-let [v (get map key)]
    [key v]))

(defn take-until [pred col]
  (take-while #(not (pred %)) col))

(defn assoc-if
  ([col key value test]
      (if test
        (assoc col key value)
        col))
  ([col key value] (assoc-if col key value value)))

(defn map-keys
  "Produces a new map where all keys have been transformed into f(k)."
  ([f col _] ; Preserving order is currently ignored
     (reduce (fn [v [key val]] (assoc v (f key) val))
             {}
             col))
  ([f col] (map-keys f col false)))

;; Date and time manipulation

(defn datetime-to-http11-string [datetime]
  (str  (unparse (formatter "E, d MMM Y H:m:s") datetime) " GMT"))

(defn- pre-and-parse-date [pre formatter string]
  "Expects pre to return a vector, [v, data]. Returns [(unparse formatter v), data].
   If nil is returned from pre, it is propagated. If unparse fails, also returns nil."
  (when-let [[v data :as whole] (pre string)]
    (try 
      [(parse formatter v) data]
      (catch IllegalArgumentException e
        nil))))

(defn- split-out-timezone [str]
  (when-let [matches (re-matches #"^(.*) ([A-Z]{3})$" str)]
    (rest matches)))

(defonce date-parsing-strategies
  (list
   [split-out-timezone (formatter "E, d MMM Y H:m:s")]  ; 1123
   [split-out-timezone (formatter "E, d-MMM-yy H:m:s")]  ; 1036
   [(juxt identity (constantly "UTC")) (formatter "E MMM  d H:m:s yyyy")])) ; Ansi C

(defn date-timezone-from-string
  "Attempts to extract a date from a string, using the HTTP 1.1 mandated
   formats in this order: RFC 822/1123, RFC 850/1036, Ansi CTime.
   (examples from the http 1.1 spec:
         Sun, 06 Nov 1994 08:49:37 GMT  ; RFC 822, updated by RFC 1123
      Sunday, 06-Nov-94 08:49:37 GMT ; RFC 850, obsoleted by RFC 1036
      Sun Nov  6 08:49:37 1994       ; ANSI C's asctime() format)"
  [string]
  (first (keep
          (fn [[pre fmt]]
            (pre-and-parse-date pre fmt string))
          date-parsing-strategies)))

