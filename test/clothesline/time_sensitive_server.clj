(ns clothesline.time-sensitive-server
  (:require [clothesline.service :as service]
            [clothesline.service.helpers :as helpers]
            [clothesline.protocol.test-helpers :as test]
            [clojure.contrib.duck-streams :as duck]
            [clj-time.core :as time]
            clothesline.core))


(defonce *last-tapped* (atom (time/now)))

(def behavior
     {:allowed-methods        (constantly #{:get :post})
      :last-modified          (fn [& args] @*last-tapped*)
      :previously-existed?    (constantly true)
      :content-types-provided (constantly {"text/plain" (fn [_ _] (str "Last tapped at " @*last-tapped*))})
      :process-post           (fn [& args]
                                (reset! *last-tapped* (time/now))
                                (str "Last tapped at: " @*last-tapped*))
      })


(defrecord time-handler [])
(helpers/extend-as-handler time-handler behavior)

;; Server
(defonce *server* (delay (clothesline.core/produce-server {"/*" (time-handler.)} 
                                                          {:join? false :port 9001})))


