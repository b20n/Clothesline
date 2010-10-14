(ns clothesline.request-handler
  (:use ring.util.response
        ring.adapter.jetty
        clout.core
        [clothesline.protocol (graph :as g)]))

(defn set-routes [route-map] 
  "Set the route map to be used by the handler. This should take the form of
   a URL path as the key and a ref to a service implementation as the value"
  (def routes route-map))

(defn no-handler-found [req]
  "Returns a 404 when no appropriate handler was found"
  (-> (response "Not found")
      (status 404)))

(defn get-route [route-map req]
  "Selects the appropriate place to route the request based on the supplied map"
  (first (filter #(route-matches (first %) req) route-map)))

(defn handler [req]
  "Slim little shim for getting the route and doing something with it"
  (let [route (get-route routes req)]
    ;; er, I think (from this gist https://gist.github.com/3085f7636f6be32b2ef4)
    ;; that this is how it should actually be called. Yes?
    (if route 
      (g/start {:handler route 
                :request req 
               :graphdata {}})
      (no-handler-found req))))

