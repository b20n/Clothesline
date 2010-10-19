(ns clothesline.simple-server
  (:use clothesline.service.helpers
        ring.adapter.jetty)
  (:require [clothesline [request-handler :as rh]]))

(defsimplehandler bogus
  "text/plain" (fn [req data] "Fu!")
  "text/html" (fn [req data] "<h1>Fu!</h1>"))

(rh/set-routes { "/" bogus })

(defonce *current-server*
     (run-jetty #'rh/handler { :join? false :port 8999 }))

