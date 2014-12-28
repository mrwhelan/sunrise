(ns sunrise.core
  (:require [clojure.string :as string :only [split join]]))

;; algorithm source:
;; http://williams.best.vwh.net/sunrise_sunset_algorithm.htm

;; zenith:      Sun's zenith for sunrise/sunset
;; official     = 90 degrees 50' (90.8333 decimal)
;; civil        = 96 degrees
;; nautical     = 102 degrees
;; astronomical = 108 degrees

(def zenith 90.8333) ; using official zenith

;; We're calculating in "degree mode", so we'll use these functions
(defn- sin [x] (Math/sin (Math/toRadians x)))
(defn- cos [x] (Math/cos (Math/toRadians x)))
(defn- tan [x] (Math/tan (Math/toRadians x)))
(defn- asin [x] (Math/toDegrees (Math/asin x)))
(defn- acos [x] (Math/toDegrees (Math/acos x)))
(defn- atan [x] (Math/toDegrees (Math/atan x)))


(defn- day-of-year
  "Calculates the day of the year given a month, day, and year."
  [day month year]
  (let [n1 (Math/floor (/ (* 275 month) 9))
        n2 (Math/floor (/ (+ month 9) 12))
        n3 (inc (/ (Math/floor (+ (- year (* (Math/floor (/ year 4)) 4)) 2)) 3))]
    (Math/round (- (+ (- n1 (* n2 n3)) day) 30))))

(defn- longitude-hour [longitude]
  (/ longitude 15))

(defn- approximate-time
  [longitude day-of-year rising-or-setting]
  (let [lh (longitude-hour longitude)]
    (if (= rising-or-setting :rising)
      (+ day-of-year (/ (- 6 lh) 24))
      (+ day-of-year (/ (- 18 lh) 24))))) ; else setting

(defn- sun-mean-anomaly
  [approximate-time]
  (- (* 0.9856 approximate-time) 3.289))

(defn- sun-true-longitude
  [sun-mean-anomaly]
  (mod (+ sun-mean-anomaly
      (* 1.916 (sin sun-mean-anomaly))
      (* 0.020 (sin (* 2 sun-mean-anomaly)))
      282.634) 360))

(defn- sun-right-ascension-unadjusted
  [sun-true-longitude]
  (mod (atan (*
                   0.91764
                   (tan sun-true-longitude)))
       360))

(defn- right-ascension-adjusted-for-quadrant
  [sun-true-longitude sun-right-ascension-unadjusted]
  (let [longitude-quadrant (* 90 (Math/floor (/ sun-true-longitude 90)))
        right-ascension-quadrant
        (* 90 (Math/floor (/ sun-right-ascension-unadjusted 90)))]
    (+ sun-right-ascension-unadjusted
       (- longitude-quadrant
          right-ascension-quadrant))))

(defn- right-ascension->hours
  [right-ascension-adjusted-for-quadrant]
  (/ right-ascension-adjusted-for-quadrant 15))

(defn- sun-right-ascension
  [sun-true-longitude]
  (->> sun-true-longitude
      (sun-right-ascension-unadjusted)
      (right-ascension-adjusted-for-quadrant sun-true-longitude)
      (right-ascension->hours)))

(defn- sun-sin-declination
  [sun-true-longitude]
  (* 0.39782 (sin sun-true-longitude)))

(defn- sun-cos-declination
  [sun-sin-declination]
  (cos (asin sun-sin-declination)))

;; Using zenith defined above.
;; If > 1, sun won't rise on this location and date.
;; If < -1, sun won't set on this location and date.
(defn- sun-local-hour-angle
  [latitude sun-sin-declination sun-cos-declination]
  (/
              (- (cos zenith)
                 (* sun-sin-declination (sin latitude)))
              (* sun-cos-declination (cos latitude))))

(defn- sun-local-hour-angle->hours
  [rising-or-setting sun-local-hour-angle]
  (if (= rising-or-setting :rising)
    (/ (- 360 (acos sun-local-hour-angle)) 15)
    (/ (acos sun-local-hour-angle) 15)))

(defn- hours-rising-or-setting->local-mean-time
  [local-hours-rising-or-setting
   sun-right-ascension
   approximate-time]
  (+
   local-hours-rising-or-setting
   sun-right-ascension
   (- (* 0.06571 approximate-time))
   (- 6.622)))

(defn- local-mean-time->UTC
  [local-mean-time longitude-hour]
  (- local-mean-time longitude-hour))

;; Change from original algorithm:
;; Do mod 24 adjustment after conversion to local time instead of before,
;; to correct for occasional errors in end-result time (e.g. 28:00 will
;; be correctly output as 4:00)
(defn- UT->local-time-zone
  [UTC-time local-offset]
  (mod (+ UTC-time local-offset) 24))

(defn- hours->hours-and-minutes
  [hours]
  (let [[h m] (string/split (str hours) #"\.")]
    (str h ":"
         (format
          "%02d"
         (Math/round (* 60 (float (read-string (str "0." m)))))))))

(defn- event-time
  [m rising-or-setting]
  (let [doy (day-of-year (:day m)
                         (:month m)
                         (:year m))
        lh (longitude-hour (:longitude m))
        at (approximate-time (:longitude m) doy rising-or-setting)
        stl (-> at
                (sun-mean-anomaly)
                (sun-true-longitude))
        sra (sun-right-ascension stl)
        sin-dec (sun-sin-declination stl)
        cos-dec (sun-cos-declination sin-dec)
        slha (sun-local-hour-angle (:latitude m) sin-dec cos-dec)]
    (cond (> slha 1) "continuous dark"
          (< slha -1) "continuous light"
          :else
          (let [slhah (sun-local-hour-angle->hours rising-or-setting slha)
                lt (-> (hours-rising-or-setting->local-mean-time slhah sra at)
                       (local-mean-time->UTC lh)
                       (UT->local-time-zone (:local-offset m)))]
            (hours->hours-and-minutes lt)))))

(defn rising-time
  "Calculates the time for sunrise (e.g., 7:12)
   given a map that contains :day, :month, :year,
   :latitude, :longitude, and :local-offset"
  [m]
  (event-time m :rising))

(defn setting-time
  "Calculates the time for sunset (e.g., 17:53)
   given a map that contains :day, :month, :year,
   :latitude, :longitude, and :local-offset"
  [m]
  (event-time m :setting))
