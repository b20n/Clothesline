(ns clothesline.startup
  (:import clothesline.Factory)
  (:use clothesline.service.helpers)
  (:gen-class))

(defsimplehandler test-handler
  "text/plain" (fn [& args] "Yar."))

;; Todo, use args to invoke properly!
(defn -main [& args]
  (let [routes {"/" test-handler}]
    (Factory/makeServer routes {:join? true :port 9999})))