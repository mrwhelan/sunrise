(ns sunrise.core
  (:require [clojure.string :as string :only [split join]]))

;; FIXME : give a sensible output when there is no sunrise or
;; no sunset for a given location and date

;; http://williams.best.vwh.net/sunrise_sunset_algorithm.htm

;; zenith:      Sun's zenith for sunrise/sunset
;; official     = 90 degrees 50' (90.8333 decimal)
;; civil        = 96 degrees
;; nautical     = 102 degrees
;; astronomical = 108 degrees

(def zenith 90.8333) ; using official zenith

(defn longitude-hour [longitude]
  (/ longitude 15))

;; We're calculating in "degree mode", so we'll use these functions
(defn- sin [x] (Math/sin (Math/toRadians x)))
(defn- cos [x] (Math/cos (Math/toRadians x)))
(defn- tan [x] (Math/tan (Math/toRadians x)))
(defn- asin [x] (Math/toDegrees (Math/asin x)))
(defn- acos [x] (Math/toDegrees (Math/acos x)))
(defn- atan [x] (Math/toDegrees (Math/atan x)))

;; same output as GNU date +%j
(defn- day-of-year
  [day month year]
  (let [n1 (Math/floor (/ (* 275 month) 9))
        n2 (Math/floor (/ (+ month 9) 12))
        n3 (inc (/ (Math/floor (+ (- year (* (Math/floor (/ year 4)) 4)) 2)) 3))]
    (Math/round (- (+ (- n1 (* n2 n3)) day) 30))))

(defn- rising-longitude-hour
  [longitude day-of-year]
  (let [lh (longitude-hour longitude)]
    (+ day-of-year (/ (- 6 lh) 24))))

(defn- setting-longitude-hour
  [longitude day-of-year]
  (let [lh (longitude-hour longitude)]
    (+ day-of-year (/ (- 18 lh) 24))))

(defn- sun-mean-anomaly
  "calculate sun's mean anomaly given t = rising or setting longitude hour"
  [t]
  (- (* 0.9856 t) 3.289))

(defn- sun-true-longitude
  [sun-mean-anomaly]
  (mod (+ sun-mean-anomaly
      (* 1.916 (sin sun-mean-anomaly))
      (* 0.020 (sin (* 2 sun-mean-anomaly)))
      282.634) 360))

(defn- sun-right-ascension
  [sun-true-longitude]
  (mod (atan (*
                   0.91764
                   (tan sun-true-longitude)))
       360))

(defn- right-ascension-adjusted-for-quadrant
  [sun-true-longitude sun-right-ascension]
  (let [longitude-quadrant (* 90 (Math/floor (/ sun-true-longitude 90)))
        right-ascension-quadrant (* 90 (Math/floor (/ sun-right-ascension 90)))]
    (+ sun-right-ascension
       (- longitude-quadrant
          right-ascension-quadrant))))

(defn- right-ascension->hours
  [right-ascension-adjusted-for-quadrant]
  (/ right-ascension-adjusted-for-quadrant 15))

(defn- sun-sin-declination
  [sun-true-longitude]
  (* 0.39782 (sin sun-true-longitude)))

(defn- sun-cos-declination
  [sun-sin-declination]
  (cos (asin sun-sin-declination)))

;; using zenith already defined
;; if > 1, sun won't rise on this location and date
;; if < 1, sun won't set on this location and date
(defn- sun-local-hour-angle
  [latitude sun-sin-declination sun-cos-declination]
  (/
   (- (cos zenith)
      (* sun-sin-declination (sin latitude)))
   (* sun-cos-declination (cos latitude))))

(defn- sun-local-hour-angle->hours-rising
  [sun-local-hour-angle]
  (/ (- 360 (acos sun-local-hour-angle)) 15))

(defn- sun-local-hour-angle->hours-setting
  [sun-local-hour-angle]
  (/ (acos sun-local-hour-angle) 15))

(defn- hours-rising-or-setting->local-mean-time
  [local-hours-rising-or-setting
   right-ascension-hours
   rising-or-setting-longitude-hour]
  (+
   local-hours-rising-or-setting
   right-ascension-hours
   (- (* 0.06571 rising-or-setting-longitude-hour))
   (- 6.622)))

(defn- local-mean-time->UTC
  [local-mean-time longitude-hour]
  (mod (- local-mean-time longitude-hour) 24))

(defn- UT->local-time-zone
  [UTC-time local-offset]
  (+ UTC-time local-offset))

(defn- hours->hours-and-minutes
  [hours]
  (let [[h m] (string/split (str hours) #"\.")]
    (str h ":"
         (format
          "%02d"
         (Math/round (* 60 (float (read-string (str "0." m)))))))))

(defn rising-time
  "Calculates the time for sunrise (e.g., 7:12)
   given a map that contains :day, :month, :year,
   :latitude, :longitude, and :local-offset"
  [m]
  (let [doy          (day-of-year (:day m)
                                  (:month m)
                                  (:year m))
        lh           (longitude-hour (:longitude m))
        rlh          (rising-longitude-hour (:longitude m) doy)
        sma          (sun-mean-anomaly rlh)
        stl          (sun-true-longitude sma)
        sra          (sun-right-ascension stl)
        sraq         (right-ascension-adjusted-for-quadrant stl sra)
        srah         (right-ascension->hours sraq)
        sin-dec      (sun-sin-declination stl)
        cos-dec      (sun-cos-declination sin-dec)
        slha         (sun-local-hour-angle (:latitude m) sin-dec cos-dec)
        slhahr       (sun-local-hour-angle->hours-rising slha)
        lmt          (hours-rising-or-setting->local-mean-time slhahr srah rlh)
        utc          (local-mean-time->UTC lmt lh)
        ltz          (UT->local-time-zone utc (:local-offset m))]
    (hours->hours-and-minutes ltz)))

(defn setting-time
  "Calculates the time for sunset (e.g., 17:53)
   given a map that contains :day, :month, :year,
   :latitude, :longitude, and :local-offset"
  [m]
  (let [doy          (day-of-year (:day m)
                                  (:month m)
                                  (:year m))
        lh           (longitude-hour (:longitude m))
        rlh          (setting-longitude-hour (:longitude m) doy)
        sma          (sun-mean-anomaly rlh)
        stl          (sun-true-longitude sma)
        sra          (sun-right-ascension stl)
        sraq         (right-ascension-adjusted-for-quadrant stl sra)
        srah         (right-ascension->hours sraq)
        sin-dec      (sun-sin-declination stl)
        cos-dec      (sun-cos-declination sin-dec)
        slha         (sun-local-hour-angle (:latitude m) sin-dec cos-dec)
        slhahr       (sun-local-hour-angle->hours-setting slha)
        lmt          (hours-rising-or-setting->local-mean-time slhahr srah rlh)
        utc          (local-mean-time->UTC lmt lh)
        ltz          (UT->local-time-zone utc (:local-offset m))]
    (hours->hours-and-minutes ltz)))
