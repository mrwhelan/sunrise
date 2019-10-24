# sunrise

A simple Clojure library for getting sunrise and sunset times,
given a location, date, and local offset from UTC.

## Usage Examples

```clojure
(use 'sunrise.core)

;; Sunset in Paris
(setting-time {:day 1
               :month 5
               :year 2015
               :latitude 48.8567
               :longitude 2.3508
               :local-offset 1})
;; -> "20:04"

;; Sunrise in Carrboro, NC
(rising-time {:day 25
              :month 12
              :year 2014
              :latitude 35.9206
              :longitude -79.0839
              :local-offset -5})
;; -> "7:25"

;; Sunrise In Norilsk
(rising-time {:day 1
              :month 1
              :year 2015
              :latitude 69.3333
              :longitude 88.2167
              :local-offset 7})
;; -> "continuous dark"

;; Sunset at McMurdo Station
(setting-time {:day 1
               :month 1
               :year 2015
               :latitude -77.85
               :longitude 166.6667
               :local-offset 13})
;; -> "continuous light"
```

As of this writing it is easy to get the latitude and longitude of a town or city by
Googling, e.g., "New York City latitude longitude".
Similarly, you can usually find the UTC offset for a location by Googling
the location with "time" (or "UTC offset").

Note: For this library, West longitudes and South latitudes should be preceded by minus (-) signs.

## Credits

The algorithm was derived from the Almanac for Computers, 1990,
published by Nautical Almanac Office, United States Naval Observatory,
Washington, DC 20392, and is described in detail at
<a href="http://williams.best.vwh.net/sunrise_sunset_algorithm.htm">
http://williams.best.vwh.net/sunrise_sunset_algorithm.htm</a>

## License

Copyright © 2014 Mark Whelan

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
