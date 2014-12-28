(ns sunrise.core-test
  (:require [clojure.test :refer :all]
            [sunrise.core :refer :all]))

(def carrboro-jan  {:day 1
                    :month 1
                    :year 2015
                    :latitude 35.9206
                    :longitude -79.0839
                    :local-offset -5}) ; rising 7:27, setting 17:13

(def sydney-jan {:day 1
                 :month 1
                 :year 2015
                 :latitude -33.86
                 :longitude 151.2094
                 :local-offset 10}) ; rising 4:47, setting 19:09

(def mcmurdo-jan {:day 1
                  :month 1
                  :year 2015
                  :latitude -77.85
                  :longitude 166.6667
                  :local-offset 13}) ; -> "continuous light"

(def norilsk-jan {:day 1
              :month 1
              :year 2015
              :latitude 69.3333
              :longitude 88.2167
              :local-offset 7}) ; -> "continuous dark"

;; compare to GNU date +%j :
;; (deftest test-day-of-year
;;   (testing "day of year calculation"
;;     (is (= (day-of-year 26 10 2013) 299))))

;; compare to output of http://www.esrl.noaa.gov/gmd/grad/solcalc/ :

(deftest test-rising-time
  (testing "rising time calculation"
    (is (= (rising-time carrboro-jan) "7:27"))
    (is (= (rising-time sydney-jan) "4:47"))
    (is (= (rising-time mcmurdo-jan) "continuous light"))
    (is (= (rising-time norilsk-jan) "continuous dark"))))

(deftest test-setting-time
  (testing "setting time calculation"
    (is (= (setting-time carrboro-jan) "17:13"))
    (is (= (setting-time sydney-jan) "19:09"))
    (is (= (setting-time mcmurdo-jan) "continuous light"))
    (is (= (setting-time norilsk-jan) "continuous dark"))))
