(ns clothesline.protocol.body-helpers
  (:require [clojure.contrib [string :as strs]]
            [clothesline [service :as s]]
            [clothesline.protocol.test-helpers :as helpers])
  (:use     [clothesline [util :only [assoc-if take-until]]]))


(defn produce-body [body request graphdata]
  (cond
   (instance? clojure.lang.IFn body) (delay (body request graphdata))
   :else                             (delay body)))

(defn default-content-handler [handler request graphdata]
  (let [[ct handler] (first (helpers/getres (s/content-types-provided handler
                                                                      request
                                                                      graphdata)))]
    [ct (produce-body handler)]))


(defn content-handler [handler request {{ct "Content-Type"} :headers
                                        body :body :as graphdata}]
  (if body
    [(or ct "text/plain") (produce-body body request graphdata)]
    (default-content-handler handler request graphdata)))

(defn body-content [handler request graphdata]
  (let [[content-type-name content-source] (content-handler handler request graphdata)]
    [content-type-name (produce-body content-source)]))
