(ns clothesline.test.response_helpers
  (:use [clojure.test]
        [clothesline.protocol.response-helpers]))



(deftest stop-response-tests
  (testing "stop-response"
    (let [ecode 500
          ebody "Whee"
          eheads {:earth "air" :water "fire"}
          sgraphdata {:graphdata {:body "NoFun" :headers {:darkness "light"}}}]
      (is (let [resp ((stop-response ecode eheads ebody) sgraphdata)]
            (and (= (:body resp) ebody)
                 (= (-> resp :headers :earth) "air")
                 (= (:status ecode))))
          "should not propagate values in the long form.")
      (is (let [resp ((stop-response ecode eheads) sgraphdata)]
            (and (= (:body resp) "NoFun")
                 (= (-> resp :headers :earth) "air")
                 (= (:status ecode))))
          "should propagate the values in 2-arity form.")
      (is (let [resp ((stop-response ecode) sgraphdata)]
            (and (= (:body resp) "NoFun")
                 (= (-> resp :headers :darkness) "light")
                 (= (:status ecode))))
          "should propagate the body AND headers in 1-arity form."))))
         
