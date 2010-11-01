(ns clothesline.protocol.body-helpers
  (:require [clojure.contrib [string :as strs]]
            [clothesline [service :as s]]
            [clothesline.protocol.test-helpers :as helpers])
  (:use     [clothesline [util :only [assoc-if take-until]]]))

(defn build-body [handler request graphdata]
  (if (:body graphdata)
    ;; Explicit bodies always get added.
    [(or (:content-type graphdata) "text/plain") (:body graphdata)]
    (if (#{:get :head} (:request-method request))
      ;; It's a head or get, so we want to build a body explicitly.
      (let [[content-type generator] (or (:content-handler graphdata)
                                         (first (helpers/getres (s/content-types-provided handler
                                                                                  request
                                                                                  graphdata))))
            body                     (if generator
                                       (generator request graphdata)
                                       "")]
        [content-type body]))))