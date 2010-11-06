(ns clothesline.protocol.test-helpers
  (:require [clojure.contrib [string :as strs]])
  (:import  clothesline.interop.nodetest.TestResult)
  (:use     [clothesline [util :only [get-with-key]]]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Response Handler Helpers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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
         (when with-default? (first map))))
  ([request field map] (map-accept-header request field map true)))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Test return value
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn annotated-return
  ([result] (TestResult. result nil))
  ([result annotations] (TestResult. result annotations)))

(defn getres [v]
  (if (instance? TestResult v)
    (:result v)
    v))

(defn getann [v]
  (if (instance? TestResult v)
    (:annotations v)
    {}))

(defn getresann [v]
  (if (instance? TestResult v)
    ((juxt :result :annotations) v)
    [v nil]))



(defn merge-annotations
  ([ann1] ann1)
  ([ann1 ann2]
     (let [f (juxt :annotate :headers)
           [a1 h1] (f ann1)
           [a2 h2] (f ann2)]
       {:annotate (merge a1 a2) :headers (merge h1 h2)}))
  ([ann1 ann2 ann3 & more]
     (apply merge-annotations (list* (-> ann1
                                         (merge-annotations ann2)
                                         (merge-annotations ann3))
                                     more))))

(defn update-graphdata-with-anns [graphdata {:keys [annotate headers]}]
  (-> (or graphdata {}) 
      (update-in [:headers] #(merge % (or headers {})))
      (merge (dissoc annotate :headers))))

(defmulti result-and-graphdata
  "A function to handle annotated vs regular returns. If the result of a
   test is a clothesline.protocol.test-helpers.TestResult, its annotations
   will be merged into the graphdata properly.

   Returns a vector, [result, graphdata]"
  (fn [& args] (class (first args))))

(defmethod result-and-graphdata TestResult
  [{result :result annotations :annotations} graphdata]
  [result (update-graphdata-with-anns graphdata annotations)])

(defmethod result-and-graphdata :default
  [result graphdata]
  [result graphdata])


