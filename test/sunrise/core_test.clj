(ns sunrise.core-test
  (:require [clojure.test :refer :all]
            [sunrise.core :refer :all]))

(deftest test-day-of-year
  (testing "day of year calculation"
    (is (= (day-of-year 26 10 2013) 299))))
