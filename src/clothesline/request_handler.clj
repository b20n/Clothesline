(ns clothesline.request-handler
  (:require [clothesline.protocol [graph :as g]]
            [clothesline.service :as handler]
            [clothesline.util :as util])
  (:use [ring.util.response]
        [ring.middleware (params :only [wrap-params])]
        [clout.core :only [route-compile route-matches]]))

(defonce ^{:doc "A ref of map of ring/clout route strings to clothesline
                 handlers, which are used in the HTTP graph."}
         *routes* {})


(defn compile-route-map [route-handler-map]
  "Takes a map of [clout-route -> thing] and compiles the routes. Note
 that this destroys the equality keys, so it's mostly useful for iterating
 over the structure."
  (util/map-keys route-compile route-handler-map true))

(defn no-handler-found [req]
  "Returns a 404 when no appropriate handler was found"
  (-> (response "404 - Resource Not found")
      (content-type "text/plain")
      (status 404)))

(defn- match-route [req route handler]
  "Internal route matcher."
  (when-let [new-params (route-matches route req)]
    [req handler new-params]))

(defn get-route [route-map req]
  "Selects the appropriate place to route the request based on the supplied map"
  (first (keep #(match-route req (first %) (second %)) route-map)))

(defn run-request [handler request]
  (g/start {:handler handler
            :request request
            :graphdata {}}))

(defn as-handler [val]
  (cond
   (satisfies? handler/service val) val
   (fn? val) (val)
   (instance? java.lang.Class val) (.newInstance val)))

(defn base-handler [req]
  "Slim little shim for getting the route and doing something with it"
  (if-let [[req handler-entry new-params] (get-route *routes* req)]
    (run-request (as-handler handler-entry)
                 (-> req
                     (assoc :url-params new-params)
                     (assoc :params (merge (:params req)
                                           new-params))))
    (no-handler-found req)))

(def ^{:doc "The default, normally-wrapped handler. Includes query and param mapping.",
       :arglists '([request])
       :name "handler"}
     handler
     (-> base-handler
         wrap-params))
