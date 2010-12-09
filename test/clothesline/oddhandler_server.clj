(ns clothesline.oddhandler-server
  (:use clothesline.service.helpers
        ring.adapter.jetty
        clothesline.core))

(defsimplehandler bogus-server
  "text/plain" (fn [req data] "Fu!")
  "text/html" (fn [req data] "<h1>Fu!</h1>"))

(defsimplehandler bogus-server2
  "text/plain" (fn [req data] (str "Your params are: " (:params req) "\nRequest Dump: " req)))

(def example-routes { "/"   (constantly bogus-server)
                      "/:p" (class bogus-server2)})

(defonce *server* (produce-server example-routes {:port 8999 :join? false}))