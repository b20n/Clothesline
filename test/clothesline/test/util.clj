(ns clothesline.test.util
  (:use [clojure.test]
        [clj-time [format :only [parse unparse]]
                  [core   :only [date-time]]])
  (:require [clothesline.util :as util]))




(deftest date-parse-tests
  (testing "date-timezone-from-string"
    (is (nil? (util/date-timezone-from-string "ham & cheese sammy"))
        "should not exception on invalid input.")
    (is (nil? (util/date-timezone-from-string "Mon, 05 Jul 2010 01:21:00"))
        "should demand time zones.")
    (is (= [(date-time 2010 7 5 1 21 0) "PST"]
             (util/date-timezone-from-string "Mon, 05 Jul 2010 01:21:00 PST"))
        "should parse 1123 dates.")
    (is (= [(date-time 1985 10 26 1 22 0) "PST"]
             (util/date-timezone-from-string "Sat, 26-Oct-85 01:22:00 PST"))
        "should parse 1036 dates")
    (is (= [(date-time 1985 10 26 1 20 0) "UTC"]
             (util/date-timezone-from-string "Sat Oct 26 01:20:00 1985"))
        "should parse ctime dates (sort of)"))
  )