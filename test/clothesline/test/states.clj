(ns clothesline.test.states
  (:use
   clothesline.protocol.test-helpers
   clothesline.test.test-helpers
   clojure.test))


(defn annotation-preserving? [state]
     (let [tok (gensym)
           {:keys [forward-args]} (test-state state :graphdata {:token tok})]
       (= tok (get-in forward-args [:graphdata :token]))))

(defn state-res-linked-to-handler-method? [state handler-keyword]
  (let [tst #(getres (:result (test-state state :handler %)))]
    (is (= (tst (testing-handler handler-keyword false)) false))
    (is (= (tst (testing-handler handler-keyword true)) true))))

(deftest b13
  (is (state-res-linked-to-handler-method? 'b13 :service-available?))
  (is (annotation-preserving? 'b13)))

;; These two states are largely ignored
(deftest b12
  (is (annotation-preserving? 'b12)))

(deftest b11
  (is (state-res-linked-to-handler-method? 'b11 :uri-too-long?))
  (is (annotation-preserving? 'b11)))


