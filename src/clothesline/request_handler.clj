(ns clothesline.request-handler
  (:use ring.util.response
        ring.middleware.params
        clout.core
        [clothesline.protocol (graph :as g)]))

(defonce *routes* (ref {}))

(defn set-routes [route-map] 
  "Set the route map to be used by the handler. This should take the form of
   a URL path as the key and a ref to a service implementation as the value"
  (dosync (alter *routes* merge route-map)))

(defn no-handler-found [req]
  "Returns a 404 when no appropriate handler was found"
  (-> (response "404 - Resource Not found")
      (content-type "text-plain")
      (status 404)))

(defn- match-route [req route handler]
  "Internal route matcher."
  (when-let [new-params (route-matches route req)]
    [req handler new-params]))

(defn get-route [route-map req]
  "Selects the appropriate place to route the request based on the supplied map"
  (first (keep #(match-route req (first %) (second %)) route-map)))

(defn base-handler [req]
  "Slim little shim for getting the route and doing something with it"
  (if-let [[req handler new-params] (get-route @*routes* req)]
    (g/start {:handler handler
              :request (-> req
                           (assoc :url-params new-params)
                           (assoc :params (merge (:params req)
                                                 new-params)))
              :graphdata {}})
    (no-handler-found req)))

(def ^{:doc "The default, normally-wrapped handler. Includes query and param mapping.",
       :arglists '([request])
       :name "handler"}
     handler
     (-> base-handler
         wrap-params))

