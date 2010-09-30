(ns clothesline.protocol.graph
  (:use [clothesline.protocol [response-helpers]
         [syntax]])
  (:require [clothesline [service :as handler]]))

;; Proposed syntax

(defstate b13
  :haltable true
  :test (fn [handler request]
          (handler/service-available? handler request))
  :success :b12
  :fail (stop-response 503))

