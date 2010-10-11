(ns clothesline.protocol.graph
  (:use [clothesline.protocol response-helpers
                              syntax
                              graph-helpers])
  (:require [clothesline [service :as s]]))

;; Proposed syntax


(protocol-machine
 
  (def start #'b13)  
  (defstate b13
    "Check if service has been made unavailable. Return status 503 if not."
    :test (call-on-handler s/service-available?)
    :yes b12
    :no (stop-response 503))

  (defstate b12
    :test (constantly true)
    :yes b11
    :no (stop-response 501))

  (defstate b11
    :test (call-on-handler s/uri-too-long?)
    :no b10
    :yes (stop-response 414))

  (defstate b10
    :test (fn [{:keys [handler request graphdata]}]
            ((s/allowed-methods handler request graphdata) 
             (:request-method request)))
    :yes b9
    :no (stop-response 501))

  (defstate b9
    :test (call-on-handler  s/malformed-request?)
    :no b8
    :yes (stop-response 400))


  
  (defstate b8
    :test (call-on-handler  s/authorized?)
    :yes b7
    :no (stop-response 401))

  (defstate b7
    :test (call-on-handler  s/forbidden?)
    :yes (stop-response 403)
    :no  b6)

  (defstate b6
    :test (call-on-handler  s/valid-content-headers?)
    :yes b5
    :no (stop-response 501))

  (defstate b5
    :test (call-on-handler  s/known-content-type?)
    :yes b4
    :no (stop-response 415))

  (defstate b4
    :test (call-on-handler  s/valid-entity-length?)
    :yes b3
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
    :test (request-header-exists? "accept")
    :yes c4
    :no  d4)
  
  
  (defstate c4
    "Map the accept handler through and set it."
    :test (fn [{:keys [handler request graphdata]}]
            (let [available-handlers (s/content-types-provided handler
                                                               request
                                                               graphdata)
                  chosen  (map-accept-header request "accept" available-handlers)]
              (if chosen
                {:result true :annotate {:content-handler chosen}}
                false)))
    :yes d4
    :no  (stop-response 406))

  (defstate d4
    :test (request-header-exists? "accept-language")
    :yes d5
    :no  e5)

  (defstate d5
    "Currently ignored. TODO: Fix me!"
    :test (constantly true)
    :yes e5
    :no  (stop-response 406))

  (defstate e5
    :test (request-header-exists? "accept-charset")
    :yes f6 ; TODO: Re-enable accept-charset!
    :no  f6)

  (defstate e6
    "Check and select a supported character set"
    :test (fn [{:keys [handler request graphdata]}]
            (let [available-handlers (s/charsets-provided handler
                                                          request
                                                          graphdata)
                  chosen  (map-accept-header request "accept-charset" available-handlers)]
              (if chosen
                {:result true :annotate {:content-charset chosen}}
                false)))
    :yes f6
    :no (stop-response 406))

  (defstate f6
    "Check if accept-encoding header is present"
    :test (request-header-exists? "accept-encoding")
    :yes g7 ; TODO: Re-enable accept-encoding
    :no g7)

  (defstate f7
    :test (fn [{:keys [handler request graphdata]}]
            (let [available-handlers (s/charsets-provided handler
                                                          request
                                                          graphdata)
                  chosen  (map-accept-header request "accept-encoding" available-handlers)]
              (if chosen
                {:result true :annotate {:content-encoding chosen}}
                false)))
    :yes g7
    :no (stop-response 406))

  (defstate g7
    :test (call-on-handler  s/resource-exists?)
    :yes g8
    :no  h7)

  ; The graph bifurcates significantly here. Taking the path down
  ; g8 leads us towards serving a resource. Taking the path down
  ; h7 leads towards various failure responses.

  ; Resource exists, move towards serving it
  (defstate g8
    :test (request-header-exists? "if-match")
    :yes g9
    :no  h10)

  (defstate g9
    :test (fn [_ request _]
            (let [if-match-value (get (:headers request) "if-match")]
              (= "*" if-match-value)))
    :yes h10
    :no  g11)

  (defstate g11
    :test (fn [{:keys [request handler graphdata]}]
            (let [if-match-value (hv request "if-match")
                  etag (s/generate-etag handler request graphdata)]
              (= if-match-value etag)))
    :yes h10
    :no (stop-response 412))

  (defstate h10
    :test (request-header-exists? "if-unmodified-since")
                                        ; :yes h11 ; TODO: Re-enable this
    :yes i12 ; TODO: Un-ignore the date headers.
    :no i12)

  (defstate i12
    :test (request-header-exists? "if-none-match")
    :yes i13
    :no l13)


  (defstate i13
    :test (request-header-is? "if-none-match" "*")
    :yes j18
    :no k13)

  (defstate k13
    :test (fn [{:keys [request handler graphdata]}]
            (= (s/generate-etag handler request graphdata)
               (hv request "if-none-match")))
    :yes j18
    :no l13)

  (defstate j18
    :test (is-request-method? :get)
    :yes (stop-response 304)
    :no (stop-response 412))

  (defstate l13
    :test (request-header-exists? "if-modified-since")
    :yes m16 ; TODO: un-ignore date headers, this should go to L14
    :no m16)


  (defstate m16
    :test (is-request-method? :delete)
    :no  n16
    :yes m20)

  (defstate m20
    :test (call-on-handler s/delete-resource)
    :yes o20
    :no (stop-response 202))

  (defstate o20
    :test (fn [{:keys [request handler graphdata]}]
            (or (:content-provider graphdata)
                (:body graphdata)))
    :yes o18
    :no (stop-response 204))

  (defstate o18
    :test (call-on-handler s/multiple-choices?)
    :yes (stop-response 300)
    :no (generate-response 200))
  
  (defstate n16
    :test (is-request-method? :post)
    :yes 'n11
    :no o16)

  (defstate o16
    :test (is-request-method? :put)
    :yes 'o14
    :no o18)

  ;; Back up to failure states

  (defstate h7
    :test (request-header-is? "if-match" "*")
    :yes (stop-response 412)
    :no  i7)

  (defstate i7
    :test (is-request-method? :put)
    :yes i4
    :no k7)

  (defstate k7
    :test (call-on-handler s/previously-existed?)
    :yes k5
    :no l7)

  (defstate k5
    :test (fn [{:keys [handler request graphdata]}]
            (when-let [redirect-to (s/moved-permanently? handler request graphdata)]
              {:result true :headers {"Location", redirect-to}}))
    :yes (generate-response 301)
    :no l5)

  (defstate i4
    :test (fn [{:keys [handler request graphdata]}]
            (when-let [redirect-to (s/moved-permanently? handler request graphdata)]
              {:result true :headers {"Location", redirect-to}}))
    :yes (generate-response 301)
    :no p3)

  (defstate l5
    :test (fn [{:keys [handler request graphdata]}]
            (when-let [redirect-to (s/moved-temporarily? handler request graphdata)]
              {:result true :headers {"Location", redirect-to}}))
    :yes (generate-response 307)
    :no l5)
  
  
  
  (defstate temp-end
    :test (constantly true)
    :yes :respond
    :no {:status 500, :body "Epic fail."})
  ) ; Protocol machine v3.