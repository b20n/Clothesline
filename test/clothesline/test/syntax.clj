(ns clothesline.test.syntax
  (:use clojure.test
        clothesline.test.test-helpers)
  (:require [clothesline.protocol.graph-helpers :as graph-helpers]
            [clothesline.service :as service]))

(deftest call-on-handler 
  (let [test-fn (graph-helpers/call-on-handler service/uri-too-long?)]
    (is (= (test-fn {:handler (testing-handler :uri-too-long? true)})
           true))
    (is (= (test-fn {:handler (testing-handler :uri-too-long? false)})
           false))))