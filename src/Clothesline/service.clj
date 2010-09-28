(ns clothesline.service)

(defprotocol service
  "Service objects comform to this spec."
  (resource-exists?       [self request] "Returning non-true results in 404.")
  (service-avilable?      [self request] "Returning non-true results in 503")
  (authorized?            [self request] "Returning non-true results in 501 & header specified if auth header")
  (forbidden?             [self request] "Default false, returning true results in 403")
  (allow-missing-post?    [self request] "Default false, should return true if you want to post to empty space")
  (malformed-request?     [self request] "Default false, return true to return 400")
  (uri-too-long?          [self request] "Default false, return true for 414")
  (known-content-type?    [self request] "Default to uses content-types-provided")
  (valid-content-headers? [self request] "Default to true")
  (valid-entity-length?   [self request] "Default to true")
  (options                [self request] "Default to {}, should be a map of headers to add in to response.")
  (allowed-methods        [self request] "Default to #{:get, :head}")
  (delete-resource        [self request] "Default to false. Called when a DELETE request should be enacted, true if deletion succeeded.")
  (delete-completed?      [self request] "Default to true. This is only called after a successful delete_resource call. Return false if
                                          the results cannot be confirmed.")
  (post-is-create?        [self request] "Default to false. If POST requests should be treated as a request to put content into a
                                          (potentially new) resource as opposed to being a generic submission for processing, then
                                          this function should return true. If it does return true, then create_path will be called
                                          and the rest of the request will be treated much like a PUT to the Path entry returned
                                          by that call.")
  (create-path            [self request] "Default to false. If POST requests should be treated as a request to put content into a
                                          (potentially new) resource as opposed to being a generic submission for processing, then
                                          this function should return true. If it does return true, then create_path will be called
                                          and the rest of the request will be treated much like a PUT to the Path entry returned by
                                          that call.")
  (process-post           [self request] "Default false. Return true if post is handled correcly, or false otherwise.")
  (content-types-provided [self request] "Default to {}. Should provide content-type (as string) to fn(request) mapping.")
  (content-types-accepted [self request] "Default to {}. Should provide content-type (as string) to fn(request) mapping for accepted types.")
  (charsets-provided      [self request] "Default to {}, should provide charset-name -> fn(characters) mapping if any should be provided.")
  (encodings-provided     [self request])
  (variances              [self request] "tbd how to handle this properly")
  (conflict?              [self request] "Default false. Returning true results in 409")
  (multiple-choices?      [self request] "Default false, return true for result in 300")
  (previously-existed?    [self request] "Default false.")
  (moved-permanently?     [self request] "Default false. If return to non-false, return value is used as path.")
  (moved-temporarily?     [self request] "Default false. If return to non-false, return value is used as path.")
  (last-modified          [self request] "Default nil. Return a DateTime to set it.")
  (expires                [self request] "Default nil. Return a DateTime to set it.")
  (generate-etag          [self request] "Default nil, return a value that is str-able to set ETag header.")
  (finish-request         [self request] "Default true. Called after request is complete."))

(def service-default
{
 :resource-exists? (fn [self request] true)
 :service-avilable? (fn [self request] true)
 :authorized? (fn [self request] true)
 :forbidden? (fn [self request] false)
 :allow-missing-post? (fn [self request] false)
 :malformed-request? (fn [self request] false)
 :uri-too-long? (fn [self request] false)
 :known-content-type? (fn [self request] true)
 :valid-content-headers? (fn [self request] true)
 :valid-entity-length? (fn [self request] true)
 :options (fn [self request] {})
 :allowed-methods (fn [self request] #{:get :head})
 :delete-resource (fn [self request] false)
 :delete-completed? (fn [self request] true)
 :post-is-create? (fn [self request] false)
 :create-path (fn [self request] false)
 :process-post (fn [self request] false)
 :content-types-provided (fn [self request] {})
 :content-types-accepted (fn [self request] {})
 :charsets-provided (fn [self request] nil)
 :encodings-provided (fn [self request] nil)
 :variances (fn [self request] nil)
 :conflict? (fn [self request] false)
 :multiple-choices? (fn [self request] false)
 :previously-existed? (fn [self request] false)
 :moved-permanently? (fn [self request] false)
 :moved-temporarily? (fn [self request] false)
 :last-modified (fn [self request] nil)
 :expires (fn [self request] nil)
 :generate-etag (fn [self request] nil)
 :finish-request (fn [self request] true))
})



