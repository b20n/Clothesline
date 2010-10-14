(ns clothesline.protocol.response-helpers
  (:require [clojure.contrib [string :as strs]]
            [clothesline [service :as s]])
  (:use     [clothesline [util :only [assoc-if get-with-key]]]))



(defn stop-response
  ([^int code] {:status code :headers {}})
  ([^int code headers] {:status code :headers headers})
  ([^int code headers msg] {:status code :headers headers :body msg}))



(defn build-body [handler request graphdata]
  (if (:body graphdata)
    ;; Explicit bodies always get added.
    [(:content-type graphdata) (:body graphdata)]
    (if (#{:get :head} (:request-method request))
      ;; It's a head or get, so we want to build a body explicitly.
      (let [[content-type generator] (or (:content-provider graphdata)
                                         (first (s/content-types-provided handler
                                                                          request
                                                                          graphdata)))
            body                     (if generator
                                       (generator request graphdata)
                                       "")]
        [content-type body]))))

(defn build-headers [handler request graphdata [content-type body]]
  (if (#{:get :head} (:request-method request))
    (-> {}
        (assoc-if "Content-Type" content-type)
        (assoc-if "Content-Length" (count body) body)
        (assoc-if "ETag" (s/generate-etag handler request graphdata))
        (assoc-if "Last-Modified" (s/last-modified handler request graphdata))
        (assoc-if "Expires" (s/expires handler request graphdata)))
    {}))

  
; For some reason, this isn't working. But it's _really close_
(defn generate-response [code {:keys [handler request graphdata]}]
  (let [[content-type body] (build-body handler request graphdata)
        default-headers (build-headers handler request graphdata body)
        headers (merge {} default-headers (:headers graphdata))]
    {:status code :body body :headers headers}))




(defn hv [request header]
  ((:headers request) header))

(defn split-header-field [request field]
  (let [headers (or (:headers request) {})
        field   (headers field)]
    (if field
      (strs/split #";" field)
      (list))))

(defn push-header-through [request field map]
  (let [split-header (split-header-field request field)]
    (keep #(when-let [v (get map %)] [% v]) split-header)))

(defn map-accept-header
  ([request field map with-default?]
     (or (first (push-header-through request field map))
         (or  (get-with-key map "*/*") (get-with-key map "*"))
         (and with-default? (first map))))
  ([request field map] (map-accept-header request field map true)))