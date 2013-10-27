(ns sunrise.core)

;;; http://williams.best.vwh.net/sunrise_sunset_algorithm.htm

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
