(ns clothesline.protocol.syntax
  (:use [clojure.contrib.macro-utils]
        [clothesline.protocol [response-helpers]]))

(defmacro defstate [name & forms]
  `(def ~name (state ~@forms)))

(def state-standards
     {:haltable true
      :test (fn [_ _] false)
      :fail (stop-response 500)
      })

(def *graph-namespace* 'clothesline.protocol.graph)
(defn- key->sym [keyword] (symbol (name keyword)))

(defn- resolve-states [map]
  (update-in map [:pass :fail]
             (fn [v] (if (keyword? v)
                       (ns-resolve *graph-namespace* (key->sym v))
                       v))))

(defmacro state [& forms]
  (let [state-opts (apply hash-map forms)
        opts (resolve-states (merge state-standards state-opts))
        has-body? (:body opts)]
    (if-not has-body?
      `(fn [[handler# request# :as args#]]
         (let [test# ~(:test opts)]
           (let [result# (test# handler# request#)
                 plan#   (if result#
                           ~(:success opts)
                           ~(:fail opts))]
             (println "Intermediate: " result# " " plan#)
             (cond
              (var? plan#) (apply plan# args#)
              (map? result#) result#    ;TODO: Consider if this is a bad idea or not.
              (map? plan#)   plan#
              (instance? clojure.lang.IFn plan#) (apply plan# args#)
              :default plan#)
             )))
      (throw (Exception. "Not ready yet!")))))



