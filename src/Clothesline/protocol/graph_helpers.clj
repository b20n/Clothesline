(ns clothesline.protocol.graph-helpers
  (:use (clothesline.protocol.response-helpers)))

;; Syntax helpers

(defmacro request-header-exists?
  [header-name]
  `(fn [{{headers# :headers} :request}]
    (if (get headers# ~header-name)
      true
      false)))

(defmacro graphdata-item-exists
  [kwd-name]
  `(fn [{graphdata# :graphdata}]
     (contains? graphdata# ~kwd-name)))

(defmacro request-header-is? [header-name value-str]
  `(fn [{{headers# :headers} :request}]
     (let [hv# (get headers# ~header-name)]
       (= hv# ~value-str))))

(defmacro is-request-method? [req-symbol]
  `(fn [{{request-method# :request-method} :request}]
     (= ~req-symbol request-method#)))

(defmacro call-on-handler [protocol-method]
  `(fn [{handler# :handler
         request# :request
         data#    :graphdata}]
     (apply ~protocol-method (list handler# request# data#))))






