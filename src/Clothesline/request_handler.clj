(ns clothesline.request_handler
  (:use ring.util.response
        ring.adapter.jetty
        clout.core)
  (:require [clothesline.protocol (graph :as g)]))

(defn no-handler-found [req graph-data]
  "Returns a 404 when no appropriate handler was found"
  (-> (response "Not found")
      (status 404)))

(defn get-route
  "Selects the appropriate place to route the request based on the supplied map"
  ([route-map req default]
    (let [[route fun] (first (filter #(route-matches (first %) req) route-map))]
      (or fun default)))
  ([route-map req] (get-route route-map req no-handler-found)))

(defn handler [req]
  "Slim little shim for getting the route and doing something with it"
  ;; er, I think (from this gist https://gist.github.com/3085f7636f6be32b2ef4)
  ;; that this is how it should actually be called. Yes?
  ;; ... also ... couldn't think of a better way to pass the route map
  ;; but I'm sure a global is *not* it
  (g/start {:handler (get-route *routes* req) 
            :request req 
            :graphdata {}}))

