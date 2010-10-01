(ns clothesline.protocol.syntax
  (:use [clojure.contrib.macro-utils]
        [clothesline.protocol [response-helpers]]))

(defmacro defstate [name & forms]
  (let [docstring (reduce str (interpose "\n" (take-while string? forms)))
        rforms    (drop-while string? forms)
        mname     (with-meta name {:doc docstring})]
    `(def ~mname (state ~@rforms))))

(def state-standards
     {:haltable true
      :test (fn [& _] false)
      :no (stop-response 500)
      :yes (stop-response 500)
      })

(def *graph-namespace* 'clothesline.protocol.graph)
(defn- key->sym [keyword] (symbol (name keyword)))
(defn- key->nsvar [ns keyword] (if (keyword? keyword)
                                 (or (ns-resolve ns (key->sym keyword)) keyword)
                                 keyword))


(defn resolve-states [map] 
  (assoc map :yes (key->nsvar *graph-namespace* (:yes map))
             :no  (key->nsvar *graph-namespace* (:no map))))
  

(defmacro state [& forms]
  (let [state-opts (apply hash-map forms)
        opts (resolve-states (merge state-standards state-opts))
        has-body? (:body opts)]
    (if-not has-body?
      `(fn [& [handler# request# :as args#]]
         (let [test# ~(:test opts)
               result# (test# handler# request#)
               plan#   (if result#
                         ~(:yes opts)
                         ~(:no opts))]
           (println "Intermediate: " result# " " plan#)
           (cond
            (var? plan#) (apply plan# args#)
            (map? result#) result# ;TODO: Consider if this is a bad idea or not.
            (map? plan#)   plan#
            (keyword? plan#) (list result# plan#)
            (instance? clojure.lang.IFn plan#) (apply plan# args#)
            :default plan#)
           ))
      (throw (Exception. "Not ready yet!")))))



