(ns clothesline.test.syntax
  (:use clojure.test
        clothesline.test.test-helpers)
  (:require [clothesline.protocol.graph-helpers :as graph-helpers]
            [clothesline.protocol.test-helpers :as  t-helpers]
            [clothesline.service :as service]))

(deftest call-on-handler 
  (let [test-fn (graph-helpers/call-on-handler service/uri-too-long?)]
    (is (= (test-fn {:handler (testing-handler :uri-too-long? true)})
           true))
    (is (= (test-fn {:handler (testing-handler :uri-too-long? false)})
           false))))

(deftest request-header-exists
  (let [example {:request {:headers {"if-unmodified-since" "june"}}}
        looking-for-t (graph-helpers/request-header-exists? "if-unmodified-since")
        looking-for-f (graph-helpers/request-header-exists? "if-modified-since")]
    (is (= (looking-for-t example) true))
    (is (= (looking-for-f example) false))))

(deftest request-header-is
  (let [example {:request {:headers {"X-Pewp" "1"}}}
        truthy1  (graph-helpers/request-header-is? "X-Pewp" "1")
        falsy1  (graph-helpers/request-header-is? "X-Pewp" "22")
        falsy2  (graph-helpers/request-header-is? "if-modified-since" "noon")]
    (is (= (truthy1 example) true))
    (is (= (falsy1 example) false))
    (is (= (falsy2 example) false))))

(deftest request-method-is
  (let [example {:request {:request-method :options}}
        truthy1 (graph-helpers/request-method-is? :options)
        falsy1  (graph-helpers/request-method-is? :put)
        falsy2  (graph-helpers/request-method-is? :meat)]
    (is (= (truthy1 example) true))
    (is (= (falsy1  example) false))
    (is (= (falsy2 {:request {}})))))

(deftest map-accept-header
  (let [def-request-html (assoc-in (make-request)
                                   [:headers "accept"]
                                   "text/html")
        def-request-xml  (assoc-in (make-request)
                                   [:headers "accept"]
                                   "text/xml,text/html")
        def-request-crd  (assoc-in (make-request)
                                   [:headers "accept"]
                                   "garbage/larbage")
        k1   (constantly "k1")
        k2   (constantly "k2")
        map1 {"text/html" k1}
        map2 {"text/html" k1
              "text/xml"  k2}
        map* {"*/*"       k1}]
    (is (= ["text/html" k1] (t-helpers/map-accept-header def-request-xml "accept" map1)))
    (is (nil? (t-helpers/map-accept-header def-request-crd "accept" map1 false)))
    (is (= ["text/html" k1] (t-helpers/map-accept-header def-request-crd "accept" map1)))
    (is (= ["text/html" k1] (t-helpers/map-accept-header def-request-html "accept" map2)))
    (is (= ["text/xml" k2] (t-helpers/map-accept-header def-request-xml "accept" map2)))
    (is (= ["*/*"      k1] (t-helpers/map-accept-header def-request-xml "accept" map*)))))

