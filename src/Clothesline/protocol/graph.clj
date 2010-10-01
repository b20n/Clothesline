(ns clothesline.protocol.graph
  (:use [clothesline.protocol [response-helpers]
         [syntax]])
  (:require [clothesline [service :as s]]))

;; Proposed syntax

(defstate b13
  "Check if service has been made unavailable. Return status 503 if not."
  :test s/service-available?
  :yes :b12
  :no (stop-response 503))

(defstate b12
  :test (constantly true)
  :yes :b11
  :no (stop-response 501))

(defstate b11
  :test s/uri-too-long?
  :no :b10
  :yes (stop-response 414))

(defstate b10
  :test (fn [handler request] ((s/allowed-methods handler request)
                               (:request-method request)) )
  :yes :b11
  :no (stop-response 501))

(defstate b9
  :test s/malformed-request?
  :no :b8
  :yes (stop-response 400))

(defstate b8
  :test s/authorized?
  :yes :b7
  :no (stop-response 401))

(defstate b7
  :test s/forbidden?
  :yes (stop-response 403)
  :no  :b6)

(defstate b6
  :test s/valid-content-headers?
  :yes :b5
  :no (stop-response 501))

(defstate b5
  :test s/known-content-type?
  :yes :b4
  :no (stop-response 415))

(defstate b4
  :test s/valid-entity-length?
  :yes :b3
  :no (stop-response 413))



