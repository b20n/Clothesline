(ns clothesline.test.request-handler
  (:use clojure.test)
  (:require [clothesline.request-handler :as rh]
            [clothesline.test.test-helpers :as test]))

(deftest no-handler-found
  (let [response (rh/no-handler-found (test/make-request))]
    (is (= 404 (:status response)))
    ;; this was broken, so enforce a legal content-type.
    (is (re-find #"^[^/]+/[^/]+$" (get-in response [:headers "Content-Type"])))))
