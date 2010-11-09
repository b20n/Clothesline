(ns clothesline.core
  (:require [ring.adapter [jetty :as ring]]
            [clothesline [util :as util]])
  (:use [clothesline.request-handler :only [compile-route-map handler *routes*
                                            run-request]])
  (:import org.mortbay.jetty.Server)
  (:gen-class :name clothesline.interop.Factory
              :init init
              :methods [^{:static true} [makeServer
                         [java.util.Map java.util.Map] ; ->
                         org.mortbay.jetty.Server]]))


(defn produce-request-handler [handler]
  (partial run-request handler))

(def ^{:doc "Set false if you hate performance"} *auto-compile-routes* true)

(defn produce-handler [routes]
  (binding [*routes* (if *auto-compile-routes* (compile-route-map routes) *routes*)]
    (bound-fn [req] (handler req))))

(defn produce-server
     ([routes server-opts]
        (ring/run-jetty (produce-handler routes) server-opts))
     ([routes] (produce-server routes {:port 80 :join? false})))

;; Expose to the outside world.
(defn -init [])
(defn -makeServer [routeTable server-opts]
  (produce-server routeTable (util/map-keys keyword server-opts)))
