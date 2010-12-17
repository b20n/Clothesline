(ns clothesline.test.states
  (:use
   clothesline.protocol.test-helpers
   clothesline.test.test-helpers
   clojure.test))


(defn annotation-preserving? [state handler-keyword]
  (let [tok (gensym)
        generative-handler (testing-handler handler-keyword
                                            (annotated-return true
                                                              {:annotate {:token tok}}))
        {:keys [forward-args]} (test-state state :handler generative-handler)]
    (is (= tok (get-in forward-args [:graphdata :token])) "preserve annotation")))



(defn state-res-linked-to-handler-method? [state handler-keyword]
  (let [tst #(getres (:result (test-state state :handler %)))]
    (testing "have their result determined solely by a handler call"
      (is (= (tst (testing-handler handler-keyword false)) false))
      (is (= (tst (testing-handler handler-keyword true)) true)))))



                                        ; Non trivial states
                                        ; b3, c3, c4, d4, e5,
                                        ; e6, f6, f7, g8, g9,
                                        ; g9, g11, h10, h11,
                                        ; h12, i12, i13, k13,
                                        ; j18, l13, l14, l15,
                                        ; l17, m16, o20, n16,
                                        ; o16, o14, i17, l7,
                                        ; m5

                                        ; Special cases
                                        ; l4, k5, l5

(def trivial-linked-states
     (hash-map
      'b13 :service-available?
      'b11 :uri-too-long?
      'b9  :malformed-request?
      'b8  :authorized?
      'b7  :forbidden?
      'b6  :valid-content-headers?
      'b5  :known-content-type?
      'b4  :valid-entity-length?
      'g7  :resource-exists?
      'm20 :delete-resource
      'o18 :multiple-choices?
      'k7 :previously-existed?
      'k5 :moved-permanently? ; Tricky example
      'i4 :moved-permanently? ; tricky example
      'l5 :moved-temporarily? ; Tricky example
      'm7 :allow-missing-post?
      'n5 :allow-missing-post?))

(deftest trivial-state-correctness
  (testing "Trivially complex states should "
    (doseq [[state-sym protocol-sym] trivial-linked-states]
      (state-res-linked-to-handler-method? state-sym protocol-sym)
      (annotation-preserving? state-sym protocol-sym))))

(deftest b10-set-accepting
  (is (= true (boolean (:result (test-state 'b10
                                            :request {:request-method :get})))))
  (is  (= true (boolean (:result (test-state 'b10
                                             :request {:request-method :options})))))
  (is (= true (boolean (:result (test-state 'b10
                                            :request {:request-method :options}
                                            :handler (testing-handler :allowed-methods #{:options :get})))))))



(deftest g11-matches-properly
  (let [etagzo (fn [etag] (testing-handler :generate-etag etag))
        hdr1   {:headers {"if-match" "123456"}}
        hdr2   {:headers {"if-match" "abc123"}}]
    (testing "g11 should match etags to header values"
      (is (= true (boolean (getres (:result (test-state 'g11 :request hdr1
                                                        :handler (testing-handler :generate-etag "123456")))))))
      (is (= false (boolean (getres (:result (test-state 'g11 :request hdr2
                                                         :handler (testing-handler :generate-etag "123456"))))))))))




