(ns com.daleroyer.clojure.2djogl.apps.2d-game-1.input.keyboard
  (:require [com.daleroyer.clojure.2djogl.apps.2d-game-1.events :as events]))

(defn update-key-state [key-code value]
  (events/key-state-change key-code value))

(defn key-down [key-code]
  (update-key-state key-code true))

(defn key-up [key-code]
  (update-key-state key-code false))
