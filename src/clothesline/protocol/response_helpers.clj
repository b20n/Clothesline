(ns clothesline.protocol.response-helpers
  (:require [clojure.contrib [string :as strs]]
            [clothesline [service :as s]]
            [clothesline.protocol [test-helpers :as helpers]
                                  [body-helpers :as bh]])
  (:use     [clothesline [util :only [assoc-if take-until]]]))



;; Response builders

; Define a protocol for 

(defn build-body [handler request graphdata]
  (if (:body graphdata)
    ;; Explicit bodies always get added.
    [(or (:content-type graphdata) "text/plain") (:body graphdata)]
    (if (#{:get :head} (:request-method request))
      ;; It's a head or get, so we want to build a body explicitly.
      (let [[content-type generator] (or (:content-handler graphdata)
                                         (first (helpers/getres (s/content-types-provided handler
                                                                                  request
                                                                                  graphdata))))
            body                     (if generator
                                       (generator request graphdata)
                                       "")]
        [content-type body]))))

(defn build-ct-header [content-type-str graphdata]
  (let [[enc encr] (:content-charset graphdata)
        encoding-str (when enc (str "; charset" enc))]
    (str content-type-str encoding-str)))

(defn build-normal-headers [handler
                     {method :request-method :as request}
                     graphdata
                     content-type
                     body]
  (cond
   (= :get method)
     (-> {}
        (assoc-if "Content-Type" (build-ct-header content-type graphdata) content-type)
        (assoc-if "Content-Length" (str (count @body)) @body)
        (assoc-if "ETag" (helpers/getres (s/generate-etag handler request graphdata)))
        (assoc-if "Last-Modified" (helpers/getres (s/last-modified handler request graphdata)))
        (assoc-if "Expires" (helpers/getres (s/expires handler request graphdata))))
   (= :head method)
     (-> {}
       (assoc-if "Content-Type" (build-ct-header content-type graphdata) content-type)
       (assoc-if "ETag" (helpers/getres (s/generate-etag handler request graphdata)))
       (assoc-if "Last-Modified" (helpers/getres (s/last-modified handler request graphdata)))
       (assoc-if "Expires" (helpers/getres (s/expires handler request graphdata))))))

  
(defn generate-normal-response [code {:keys [handler request graphdata]}]
  (let [[content-type body] (bh/body-content handler request graphdata)
        default-headers (build-normal-headers handler request graphdata content-type body)
        headers (merge {} default-headers (:headers graphdata))
        final-body (when-not (= :head (:request-method request)) @body)]
    ;; (println "--- BUILDING BODY! (" code ")\n---- Final Body:\n" final-body
    ;;          "\n---- Headers: " headers)
    (s/finish-request handler request graphdata) ; Currently ignored. Probably shouldn't be.
    {:status code :body final-body :headers headers}))


(defn stop-response
  ([^int code] {:status code :headers {}})
  ([^int code headers] {:status code :headers headers})
  ([^int code headers msg] {:status code :headers headers :body msg}))