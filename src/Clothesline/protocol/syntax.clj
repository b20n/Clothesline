(ns clothesline.protocol.syntax
  (:use [clojure.contrib.macro-utils]
        [clothesline.protocol [response-helpers]]))

(defmacro defstate [name & forms]
  (let [docstring (reduce str (interpose "\n" (take-while string? forms)))
        rforms    (drop-while string? forms)
        mname     (with-meta name {:doc docstring :name name
                                   })]
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
  
(defn update-response [{:keys [add-headers remove-headers headers body status] :as map}
                       response]
  (-> response
      (assoc :headers (or headers (:headers response)))
      (update-in [:headers] #(merge % (or add-headers {})))
      (update-in [:headers] #(apply dissoc (list* % (or remove-headers []))))
      (assoc :body (or (:body response) body))
      (assoc :status (or (:status response) status))))

(defmacro state [& forms]
  (let [state-opts (apply hash-map forms)
        opts (resolve-states (merge state-standards state-opts))
        has-body? (:body opts)]
    (if-not has-body?
      `(fn [& [ {request# :request
                 handler# :handler
                 response# :response :as args#}]]
         (let [test# ~(:test opts)
               test-result# (test# handler# request#)
               result# (or (:result test-result#)
                           test-result#)
               plan#   (if result#
                         ~(:yes opts)
                         ~(:no opts))
               nresponse# (if (map? test-result#)
                            (update-response test-result# response#)
                            response#)
               forward-args# {:request request#
                              :handler handler#
                              :response nresponse#}]
           (println "Intermediate: " test-result# " -> " result#) ; For debugging
           (cond
            (map? plan#)   plan#
            (instance? clojure.lang.IFn plan#) (apply plan# (list args#))
            (var? plan#) (apply plan# (list forward-args#))
            (keyword? plan#) (list result# plan#)

            :default plan#)
           ))
      (throw (Exception. "Not ready yet!")))))



