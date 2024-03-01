(ns com.daleroyer.clojure.2djogl.apps.2d-game-1.opengl.draw
  (:import [com.jogamp.opengl GL GL4])
  (:import [org.joml Matrix4f])
  (:require [com.daleroyer.clojure.2djogl.jogl.2d :as two-d]
            [com.daleroyer.clojure.2djogl.apps.2d-game-1.physics.object :as physics])
  (:require [com.daleroyer.clojure.2djogl.apps.2d-game-1.model.camera :as camera]))

(defn draw-path-shape [graphics-state
                       pixel-width rgba s]
  (when (= :line (:shape s))
    (two-d/draw-line graphics-state
                     (conj (:start s) 0) (conj (:end s) 0) pixel-width rgba)))

(defn solid? [shape]
  (some #(= :solid %) (:properties shape)))

(defn draw-shapes [graphics-state shapes]
  (let [^Matrix4f view-matrix-in (:view-matrix graphics-state)]
    (doseq [frame-shape shapes]
      (let [s (:shape frame-shape)
            ^Matrix4f frame-transform (:transform frame-shape)
            ^Matrix4f shape-transform (:transform s)
            view-matrix view-matrix-in
            model-matrix (.mul frame-transform
                               shape-transform
                               (new Matrix4f))
            gs (assoc graphics-state
                      :view-matrix view-matrix
                      :model-matrix model-matrix)
            stroke-color (:stroke-rgba s)
            fill-color (:fill-rgba s)]
        (case (:shape s)
          :circle (two-d/draw-circle gs
                                     [(:center-x s) (:center-y s)]
                                     (:radius s)
                                     (:stroke-width s)
                                     stroke-color
                                     fill-color)

          :ellipse (two-d/draw-ellipse gs
                                       [(:center-x s) (:center-y s)]
                                       [(:radius-x s)
                                        (:radius-y s)]
                                       (:stroke-width s)
                                       stroke-color
                                       fill-color)

          :rect (two-d/draw-filled-rect-xy gs
                                           [(:x s) (:y s)]
                                           [(+ (:x s) (:width s)) (+ (:y s) (:height s))]
                                           (:stroke-width s)
                                           stroke-color
                                           fill-color)
          :line (two-d/draw-line gs
                                 (:start s)
                                 (:end s)
                                 (:stroke-width s)
                                 stroke-color)
          :triangle (two-d/draw-filled-triangle-xy gs
                                                   (:a s)
                                                   (:b s)
                                                   (:c s)
                                                   stroke-color)
          (println "unknown shape:" (:shape s) "in" s))))))

(defn draw [gl-state ^GL4 g4 app-state]
  (let [game-state (:game-state @app-state)]

    (.glDisable g4 GL/GL_DEPTH_TEST)
    (.glBlendFunc g4 GL/GL_SRC_ALPHA GL/GL_ONE_MINUS_SRC_ALPHA)
    (.glEnable g4 GL/GL_BLEND)

    (let [resolution (:resolution gl-state)
          global-bounds {:x1 0 :y1 0 :x2 1920 :y2 1080}
          focus-bounds (physics/sweep-object-aabb
                        (first (:player-objects game-state))
                        (:time game-state))
          view-matrix (camera/get-view-matrix resolution global-bounds focus-bounds)
          graphics-state {:g4 g4
                          :shader-programs (:shader-programs gl-state)
                          :quad-va (:quad gl-state)
                          :resolution resolution
                          :proj-matrix (:proj-matrix gl-state)
                          :view-matrix view-matrix}]
      (draw-shapes graphics-state (:active-shapes game-state)))
    
    (.glDisable g4 GL/GL_BLEND)
    (.glEnable g4 GL/GL_DEPTH_TEST)))
