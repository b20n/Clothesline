(ns clothesline.core
  (:require [ring.adapter [jetty :as ring]]
            [clothesline [util :as util]])
  (:use [clothesline.request-handler :only [compile-route-map handler *routes*]])
  (:import org.mortbay.jetty.Server)
  (:gen-class :name clothesline.interop.Factory
              :init init
              :methods [^{:static true} [makeServer
                         [java.util.Map java.util.Map] ; ->
                         org.mortbay.jetty.Server]]))



(def ^{:doc "Set false if you hate performance"} *auto-compile-routes* true)
(defn produce-server
     ([routes server-opts]
        (binding [*routes* (if *auto-compile-routes* (compile-route-map routes) routes)]
          (ring/run-jetty (bound-fn [req] (handler req)) server-opts)))
     ([routes] (produce-server routes {:port 80 :join? false})))

(defn -init [])

;; Expose to the outside world.
(defn -makeServer [routeTable server-opts]
  (produce-server routeTable (util/map-keys keyword server-opts)))
