(ns clothesline.simple-server
  (:use clothesline.service.helpers
        ring.adapter.jetty
        clothesline.core))

(defsimplehandler bogus-server
  "text/plain" (fn [req data] "Fu!")
  "text/html" (fn [req data] "<h1>Fu!</h1>"))

(defsimplehandler bogus-server2
  "text/plain" (fn [req data] (str "Your params are: " (:params req) "\nRequest Dump: " req)))

(def example-routes { "/" bogus-server,
                      "/:p" bogus-server2})

(defonce *server* (delay (produce-server example-routes {:port 8999 :join? false})))