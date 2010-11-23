(ns clothesline.protocol.graph
  (:use [clothesline.protocol
         syntax
         response-helpers
         graph-helpers
         test-helpers
         test-errors]
        [clj-time [core :only [after? now]]])
  (:require [clothesline [service :as s]]
            [clojure.contrib [error-kit :as error-kit]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Helpers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; It'd be nice if eventually this generated the header we need.
(defn accept-content-helper [handler request graphdata]
  (let [handlers (getres (s/content-types-accepted handler request graphdata))]
    (if-let [[type body-handler] (map-accept-header request "content-type" handlers false)]
      (do        
        (body-handler request graphdata)
        true)
      false)))

; P3 and O14 both share similar logic, represented here
(defn conflict-states-helper [{:keys [handler request graphdata]}]
  (let [conflict-rval (s/conflict? handler request graphdata)
        conflict?     (getres conflict-rval)
        handled?         (when-not conflict?
                           (accept-content-helper handler request graphdata))]
    (cond
     conflict?    (annotated-return true)
     handled?     (annotated-return false)
     :unhandled?  (breakout-of-test 415))))

 ;; State N11 is something of a bear, unfortuantely. 
 (defn n11-helper [{:keys [handler request graphdata]}]
   (let [[is-create? s1-anns] (getresann (s/post-is-create? handler
                                                            request
                                                            graphdata))
         igraphdata (update-graphdata-with-anns graphdata s1-anns)]
     (if is-create?
       (let [[cpath s2-anns] (getresann (s/create-path handler request igraphdata))
             merged-anns (merge-annotations s1-anns
                                            s2-anns
                                            {:annotate {:post-created-path cpath
                                                        :post-status true}
                                             :headers {"Location" cpath}})
             final-graphdata (update-graphdata-with-anns graphdata merged-anns)]
         (if (accept-content-helper handler request final-graphdata)
           (annotated-return (:post-is-redirect final-graphdata)
                             merged-anns)
           (breakout-of-test 415)))
       (let [[post-status s2-anns] (getresann (s/process-post handler
                                                              request
                                                              igraphdata))
             merged-anns (merge-annotations s1-anns
                                            s2-anns
                                            {:annotate {:post-status post-status}})
             final-graphdata (update-graphdata-with-anns graphdata
                                                         merged-anns)]
         (annotated-return (boolean (and (-> final-graphdata :headers (get "Location"))
                                         (:post-is-redirect final-graphdata)))
                           merged-anns)))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Protocol Graph
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(protocol-machine
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
           ((getres (s/allowed-methods handler request graphdata)) 
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
             (let [options-headers (or (getres (s/options handler request graphdata)) {})]
               {:status 200
                :body ""
                :headers (getres options-headers)})
             (c3 args))))

 (defstate c3
   :test (request-header-exists? "accept")
   :yes c4
   :no  d4)
 
 
 (defstate c4
   "Map the accept handler through and set it."
   :test (fn [{:keys [handler request graphdata]}]
           (let [available-handlers (getres (s/content-types-provided handler
                                                                      request
                                                                      graphdata))
                 [type generator :as chosen]  (map-accept-header request
                                                                 "accept"
                                                                 available-handlers)]
             (if chosen
               (annotated-return true {:annotate {:body generator}
                                       :headers  {"Content-Type" type}})
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
           (let [available-handlers (getres (s/charsets-provided handler
                                                                 request
                                                                 graphdata))
                 chosen  (map-accept-header request "accept-charset" available-handlers)]
             (if chosen
               (annotated-return true {:annotate {:content-charset chosen}})
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
           (let [available-handlers (getres (s/charsets-provided handler
                                                                 request
                                                                 graphdata))
                 [name encoder :as chosen]  (map-accept-header request
                                                               "accept-encoding"
                                                               available-handlers)]
             (if chosen
               (annotated-return true {:annotate {:content-encoding name
                                                  :content-encoder  encoder}})
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
                 [etag ann] (getres (s/generate-etag handler request graphdata))]
             (annotated-return (= if-match-value etag) ann)))
   :yes h10
   :no (stop-response 412))

 (defstate h10
   :test (request-header-exists? "if-unmodified-since")
   :yes h11 
   :no i12)

 (defstate h11
   :test (fn [{request :request}]
           (if-let [date (date-for-request-header request "if-unmodified-since")]
             (annotated-return true {:annotate {:if-unmodified-since date}})))
   :yes h12
   :no i12)

 (defstate h12
   :test (fn [{h :handler
               req :request
               {date :if-unmodified-since :as gd} :graphdata}]
           (if-let [[v ann] (getresann (s/last-modified h req gd))]
             (annotated-return (after? v date)
                               ann)
             false))
   :yes (stop-response 412)
   :no  i12)
 
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
           (let [[v ann] (getresann (s/generate-etag handler request graphdata))] 
             (annotated-return (= v (hv request "if-none-match"))
                               ann)))
   :yes j18
   :no l13)

 (defstate j18
   :test (request-method-is? :get)
   :yes (stop-response 304)
   :no (stop-response 412))

 (defstate l13
   :test (request-header-exists? "if-modified-since")
   :yes l14
   :no m16)

 (defstate l14
   :test (fn [{request :request}]
           (if-let [date (date-for-request-header request "if-modified-since")]
             (annotated-return true {:annotate {:if-modified-since date}})
             false))
   :yes l15
   :no m16)

 (defstate l15
   :test (fn [{{ims-date :if-modified-since :as graphdata} :graphdata}]
           (after? ims-date (now)))
   :yes m16
   :no l17)

 (defstate l17
   :test (fn [{h :handler
               req :request
               {ims-date :if-modified-since :as gd} :graphdata}]
           (if-let [[lm-date ann] (getresann (s/last-modified h req gd))]
             (annotated-return (after? lm-date ims-date) ann)
             true)) ;; We kick to m16 if there is no last-modified date.
                    ;; ... What? Do you have a better idea?
   :yes m16
   :no (stop-response 304))

 (defstate m16
   :test (request-method-is? :delete)
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
   :no  (normal-response 200)
   )
 
 (defstate n16
   :test (request-method-is? :post)
   :yes n11
   :no o16)

 (defstate o16
   :test (request-method-is? :put)
   :yes o14
   :no o18)

 (defstate o14
   :test conflict-states-helper
   :yes (stop-response 409)
   :no  p11)

 ;; Back up to failure states

 (defstate h7
   :test (request-header-is? "if-match" "*")
   :yes (stop-response 412)
   :no  i7)

 (defstate i7
   :test (request-method-is? :put)
   :yes i4
   :no k7)

 (defstate k7
   :test (call-on-handler s/previously-existed?)
   :yes k5
   :no l7)

 (defstate k5
   :test (fn [{:keys [handler request graphdata]}]
           (let [[redirect-to ann] (getresann (s/moved-permanently? handler
                                                                    request
                                                                    graphdata))]
             (if redirect-to
               (annotated-return true (merge ann {:headers {"Location", redirect-to}}))
               (annotated-return false ann))))
   :yes (normal-response 301)
   :no l5)

 (defstate i4
   :test (fn [{:keys [handler request graphdata]}]
           (let [[redirect-to ann] (getresann (s/moved-permanently? handler
                                                                    request
                                                                    graphdata))]
             (if redirect-to
               (annotated-return true (merge ann {:headers {"Location", redirect-to}}))
               (annotated-return false ann))))
   :yes (normal-response 301)
   :no p3)

 (defstate l5
   :test (fn [{:keys [handler request graphdata]}]
           (let [[redirect-to ann] (getresann (s/moved-temporarily? handler
                                                                    request
                                                                    graphdata))]
             (if redirect-to
               (annotated-return true (merge ann {:headers {"Location", redirect-to}}))
               (annotated-return false ann))))
   :yes (normal-response 307)
   :no m5)

 (defstate l7
   :test (request-method-is? :post)
   :no (stop-response 404)
   :yes m7)

 (defstate m7
   :test (call-on-handler s/allow-missing-post?)
   :yes n11
   :no (stop-response 404))

 (defstate m5
   :test (request-method-is? :post)
   :no (stop-response 410)
   :yes n5) ; Identical to m7

 (defstate n5
   :test (call-on-handler s/allow-missing-post?)
   :no (stop-response 410)
   :yes n11) 

 (defstate n11
   :test n11-helper
   :yes (normal-response 303)
   :no p11)

 (defstate p11
   :test (response-header-set? "Location")
   :yes (normal-response 201)
   :no  o20)

 (defstate p3
   :test conflict-states-helper
   :yes (stop-response 409)
   :no p11)
 
 
 (defstate temp-end
   :test (constantly (annotated-return false {:annotate {:fart true}
                                              :headers  {"fart" "true"}}))
   :yes :respond
   :no {:status 500, :body "Epic fail."})

 (defstate test-fail-state
   :test (fn [& args] (breakout-of-test 999)))
 
 ) ; Protocol machine v3.

  (def start #'b13)