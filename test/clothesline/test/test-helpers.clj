(ns clothesline.test.test-helpers
  (:require [clojure.walk :as walk])
  (:import java.io.StringBufferInputStream))


(defn- make-body [contents]
  (StringBufferInputStream. contents))
(def ^{:private true} default-body (make-body ""))

(def ^{:private true} query-model
     {:remote-addr "0:0:0:0:0:0:0:1%0",
      :scheme :http,
      :request-method :get,
      :query-string nil,
      :content-type nil,
      :uri "/",
      :server-name "localhost",
      :headers {"connection" "keep-alive",
                "cookie" "ookie",
                "accept-encoding" "gzip, deflate",
                "accept-language" "en-us",
                "accept" "application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5",
                "user-agent" "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_5; en-us) AppleWebKit/533.19.4 (KHTML, like Gecko) Version/5.0.3 Safari/533.19.4",
                "host" "localhost:8333"},
      :content-length nil,
      :server-port 8333,
      :character-encoding nil,
      :body default-body})

(defn make-request [& {:as mergevals}]
  (merge query-model mergevals))

(def *method* :get)

(defn make-request-query [& {:as querykeys}]
  (let [qpairs (map (fn [[k v]] (str (name k)
                                      "=" (str v)))
                     querykeys)
        querymap (walk/stringify-keys querykeys)
        qstr (apply str (interpose "&" qpairs))]
    (make-request :query-string qstr
                  :query-params querymap
                  :params querymap
                  :request-method *method*)))


