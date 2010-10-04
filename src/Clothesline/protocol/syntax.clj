(ns clothesline.protocol.syntax
  (:use [clojure.contrib.macro-utils]
        [clothesline.protocol [response-helpers]]))

(defmacro protocol-machine [version-handle & forms]
  (let [stateforms (filter #(= (str (first %)) "defstate") forms)
        names      (map #(second %) stateforms)]
    `(do (declare ~@names) ~@forms)))

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

(defn update-data [{:keys [add-data]} data]
  (merge data add-data))

(defn update-response [{:keys [add-headers
                               remove-headers
                               headers
                               body
                               status] :as map} response]
  (-> response
      (assoc :headers (or headers (:headers response)))
      (update-in [:headers] #(merge % (or add-headers {})))
      (update-in [:headers] #(apply dissoc (list* % (or remove-headers []))))
      (assoc :body (or (:body response) body))
      (assoc :status (or (:status response) status))))

(declare gen-test-forms gen-body-forms)

(defmacro state [& {:as state-opts}]
  (let [has-body? (:body state-opts)]
    (if-not has-body?
      (gen-test-forms state-opts)
      (gen-body-forms state-opts))))

(defn- gen-test-forms [state-opts]
  (let [opts (resolve-states (merge state-standards state-opts))]
    `(fn [& [ {request# :request
               handler# :handler
               response# :response
               graphdata# :graphdata :as args#}]]
         (let [test# ~(:test opts)
               test-result# (test# handler# request# graphdata#)
               result# (or (:result test-result#)
                           test-result#)
               plan#   (if result#
                         ~(:yes opts)
                         ~(:no opts))
               nresponse# (if (map? test-result#)
                            (update-response test-result# response#)
                            response#)
               ndata#     (if (map? test-result#)
                            (update-data graphdata# test-result#)
                            graphdata#)
               forward-args# {:request request#
                              :handler handler#
                              :response nresponse#
                              :graphdata ndata#}]
           (println "Intermediate: " test-result# " -> " result#) ; For debugging
           (cond
            (map? plan#)   plan# ; If it's a map, return it.
            (or (instance? clojure.lang.IFn plan#) (var? plan#))
                (apply plan# (list forward-args#)) ; If it's invokable, invoke it.
            (keyword? plan#) (list result# plan#) ; For debugging.
            :default plan#) ; For futureproofing?
           ))))

(defn gen-body-forms [state-opts] (:body state-opts))


