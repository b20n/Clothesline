(ns clothesline.protocol.response-helpers)

(defn stop-response
  ([^int code] {:status code :headers {}})
  ([^int code headers] {:status code :headers headers})
  ([^int code headers msg] {:status code :headers headers :body msg}))

(defn request-header-exists
  "Produces a test function that checks to see if a header is
   present."
  [header-name]
  (fn [_ {headers :headers}  _]
    (if (get headers header-name)
      true
      false)))



