(ns com.daleroyer.clojure.2djogl.apps.2d-game-1.model.map
  (:require [com.daleroyer.clojure.2djogl.apps.2d-game-1.physics.object :as physics]))

(defn update-map [map _] ;;game-state]
  map)

(defn make-map [animations]
  (physics/add-physics
   {:animations animations
    :active-animation (animations "background")
    :update-fn update-map}))