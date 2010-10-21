(ns clothesline.simple-server
  (:use clothesline.service.helpers
        ring.adapter.jetty)
  (:require [clothesline [request-handler :as rh]]))

(defsimplehandler bogus-server
  "text/plain" (fn [req data] "Fu!")
  "text/html" (fn [req data] "<h1>Fu!</h1>"))

(defsimplehandler bogus-server2
  "text/plain" (fn [req data] (str "Your params are: " (:params req) "\nRequest Dump: " req)))

(rh/set-routes { "/" bogus-server,
                 "/:p" bogus-server2})

(defonce *current-server*
     (run-jetty #'rh/handler { :join? false :port 8999 }))

