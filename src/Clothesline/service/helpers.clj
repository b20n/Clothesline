(ns clothesline.service.helpers
  (:require [clothesline.service :as service]))


(def ^{:arglists '([type map-of-fns] [type & kw-and-impls]) :name "extend-as-handler"}
     extend-as-handler
     (fn  [type & params]
       (if (and (map? (first params))
                (= 1 (count params)))
         (extend type service/service (merge service/service-default (first params)))
         (extend type service/service (merge service/service-default (apply hash-map params))))))


(defmacro defsimplehandler [name & ct-generator-forms]
  `(do
     (deftype ~name [])
     (extend ~name service/service
             (merge service/service-default
                    {:content-types-provided (hash-map ~@ct-generator-forms)}))))