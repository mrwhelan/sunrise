(ns sunrise.core)

;; http://williams.best.vwh.net/sunrise_sunset_algorithm.htm

;; zenith: Sun's zenith for sunrise/sunset
;; offical      = 90 degrees 50'
;; civil        = 96 degrees
;; nautical     = 102 degrees
;; astronomical = 108 degrees

(defn degrees->radians [point]
  (mapv #(Math/toRadians %) point))

(defn radians->degrees [point]
  (mapv #(Math/toDegrees %) point))

(defn day-of-year
  [day month year]
  (let [n1 (int (/ (* 275 month) 9))
        n2 (int (/ (+ month 9) 12))
        n3 (inc (/ (int (+ (- year (* (int (/ year 4)) 4)) 2)) 3))]
    (- (+ (- n1 (* n2 n3)) day) 30)))

(defn rising-longitude-hour
  [longitude day-of-year]
  (let [longitude-hour (/ longitude 15)]
    (+ day-of-year (/ (- 6 longitude-hour) 24))))

(defn setting-longitude-hour
  [longitude day-of-year]
  (let [longitude-hour (/ longitude 15)]
    (+ day-of-year (/ (- 18 longitude-hour) 24))))

(defn sun-mean-anomaly
  "calculate sun's mean anomaly given t = rising or setting longitude hour"
  [t]
  (- (* 0.9856 t) 3.289))

(defn sun-true-longitude
  [sun-mean-anomaly]
  (mod (+ sun-mean-anomaly
      (* 1.916 (Math/sin sun-mean-anomaly))
      (* 0.020 (Math/sin (* 2 sun-mean-anomaly)))
      282.634) 360))

(defn sun-right-ascension
  [sun-true-longitude]
  (mod (Math/atan (*
                   0.91764
                   (Math/tan sun-true-longitude)))
       360))

(defn right-ascension-adjusted-for-quadrant
  [sun-true-longitude sun-right-ascension]
  (let [longitude-quadrant (* 90 (int (/ sun-true-longitude 90)))
        right-ascension-quadrant (* 90 (int (/ sun-right-ascension 90)))]
    (+ sun-right-ascension
       (- longitude-quadrant
          right-ascension-quadrant))))

(defn right-ascension-converted-to-hours
  [right-ascension-adjusted-for-quadrant]
  (/ right-ascension-adjusted-for-quadrant 15))

(defn sun-sin-declination
  [sun-true-longitude]
  (* 0.39782 (Math/sin sun-true-longitude)))

(defn sun-cos-declination
  [sun-sin-declination]
  (Math/cos (Math/asin sun-sin-declination)))

(defn sun-local-hour-angle
  [])
