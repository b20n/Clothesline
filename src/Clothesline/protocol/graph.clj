(ns clothesline.protocol.graph
  (:use [clothesline.protocol [response-helpers]
         [syntax]])
  (:require [clothesline [service :as s]]))

;; Proposed syntax

(protocol-machine "v3"
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
    :test (fn [handler request graphdata]
            ((s/allowed-methods handler request graphdata) 
             (:request-method request)))
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

  (defstate b3
    :body (fn [{:keys [handler request graphdata] :as args}]
            (if (= :options (:request-method request))
              (let [options-headers (or (s/options handler request graphdata) {})]
                {:status 200
                 :body ""
                 :headers options-headers})
              (c3 args))))

  (defstate c3
    :test (request-header-exists "accept")
    :yes :c4
    :no :d4)
  
  
  (defstate c4
    :test (fn [handler request graphdata]
            (let [acceptv ((request :headers) "accept")
                  handlers (s/content-types-provided handler request graphdata)
                  chosen-handler (handlers acceptv)]
              (if chosen-handler
                {:result true :add-data {:content-provider chosen-handler}}
                false)))
    :yes d4
    :no (stop-response 406))

  (defstate d4
    :test (request-header-exists "accept-language")
    :yes d5
    :no e5)

  (defstate d5
    "Currently ignored. TODO: Fix me!"
    :test (constantly true)
    :yes e5
    :no (stop-response 406))

  (defstate e5
    :test (request-header-exists "accept-charset")
    :yes e6
    :no f6)

  (defstate e6
    "Check and select a supported character set"
    :test (fn [handler request data]
            (let [converters    (s/charsets-provided handler request data)
                  accept-c      (split-header-field request "accept-charset")]
              (if (some #{"*"} accept-c)
                true                    ; Bank out if * is allowed.
                (if-let [converter (first (keep #(get converters %) accept-c))]
                  {:result true, :character-converter converter}
                  false))))
    :yes f6
    :no (stop-response 406))

  (defstate f6
    "Check if accept-encoding header is present"
    :test (request-header-exists "accept-encoding")
    :yes f7
    :no g7)

  (defstate f7
    :test (fn [handler request data]
            (let [encoders (s/encodings-provided handler request data)
                  encode-c (split-header-field request "accept-encoding")]
              (if (some #{"*"} encode-c)
                true
                (if-let [encoder (first keep #(get encoders %) accept-c)]
                  {:result true, :encoding-converter encoder}
                  false))))
    :yes g7
    :no (stop-response 406))

  (defstate g7
    :test s/resource-exists?
    :yes g8
    :no  h7)

  
  

  (defstate temp-end
    :test (constantly true)
    :yes :respond
    :no {:status 500, :body "Epic fail."})
  ) ; Protocol machine v3.