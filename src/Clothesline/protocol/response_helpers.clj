(ns clothesline.protocol.response-helpers
  (:require [clojure.contrib [string :as strs]]
            [clothesline [service :as s]]))

(defn get-with-key [map key]
  (when-let [v (get map key)]
    [key v]))

(defn stop-response
  ([^int code] {:status code :headers {}})
  ([^int code headers] {:status code :headers headers})
  ([^int code headers msg] {:status code :headers headers :body msg}))

;; (defn generate-response [code]
;;   (fn [{:keys [handler request graphdata]}]
;;     (let [[content-type generator] (or (:content-provider graphdata)
;;                                        (first (s/content-types-provided handler
;;                                                                         request
;;                                                                         graphdata)))
;;           status code
;;           body (generator request graphdata)
;;           headers ])))

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