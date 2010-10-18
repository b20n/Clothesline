(ns clothesline.debug-server
  (:use clothesline.service.helpers
        ring.adapter.jetty)
  (:require [clothesline [request-handler :as rh]]))

(def saved-responses (atom []))

(defn- handle-req [r]
  (println "Handling mah req")
  (swap! saved-responses concat [r])
  {:status 200, :body "<html><body><h1> YES </h1></body></html>", :headers {}})

(defonce instance
     (run-jetty #'handle-req { :join? false :port 8333 }))

