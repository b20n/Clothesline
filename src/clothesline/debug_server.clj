(ns clothesline.debug-server
  (:use clothesline.service.helpers
        ring.adapter.jetty)
  (:require [clothesline [request-handler :as rh]]))

(def saved-responses (atom []))

(defn- handle-req [r]
  (println "Handling mah req")
  (swap! saved-responses concat [r])
  {:status 200, :headers {}})

(defonce instance
     (run-jetty #'handle-req { :join? false :port 8333 }))

