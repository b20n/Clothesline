(ns clothesline.service.helpers
  (:require [clothesline.service :as service]))

(def
  ^{:name "extend-as-handler"
    :arglists '([type map-of-fns] [type & kw-and-impls])
    :doc "Extend an existing type with the service behaviours to become a
Clothesline resource.  This merges the supplied behaviours with the default
set of behaviours required to work correctly."}
     extend-as-handler
     (fn  [type & params]
       (if (and (map? (first params))
                (= 1 (count params)))
         (extend type service/service (merge service/service-default (first params)))
         (extend type service/service (merge service/service-default (apply hash-map params))))))


(defmacro defhandler
  "Define a new Clothesline handler type and instance, extending the default
behaviour with custom handlers by mapping symbols to service implementations.

See `clothesline.service/service` or WebMachine for available methods,
including their arguments and return values.

See `extend-as-handler` for full details of the valid body forms.

    (defhandler example {:service-available? (constantly false)})

    ;; (defn sample-malformed-request [...] ...)
    (defhandler sample
      :allowed-methods     (constantly #{:head :get})
      :malformed-request?  sample-malformed-request?
      :resource-exists? (fn [...] ...))"
  {:arglists '([name map-of-fns] [name & kw-and-impls])}
  [name & params]
  (let [typename (symbol (str name "-type"))]
    `(do
       ;; Define a type to hook into the protocol mechanism.
       (deftype ~typename [])
       ;; Extend that with the standard suite of behaviours...
       (clothesline.service.helpers/extend-as-handler ~typename ~@params)
       ;; ...and a user accessor.
       (def ~name (new ~typename)))))


(defmacro defsimplehandler [name & ct-generator-forms]
  `(clothesline.service.helpers/defhandler ~name
     :allowed-methods (constantly #{:get :head :post :put :delete :options})
     :content-types-provided (fn [handler# request# graphdata#]
                               (hash-map ~@ct-generator-forms))))
