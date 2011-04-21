(ns clothesline.protocol.body-helpers
  (:require [clojure.contrib [string :as strs]]
            [clothesline [service :as s]]
            [clothesline.protocol.test-helpers :as helpers]
	    [clojure.java.io :as io])
  (:use     [clothesline [util :only [assoc-if take-until]]])
  (:import (java.io File InputStream FileInputStream ByteArrayOutputStream)))


(defn produce-body [body request graphdata]
  (cond
   (instance? clojure.lang.IFn body) (delay (body request graphdata))
   (instance? File body) (with-open [stream (FileInputStream. body)]
			   (produce-body stream request graphdata))
   (instance? InputStream body) (delay (let [baos (ByteArrayOutputStream.)]
					 (io/copy body baos)
					 (.toByteArray baos)))
   :else                             (delay body)))

(defn default-content-handler [handler request graphdata]
  (let [[ct handler] (first (helpers/getres (s/content-types-provided handler
                                                                      request
                                                                      graphdata)))]
    [ct handler]))


(defn content-handler [handler request {{ct "Content-Type"} :headers
                                        body :body :as graphdata}]
  (if body
    [(or ct "text/plain") body]
    (default-content-handler handler request graphdata)))

(defn body-content [handler request graphdata]
  (let [[content-type-name content-source] (content-handler handler request graphdata)]
    [content-type-name (produce-body content-source request graphdata)]))
