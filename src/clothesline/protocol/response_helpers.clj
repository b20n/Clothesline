(ns clothesline.protocol.response-helpers
  (:require [clojure.contrib [string :as strs]]
            [clothesline [service :as s]]
            [clothesline.protocol [test-helpers :as helpers]
                                  [body-helpers :as bh]])
  (:use     [clothesline [util :only [assoc-if take-until datetime-to-http11-string]]]))



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


;; TODO: Rebuild this so that each request type is a set and all header building strategies
;;       are represented as a seq. Otherwise this is only going to get more absurd over time.
(defn build-normal-headers [handler
                     {method :request-method :as request}
                     graphdata
                     content-type
                     body]
  (let [etag-val          (helpers/getres (s/generate-etag handler request graphdata))
        last-modified-val (helpers/getres (s/last-modified handler request graphdata))
        expires-val       (helpers/getres (s/expires handler request graphdata))]
    (cond
     (= :get method)
     (-> {}
         (assoc-if "Content-Type" (build-ct-header content-type graphdata) content-type)
         (assoc-if "Content-Length" (str (cond
					  (instance? java.io.InputStream @body) (.available @body)
					  (instance? java.io.File @body) (.length @body)
					  :else (count @body))) @body)
         (assoc-if "ETag" (str etag-val) etag-val)
         (assoc-if "Last-Modified" (datetime-to-http11-string last-modified-val) last-modified-val)
         (assoc-if "Expires" (datetime-to-http11-string expires-val) expires-val))
     
     (= :head method)
     (-> {}
         (assoc-if "Content-Type" (build-ct-header content-type graphdata) content-type)
         (assoc-if "ETag" (str etag-val) etag-val)
         (assoc-if "Last-Modified" (datetime-to-http11-string last-modified-val) last-modified-val)
         (assoc-if "Expires" (datetime-to-http11-string last-modified-val) expires-val)))))


  
(defn generate-normal-response [code {:keys [handler request graphdata]}]
  (let [[content-type body] (bh/body-content handler request graphdata)
        default-headers (build-normal-headers handler request graphdata content-type body)
        headers (merge {} default-headers (:headers graphdata))
        final-body (when-not (= :head (:request-method request)) @body)]
    ;; (println "--- BUILDING BODY! (" code ")\n---- Final Body:\n" final-body
    ;;          "\n---- Headers: " headers)
    (s/finish-request handler request graphdata) ; Currently ignored. Probably shouldn't be.
    ;; (println "Final output: " {:status code :body final-body :headers headers})
    {:status code :body final-body :headers headers}))


(defn stop-response
  ([^int code] (fn [{:keys [handler request graphdata]}]
                 {:status code
                  :headers (merge {} (:headers graphdata))
                  :body @(bh/produce-body (:body graphdata) request graphdata)}))
  ([^int code headers] (fn [{:keys [handle request graphdata]}]
                         {:status code
                          :headers headers
                          :body @(bh/produce-body (:body graphdata) request graphdata)}))
  ; The other two handlers produce a function which produces a result, but this
  ; version of the method doesn't need that indirection. It only does so for uniformity.
  ([^int code headers body]
     (fn [_] {:status code :headers headers :body body})))


