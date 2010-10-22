(ns clothesline.service
  (:import clothesline.service.IService))

(defprotocol service
  "Service objects comform to this spec."
  (resource-exists?       [self request graphdata] "Returning non-true results in 404.")
  (service-available?      [self request graphdata] "Returning non-true results in 503")
  (authorized?            [self request graphdata] "Returning non-true results in 501 & header specified if auth header")
  (forbidden?             [self request graphdata] "Default false, returning true results in 403")
  (allow-missing-post?    [self request graphdata] "Default false, should return true if you want to post to empty space")
  (malformed-request?     [self request graphdata] "Default false, return true to return 400")
  (uri-too-long?          [self request graphdata] "Default false, return true for 414")
  (known-content-type?    [self request graphdata] "Default to uses content-types-provided")
  (valid-content-headers? [self request graphdata] "Default to true")
  (valid-entity-length?   [self request graphdata] "Default to true")
  (options                [self request graphdata] "Default to nil, may be a map of headers to add in to response.")
  (allowed-methods        [self request graphdata] "Default to #{:get, :head}")
  (delete-resource        [self request graphdata] "Default to false. Called when a DELETE request should be enacted, true if deletion succeeded.")
  (delete-completed?      [self request graphdata] "Default to true. This is only called after a successful delete_resource call. Return false if
                                          the results cannot be confirmed.")
  (post-is-create?        [self request graphdata] "Default to false. If POST requests should be treated as a request to put content into a
                                          (potentially new) resource as opposed to being a generic submission for processing, then
                                          this function should return true. If it does return true, then create_path will be called
                                          and the rest of the request will be treated much like a PUT to the Path entry returned
                                          by that call.")
  (create-path            [self request graphdata] "This will be called on a POST request if post_is_create returns true.
                                                    It is an error for this function to not produce a Path if post_is_create
                                                    returns true. The Path returned should be a valid URI part following the
                                                    dispatcher prefix. That Path will replace the previous one in the return
                                                    value of wrq:disp_path(ReqData) for all subsequent resource function calls
                                                    in the course of this request.")
  (process-post           [self request graphdata] "Default false. Return true if post is handled correcly, or false otherwise.")
  (content-types-provided [self request graphdata] "Default to {}. Should provide content-type (as string) to fn(request,data) mapping.")
  (content-types-accepted [self request graphdata] "Default to {}. Should provide content-type (as string) to fn(request) mapping for accepted types.")
  (charsets-provided      [self request graphdata] "Default to {}, should provide charset-name -> fn(characters) mapping if any should be provided.")
  (encodings-provided     [self request graphdata])
  (variances              [self request graphdata] "tbd how to handle this properly")
  (conflict?              [self request graphdata] "Default false. Returning true results in 409")
  (multiple-choices?      [self request graphdata] "Default false, return true for result in 300")
  (previously-existed?    [self request graphdata] "Default false.")
  (moved-permanently?     [self request graphdata] "Default false. If return to non-false, return value is used as path.")
  (moved-temporarily?     [self request graphdata] "Default false. If return to non-false, return value is used as path.")
  (last-modified          [self request graphdata] "Default nil. Return a DateTime to set it.")
  (expires                [self request graphdata] "Default nil. Return a DateTime to set it.")
  (generate-etag          [self request graphdata] "Default nil, return a value that is str-able to set ETag header.")
  (finish-request         [self request graphdata] "Default true. Called after request is complete."))

;; We keep a source representation so that, one glorious day,
;; we could stitch the source reps into a deftype and get
;; better handler performance.
(def source-service-default
'{
 :resource-exists? (fn [self request graphdata] (.resourceExists self request graphdata))
 :service-available? (fn [self request graphdata] (.serviceAvailable self request graphdata))
 :authorized? (fn [self request graphdata] (.authorized  self request graphdata))
 :forbidden? (fn [self request graphdata] (.forbidden  self request graphdata))
 :allow-missing-post? (fn [self request graphdata] (.allowMissingPost  self request graphdata))
 :malformed-request? (fn [self request graphdata] (.malformedRequest  self request graphdata))
 :uri-too-long? (fn [self request graphdata] (.uriTooLong  self request graphdata))
 :known-content-type? (fn [self request graphdata] (.knownContentType  self request graphdata))
 :valid-content-headers? (fn [self request graphdata] (.validContentHeaders  self request graphdata))
 :valid-entity-length? (fn [self request graphdata] (.validEntityLength  self request graphdata))
 :options (fn [self request graphdata] (.options  self request graphdata))
 :allowed-methods (fn [self request graphdata] (.allowedMethods  self request graphdata))
 :delete-resource (fn [self request graphdata] (.deleteResource  self request graphdata))
 :delete-completed? (fn [self request graphdata] (.deleteCompleted  self request graphdata))
 :post-is-create? (fn [self request graphdata] (.postIsCreate  self request graphdata))
 :create-path (fn [self request graphdata] (.createPath  self request graphdata))
 :process-post (fn [self request graphdata] (.processPost  self request graphdata))
 :content-types-provided (fn [self request graphdata] (.contentTypesProvided  self request graphdata))
 :content-types-accepted (fn [self request graphdata] (.contentTypesAccepted  self request graphdata))
 :charsets-provided (fn [self request graphdata] (.charsetsProvided  self request graphdata))
 :encodings-provided (fn [self request graphdata] (.encodingsProvided  self request graphdata))
 :variances (fn [self request graphdata] (.variances  self request graphdata))
 :conflict? (fn [self request graphdata] (.conflict  self request graphdata))
 :multiple-choices? (fn [self request graphdata] (.multipleChoices  self request graphdata))
 :previously-existed? (fn [self request graphdata] (.previously-existed  self request graphdata))
 :moved-permanently? (fn [self request graphdata] (.movedPermanently  self request graphdata))
 :moved-temporarily? (fn [self request graphdata] (.movedTemporarily  self request graphdata))
 :last-modified (fn [self request graphdata] (.lastModified  self request graphdata))
 :expires (fn [self request graphdata] (.expires  self request graphdata))
 :generate-etag (fn [self request graphdata] (.generateEtag  self request graphdata))
 :finish-request (fn [self request graphdata] (.finishRequest  self request graphdata))
})

(def service-default
     (reduce #(assoc %1 (first %2)
                     (eval (second %2)))
             {}
             source-service-default))


(def iservice-default-binding)

(extend-protocol service
  clothesline.service.IService
  (resource-exists?  [self request graphdata] true)
  (service-available?  [self request graphdata] true)
  (authorized?  [self request graphdata] true)
  (forbidden?  [self request graphdata] false)
  (allow-missing-post?  [self request graphdata] false)
  (malformed-request?  [self request graphdata] false)
  (uri-too-long?  [self request graphdata] false)
  (known-content-type?  [self request graphdata] true)
  (valid-content-headers?  [self request graphdata] true)
  (valid-entity-length?  [self request graphdata] true)
  (options  [self request graphdata] nil)
  (allowed-methods  [self request graphdata] #{:get :head})
  (delete-resource  [self request graphdata] false)
  (delete-completed?  [self request graphdata] true)
  (post-is-create?  [self request graphdata] false)
  (create-path  [self request graphdata] false)
  (process-post  [self request graphdata] false)
  (content-types-provided  [self request graphdata] {})
  (content-types-accepted  [self request graphdata] {})
  (charsets-provided  [self request graphdata] nil)
  (encodings-provided  [self request graphdata] nil)
  (variances  [self request graphdata] nil)
  (conflict?  [self request graphdata] false)
  (multiple-choices?  [self request graphdata] false)
  (previously-existed?  [self request graphdata] false)
  (moved-permanently?  [self request graphdata] false)
  (moved-temporarily?  [self request graphdata] false)
  (last-modified  [self request graphdata] nil)
  (expires  [self request graphdata] nil)
  (generate-etag  [self request graphdata] nil)
  (finish-request  [self request graphdata] true))