(ns clothesline.protocol.test-helpers
  (:require [clojure.contrib [string :as strs]])
  (:use     [clothesline [util :only [get-with-key]]]))


;; Response Handler Helpers

(defn hv [request header]
  ((:headers request) header))

(defn purge-str-semicolon [astring]
  (first (strs/split #";" astring)))

(defn split-header-field [request field]
  (let [headers (or (:headers request) {})
        field   (headers field)]
    (if field
      (map purge-str-semicolon (strs/split #"," field))
      (list))))

(defn push-header-through [request field map]
  (let [split-header (split-header-field request field)]
    (keep #(when-let [v (get map %)] [% v]) split-header)))

(defn map-accept-header
  ([request field map with-default?]
     (or (first (push-header-through request field map))
         (or  (get-with-key map "*/*") (get-with-key map "*"))
         (and with-default? (first map))))
  ([request field map] (map-accept-header request field map true)))

;; Test return value

(defrecord TestResult [result annotations])

(defn annotated-return
  ([result] (TestResult. result nil))
  ([result annotations] (TestResult. result annotations)))

(defmulti result-and-graphdata
  "A function to handle annotated vs regular returns. If the result of a
   test is a clothesline.protocol.test-helpers.TestResult, its annotations
   will be merged into the graphdata properly.

   Returns a vector, [result, graphdata]"
  (fn [& args] (class (first args))))

(defmethod result-and-graphdata TestResult
  [{result :result {:keys [annotate headers]} :annotations}
   graphdata]
  [result (-> graphdata
              (update-in [:headers] #(merge % (or headers {})))
              (merge (dissoc annotate :headers)))])

(defmethod result-and-graphdata :default
  [result graphdata]
  [result graphdata])


