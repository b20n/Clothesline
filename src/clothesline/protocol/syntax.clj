(ns clothesline.protocol.syntax
  (:use [clojure.contrib.macro-utils]
        [clothesline.protocol [response-helpers]])
  (:require [clothesline.protocol [test-helpers :as test-helpers]]))

(defmacro protocol-machine [& forms]
  (let [stateforms (filter #(= (str (first %)) "defstate") forms)
        nameholders (->> stateforms (map second) (map (fn [x] `(def ~x identity))))]
    `(do ~@nameholders ~@forms)))

(defmacro defstate [name & forms]
  (let [docstring (reduce str (interpose "\n" (take-while string? forms)))
        mname     (with-meta name {:doc docstring :name name})
        rforms    (list* :name (str name) (drop-while string? forms))]
    `(def ~mname (state ~@rforms))))

(def state-standards
     {:haltable true
      :test (fn [& _] false)
      :no (stop-response 500)
      :yes (stop-response 500)
      })


(defn update-data [{:keys [headers
                           annotate
                           body]} graphdata]
  (-> graphdata
      (update-in [:headers] #(merge % (or headers {})))
      (merge (dissoc annotate :headers))))

(declare gen-test-forms gen-body-forms)
(def *debug-mode-runone* false)   ; If set to true, the state doesn't progress, but rather
                                  ;  stops immediately with a processing dump.

(defmacro state [& {:as state-opts}]
  (let [has-body? (:body state-opts)]
    (if-not has-body?
      (gen-test-forms state-opts)
      (gen-body-forms state-opts))))

(defn- gen-test-forms [state-opts]
  (let [opts (merge state-standards state-opts)]
    `(fn [& [ {request# :request
               handler# :handler
               graphdata# :graphdata :as args#}]]
       (let [test# ~(:test opts)
             test-result# (test# args#)
             [result# ndata#] (test-helpers/result-and-graphdata
                               test-result#
                               graphdata#)
             plan#   (if result#
                         ~(:yes opts)
                         ~(:no opts))
             forward-args# (assoc args# :graphdata ndata#)]
           (println "Intermediate (" ~(:name opts) ")" test-result#)
           (println "  :: " forward-args#)
           (cond
            *debug-mode-runone*
                           forward-args#
            (map? plan#)
                           plan# ; If it's a map, return it.
            (instance? java.util.concurrent.Callable plan#)
                           (apply plan#
                                  (list forward-args#)) ; If it's invokable, invoke it.
            :default plan#)))))



(defn gen-body-forms [state-opts] (:body state-opts))


