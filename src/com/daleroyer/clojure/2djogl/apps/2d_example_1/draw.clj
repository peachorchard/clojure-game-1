(ns com.daleroyer.clojure.2djogl.apps.2d-example-1.draw
  (:import [com.jogamp.opengl GL GL4])
  (:import [org.joml Matrix4f])
  (:require [com.daleroyer.clojure.2djogl.jogl.2d :as two-d]))

(defn make-view-matrix []
  (.translate (new Matrix4f) 400 300 0)
  ;;(new Matrix4f)
  )

(defn make-model-matrix [gl-state scale]
  (let [now (System/currentTimeMillis)
        diff (/ (- now (:start-time gl-state)) 2000)
        translate-matrix (new Matrix4f);;(.translate (new Matrix4f) 150 50 0)
        scale-matrix (.scale (new Matrix4f) (float scale))
        rotate-z (.rotate (new Matrix4f) diff 0 0 1) ;; (/ Math/PI 20) 0 0 1) ;; 
        model-matrix (.mul translate-matrix
                           (.mul scale-matrix
                                 rotate-z
                                 (new Matrix4f))
                           (new Matrix4f))]
    model-matrix))

(def black {:red 0 :green 0 :blue 0 :alpha 1})

(defn draw [gl-state ^GL4 g4]
  (let [scale 2
        stroke-width 5
        view-matrix (make-view-matrix)
        model-matrix (make-model-matrix gl-state scale)
        graphics-state {:g4 g4
                        :resolution (:resolution gl-state)
                        :shader-programs (:shader-programs gl-state)
                        :quad-va (:quad gl-state)
                        :proj-matrix (:proj-matrix gl-state)
                        :view-matrix view-matrix
                        :model-matrix model-matrix}]
    (.glDisable g4 GL/GL_DEPTH_TEST)
    (.glBlendFunc g4 GL/GL_SRC_ALPHA GL/GL_ONE_MINUS_SRC_ALPHA)
    (.glEnable g4 GL/GL_BLEND)

    (two-d/draw-rect-xy graphics-state
                        [0 0 0] [100 100 0] stroke-width black)

    (.glDisable g4 GL/GL_BLEND)
    (.glEnable g4 GL/GL_DEPTH_TEST)))
