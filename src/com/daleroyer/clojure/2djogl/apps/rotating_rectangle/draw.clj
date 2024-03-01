(ns com.daleroyer.clojure.2djogl.apps.rotating-rectangle.draw
  (:import [com.jogamp.opengl GL4])
  (:import [org.joml Matrix4f])
  (:require [com.daleroyer.clojure.2djogl.jogl.2d :as two-d]))

(defn make-view-matrix []
    (new Matrix4f))

(defn make-model-matrix [gl-state]
  (let [now (System/currentTimeMillis)
        diff (/ (- now (:start-time gl-state)) 1000)
        scale (.scale (new Matrix4f) 0.5)
        rotate-z (.setRotationXYZ (new Matrix4f) 0 0 diff)
        model-matrix (.mul scale rotate-z)]
    model-matrix))

(defn draw [gl-state ^GL4 g4]
  (let [view-matrix (make-view-matrix)
        model-matrix (make-model-matrix gl-state)
        graphics-state {:g4 g4
                        :resolution (:resolution gl-state)
                        :shader-programs (:shader-programs gl-state)
                        :quad-va (:quad gl-state)
                        :proj-matrix (:proj-matrix gl-state)
                        :view-matrix view-matrix
                        :model-matrix model-matrix}]
    (two-d/draw-filled-quad-xy graphics-state
                               [0 0] [1 1] 2 {:red 0
                                              :green 0
                                              :blue 1
                                              :alpha 1})))
