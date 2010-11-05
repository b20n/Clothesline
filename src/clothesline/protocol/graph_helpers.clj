(ns clothesline.protocol.graph-helpers
  (:require [clojure.contrib [string :as strs]]
            [clothesline.protocol [response-helpers :as helper]]))

;; Construct a valid return value



;; Syntax helpers

(defmacro request-header-exists?
  [header-name]
  `(fn [{{headers# :headers} :request}]
     (contains? headers# ~header-name)))

(defmacro response-header-set?
  [header-name]
  `(fn [{{headers# :headers} :graphdata}]
     (contains? headers# ~header-name)))

(defmacro graphdata-item-exists?
  [kwd-name]
  `(fn [{graphdata# :graphdata}]
     (contains? graphdata# ~kwd-name)))

(defmacro request-header-is? [header-name value-str]
  `(fn [{{headers# :headers} :request}]
     (let [hv# (get headers# ~header-name)]
       (= hv# ~value-str))))

(defmacro request-method-is? [req-symbol]
  `(fn [{{request-method# :request-method} :request}]
     (= ~req-symbol request-method#)))

(defmacro call-on-handler [protocol-method]
  `(fn [{handler# :handler
         request# :request
         data#    :graphdata}]
     (apply ~protocol-method (list handler# request# data#))))

(defmacro normal-response [^int code]
  `(partial helper/generate-normal-response ~code))


