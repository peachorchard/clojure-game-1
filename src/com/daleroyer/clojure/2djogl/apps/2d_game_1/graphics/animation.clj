(ns com.daleroyer.clojure.2djogl.apps.2d-game-1.graphics.animation
  (:import [org.joml Matrix4f])
  (:require [com.daleroyer.clojure.2djogl.apps.2d-game-1.physics.object :as physics]))

(defn current-animation-frame [animation absolute-time]
  (let [all-frames (:frames animation)]
    (when (and all-frames (> (count all-frames) 0))
      (let [fps 8
            frame-number (inc (mod (int (* fps absolute-time)) (count all-frames)))
            frame (all-frames frame-number)]
        frame))))

(defn update-animation-objects [game-state]
  (let [time (:time game-state)
        current-time (:current-time time)
        start-time (:start-time time)
        absolute-time (- current-time start-time)]
    (assoc game-state
           :objects
           (remove nil?
                   (mapv (fn [o]
                           (assoc o
                                  :active-frame (current-animation-frame (:active-animation o)
                                                                         absolute-time)))
                         (:objects game-state))))))

(defn update-animations [game-state]
  (update-animation-objects game-state))

(defn update-active-shapes [game-state]
  (assoc game-state
         :active-shapes
         (apply concat (mapv (fn [o]
                               (let [f (:active-frame o)]
                                 (mapv (fn [s]
                                         {:shape s
                                          :transform (.mul
                                                      ^Matrix4f (physics/get-transform o)
                                                      ^Matrix4f (:transform f)
                                                      (new Matrix4f))})
                                       (:shapes f))))
                             (:objects game-state)))))
