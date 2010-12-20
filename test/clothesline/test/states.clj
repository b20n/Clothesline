(ns clothesline.test.states
  (:use
   [clj-time.core :only [now date-time]]
   clothesline.protocol.test-helpers
   clothesline.test.test-helpers
   clojure.test))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Helpers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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


(defn produces-graphdata-entry? [state-sym graphdata-key & {:as mods}]
  (let [d-args {:request (make-request)
                :handler default-handler
                :graphdata {:headers {}}}
        m-args (merge d-args mods)
        result (apply test-state (list* state-sym (-> m-args seq flatten)))
        ndata  (:ndata result)]
    (is (not (nil? (get ndata graphdata-key))))))

(defn produces-headers-entry? [state-sym ^string header-key & {:as mods}]
  (let [d-args {:request (make-request)
                :handler default-handler
                :graphdata {:headers {}}}
        m-args (merge d-args mods)
        result (apply test-state (list* state-sym (-> m-args seq flatten)))
        headers  (-> result :ndata :headers)]
    (is (not (nil? (get headers header-key))))))



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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; State tests
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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



(deftest h11-extracts-dates
  (testing "h11 should extract dates into the graphdata"
    (let [req (make-request :headers {"if-unmodified-since" "Mon, 05 Jul 2010 01:21:00 UTC"})]
      (produces-graphdata-entry? 'h11 :if-unmodified-since :request req))))

(deftest h12-logic
  (testing "h12 should"
    (let [hdlr (testing-handler :last-modified (now))
          req (make-request)]
      (is (= false (getres (:result (test-state 'h12 :request req
                                                     :handler hdlr
                                                     :graphdata {:if-unmodified-since (date-time 3050)}))))
          "return false for new date headers")
      (is (= true  (getres (:result (test-state 'h12 :request req
                                                     :handler hdlr
                                                     :graphdata {:if-unmodified-since (date-time 1950)}))))
          "return true for old date headers"))))
    
  

(deftest k13-matches-properly
  (let [handler  (testing-handler :generate-etag "123456")
        hdr1     {:headers {"if-none-match" "123456"}}
        hdr2     {:headers {"if-none-match" "abc123"}}]
    (testing "k13 should match etags to header values"
      (is (= true (boolean (getres (:result (test-state 'k13
                                                        :request hdr1
                                                        :handler handler))))))
      (is (= false (boolean (getres (:result (test-state 'k13
                                                         :request hdr2
                                                         :handler handler)))))))))


(deftest l14-extracts-dates
  (testing "l14 should extract dates into the graphdata"
    (let [req (make-request :headers {"if-modified-since" "Mon, 05 Jul 2010 01:21:00 UTC"})]
      (produces-graphdata-entry? 'l14 :if-modified-since :request req))))

(deftest l17-logic
  (testing "l17 should"
    (let [hdlr (testing-handler :last-modified (now))
          req (make-request)]
      (is (= false (getres (:result (test-state 'l17 :request req
                                                     :handler hdlr
                                                     :graphdata {:if-modified-since (date-time 3050)}))))
          "return false for new date headers")
      (is (= true  (getres (:result (test-state 'l17 :request req
                                                     :handler hdlr
                                                     :graphdata {:if-modified-since (date-time 1950)}))))
          "return true for old date headers"))))

(deftest o14-placeholder
  (testing "state o14"
    (testing "preserves annotation."
      (annotation-preserving? 'o14 :conflict?))
    (testing "return true with service/conflict is true."
      (is (= true (getres (:result (test-state 'o14
                                               :handler (testing-handler :conflict? true)))))))
    (testing "return 415 if not service/confict is false AND there IS NO salient handler"
      (let [handler (testing-handler :conflict?              false
                                     :content-types-accepted {})
            r (test-state 'o14 :handler handler)]
        (is (= 415 (:status r)))))
    (testing "returns false if not service/conflict and there IS salient handler."
      (let [handler (testing-handler :conflict?              false
                                     :content-types-accepted {"*/*" (constantly "meat")})
            r (test-state 'o14 :handler handler)]
        (is (= false (getres (:result r))))))
    (testing "creates appropriate graphdata entries on false result."
      (let [content-type "text/plain"
            content-generator (constantly "meat")
            handler (testing-handler :conflict?              false
                                     :content-types-accepted {content-type content-generator})
            gd      {:content-type "text/plain",
                     :headers      {"Content-Type" "text/plain"}}
            r       (test-state 'o14 :handler handler
                                     :graphdata gd)
            result  (:result r)
            ann     (:ndata r)]
        (is (= false (getres result)))
        (is (= content-type (:content-type ann)))
        (is (= content-generator (:content-provider ann)))))))


(deftest o20-selection
  (testing "o20 should"
    (let [gd1 {:body "testbody"}
          gd2 {:some "otherkeys"}]
      (is (boolean (:result (test-state 'o20 :graphdata gd1))) "return true if there is a body defined.")
      (is (not (boolean (:result (test-state 'o20 :graphdata gd2)))) "return false if there is no body."))))

(deftest k5-i4-anno
  (testing "k5 & i4 should create location ehader"
    (let [handler (testing-handler :moved-permanently? "http://google.com")]
      (produces-headers-entry? 'k5 "Location" :handler handler)
      (produces-headers-entry? 'i4 "Location" :handler handler))))

(deftest l5-anno
  (testing "l5 should create location header"
    (let [handler (testing-handler :moved-temporarily? "http://google.com")]
      (produces-headers-entry? 'l5 "Location" :handler handler))))