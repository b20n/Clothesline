(ns clothesline.complex_server
  (:require [clothesline.service :as service]
            [clothesline.service.helpers :as helpers]
            [clothesline.protocol.test-helpers :as test]
            clothesline.core))

;; Storage
(def *url-store* (ref {}))
(defn add-name-url [name url]
     (dosync (alter *url-store* assoc name url)))
(defn name-exists? [name] (boolean (some #{name} (keys @*url-store*))))
(defn get-name-url [name] (get @*url-store* name))


;; Behavior
(def behavior
     {:allowed-methods (constantly #{:get :post})
      :malformed-request? (fn [_ {:keys [request-method params]} _]
                            (let [name (params "name")
                                  location (params "location")]
                              (cond
                               (= request-method :get) (nil? name)
                               :otherwise              (or (nil? name)
                                                           (nil? location)))))
      :previously-existed? (fn [_ {params :params} _] (get-name-url (params "name")))
      :resource-exists? (constantly false)
      :allow-missing-post? (constantly true)
      :moved-permanently? (fn [_ {params :params} _] (get-name-url (params "name")))
      :post-is-create? (fn [_ {params :params} _] (not (name-exists? (params "name"))))
      :create-path (fn [_ {{:strs [name location]} :params} _]
                     (add-name-url name location)
                     (test/annotated-return name))
      :process-post (fn [_ {{:strs [name location]} :params :as request} _]
                         (add-name-url name location)
                         (test/annotated-return true))
     }

)

(defrecord bookmark-handler [])
(helpers/extend-as-handler bookmark-handler behavior)

;; Server

(defonce *server* (clothesline.core/produce-server {"/:name" (bookmark-handler.)} 
                                                   {:join? false :port 9001}))


