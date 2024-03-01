(ns com.daleroyer.clojure.entry
  (:gen-class)
  (:require [com.daleroyer.clojure.fatjar.app :as fat-jar])
  (:require [com.daleroyer.clojure.2djogl.apps.rotating-rectangle.app :as rotating-rectangle])
  (:require [com.daleroyer.clojure.2djogl.apps.2d-example-1.app :as two-d-example-1])
  (:require [com.daleroyer.clojure.2djogl.apps.2d-game-1.app :as two-d-game-1]))

(defn -main [& args]
  (case (first args)
    "1" (apply rotating-rectangle/app (rest args))
    "2" (apply two-d-example-1/app (rest args))
    "fat-jar" (fat-jar/app)
    (apply two-d-game-1/app (rest args))))
