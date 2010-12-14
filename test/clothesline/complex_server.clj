(ns clothesline.complex_server
  (:require [clothesline.service :as service]
            [clothesline.service.helpers :as helpers]
            [clothesline.protocol.test-helpers :as test]
            clothesline.core)
  (:require [clojure.contrib.duck-streams :as duck]))

;; Storage
(def *url-store* (ref {}))
(defn add-name-url [name url]
     (dosync (alter *url-store* assoc name url)))
(defn name-exists? [name] (boolean (some #{name} (keys @*url-store*))))
(defn get-name-url [name] (get @*url-store* name))


;; Behavior
(def behavior
     {:allowed-methods (fn [_ _ _]
                         (test/annotated-return #{:get :post :put}
                                                {:annotate {:debug-output
                                                            (fn [_] (println "o/~"))}}))
      
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
      :moved-permanently? (fn [_ {params :params method :request-method} _]
                            
                            (if (= method :put)
                              false
                              (get-name-url (params "name"))))
      :post-is-create? (fn [_ {params :params} _] (not (name-exists? (params "name"))))
      :create-path (fn [_ {{:strs [name]} :params} _] (test/annotated-return (str "/" name)))
      :process-post (fn [_ {{:strs [name location]} :params :as request} _]
                         (add-name-url name location)
                         (test/annotated-return true))

      ;; This is mostly just to handle params
      :content-types-accepted (fn [& _]
                                {"*/*" (fn [{params :params body :body} _]
                                         (add-name-url (params "name")
                                                       (params "location")))})
     }

)

(defrecord bookmark-handler [])
(helpers/extend-as-handler bookmark-handler behavior)

;; Server


(defonce *server* (clothesline.core/produce-server {"/:name" (bookmark-handler.)} 
                                                   {:join? false :port 9001}))


