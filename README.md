# sunrise

A simple Clojure library for getting sunrise and sunset times,
given a location (latitude and longitude), date, and offset from
UTC.

## Example Usage

```clojure
(use 'sunrise.core))

(rising-time {:day 25
              :month 12
              :year 2014
              :latitude 35.9206
              :longitude -79.0839
              :local-offset -5})
;; -> "7:25"

(setting-time {:day 1
               :month 1
               :year 2015
               :latitude 35.9206
               :longitude -79.0839
               :local-offset -5})
;; -> "17:13"
```

Note: West longitudes and South latitudes should be preceded by minus (-) signs.

## License

Copyright © 2014 Mark Whelan

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
