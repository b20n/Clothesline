(ns clothesline.protocol.test-errors
  (:require [clojure.contrib [error-kit :as error]]))

(error/deferror test-breakout-error []
  "When a test precondition is violated, it can throw this exception
   to force an immedaite halt of all processing. The response shown is
   exactly what is specified."
  [code headers body]
  {:msg (str "Test brokeout with code " code " (" headers " -- " body ")")
   :unhandled (error/throw-msg IllegalArgumentException)})

(defn breakout-of-test
  ([code headers body]
     (error/raise test-breakout-error code headers body))
  ([code headers] (breakout-of-test code headers nil))
  ([code] (breakout-of-test code {} nil)))


