(ns com.daleroyer.clojure.2djogl.jogl.2d
  (:import [org.joml Matrix4f])
  (:require [com.daleroyer.clojure.svg.svg :as svg])
  (:require [com.daleroyer.clojure.2djogl.jogl.jogl :as jogl]
            [com.daleroyer.clojure.2djogl.shaders.defs :as shader-defs])
  (:require [com.daleroyer.clojure.2djogl.jogl.uniforms :as uniforms]))

(defn draw-line [graphics-state start end pixel-width rgba]
  (let [g4 (:g4 graphics-state)
        resolution (:resolution graphics-state)
        program (:line-2d (:shader-programs graphics-state))]
    (uniforms/set-proj-matrix g4 program (:proj-matrix graphics-state))
    (uniforms/set-view-matrix g4 program (:view-matrix graphics-state))
    (uniforms/set-model-matrix g4 program (:model-matrix graphics-state))
    (uniforms/update-vec4 g4 program (:resolution shader-defs/Locations)
                          (first resolution) (second resolution) 0 0)
    (uniforms/update-vec4 g4 program (:start-point shader-defs/Locations)
                          (nth start 0) (nth start 1) 0 1)
    (uniforms/update-vec4 g4 program (:end-point shader-defs/Locations)
                          (nth end 0) (nth end 1) 0 1)
    (uniforms/update-float g4 program (:line-width shader-defs/Locations)
                           pixel-width)
    (uniforms/update-vec4 g4 program (:stroke-color shader-defs/Locations)
                          (:red rgba) (:green rgba) (:blue rgba) (:alpha rgba))
    (jogl/draw-vertex-array-with-program g4 (:quad-va graphics-state) program)))

(defn draw-circle [graphics-state
                   center radius pixel-width stroke-rgba fill-rgba]
  (let [g4 (:g4 graphics-state)
        resolution (:resolution graphics-state)
        program (:circle-2d (:shader-programs graphics-state))]
    (uniforms/set-proj-matrix g4 program (:proj-matrix graphics-state))
    (uniforms/set-view-matrix g4 program (:view-matrix graphics-state))
    (uniforms/set-model-matrix g4 program (:model-matrix graphics-state))
    (uniforms/update-vec4 g4 program (:resolution shader-defs/Locations)
                          (first resolution) (second resolution) 0 0)
    (uniforms/update-vec4 g4 program (:center shader-defs/Locations)
                          (nth center 0) (nth center 1) 0 0)
    (uniforms/update-float g4 program (:radii shader-defs/Locations)
                           radius)
    (uniforms/update-float g4 program (:line-width shader-defs/Locations)
                           pixel-width)
    (uniforms/update-vec4 g4 program (:stroke-color shader-defs/Locations)
                          (:red stroke-rgba)
                          (:green stroke-rgba)
                          (:blue stroke-rgba)
                          (:alpha stroke-rgba))
    (uniforms/update-vec4 g4 program (:fill-color shader-defs/Locations)
                          (:red fill-rgba)
                          (:green fill-rgba)
                          (:blue fill-rgba)
                          (:alpha fill-rgba))
    (jogl/draw-vertex-array-with-program g4 (:quad-va graphics-state) program)))

(defn draw-ellipse [graphics-state
                    center radii pixel-width stroke-rgba fill-rgba]
  (let [g4 (:g4 graphics-state)
        resolution (:resolution graphics-state)
        program (:ellipse-2d (:shader-programs graphics-state))]
    (uniforms/set-proj-matrix g4 program (:proj-matrix graphics-state))
    (uniforms/set-view-matrix g4 program (:view-matrix graphics-state))
    (uniforms/set-model-matrix g4 program (:model-matrix graphics-state))
    (uniforms/update-vec4 g4 program (:resolution shader-defs/Locations)
                          (first resolution) (second resolution) 0 0)
    (uniforms/update-vec4 g4 program (:center shader-defs/Locations)
                          (nth center 0) (nth center 1) 0 0)
    (uniforms/update-vec4 g4 program (:radii shader-defs/Locations)
                          (nth radii 0) (nth radii 1) 0 0)
    (uniforms/update-float g4 program (:line-width shader-defs/Locations)
                           pixel-width)
    (uniforms/update-vec4 g4 program (:stroke-color shader-defs/Locations)
                          (:red stroke-rgba)
                          (:green stroke-rgba)
                          (:blue stroke-rgba)
                          (:alpha stroke-rgba))
    (uniforms/update-vec4 g4 program (:fill-color shader-defs/Locations)
                          (:red fill-rgba)
                          (:green fill-rgba)
                          (:blue fill-rgba)
                          (:alpha fill-rgba))
    (jogl/draw-vertex-array-with-program g4 (:quad-va graphics-state) program)))

(defn draw-filled-quad-xy [graphics-state
                           corner-a corner-b pixel-width fill-rgba]
  (let [g4 (:g4 graphics-state)
        half-pixel-width (/ pixel-width 2)
        ax (min (first corner-a) (first corner-b))
        ay (min (second corner-a) (second corner-b))
        bx (max (first corner-a) (first corner-b))
        by (max (second corner-a) (second corner-b))
        width (- bx ax)
        height (- by ay)
        program (:solid-color-alpha (:shader-programs graphics-state))
        qtranslate (.translate (new Matrix4f)
                               (+ ax half-pixel-width)
                               (+ ay half-pixel-width) 0)
        qscale (.scale (new Matrix4f)
                       width height 1)
        qtransform (.mul qtranslate qscale (new Matrix4f))]
    (uniforms/set-proj-matrix g4 program (:proj-matrix graphics-state))
    (uniforms/set-view-matrix g4 program (:view-matrix graphics-state))
    (uniforms/set-model-matrix g4 program (:model-matrix graphics-state))
    (uniforms/update-vec4 g4 program (:fill-color shader-defs/Locations)
                          (:red fill-rgba)
                          (:green fill-rgba)
                          (:blue fill-rgba)
                          (:alpha fill-rgba))
    (uniforms/update-mat4 g4 program (:transform shader-defs/Locations)
                          false (.get qtransform (float-array 16)))
    (jogl/draw-vertex-array-with-program g4 (:quad-va graphics-state) program)))

(defn draw-filled-triangle-xy [graphics-state
                               a b c rgba]
  (let [g4 (:g4 graphics-state)
        resolution (:resolution graphics-state)
        program (:triangle-2d (:shader-programs graphics-state))]
    (uniforms/set-proj-matrix g4 program (:proj-matrix graphics-state))
    (uniforms/set-view-matrix g4 program (:view-matrix graphics-state))
    (uniforms/set-model-matrix g4 program (:model-matrix graphics-state))
    (uniforms/update-vec4 g4 program (:resolution shader-defs/Locations)
                          (first resolution) (second resolution) 0 0)
    (uniforms/update-vec4 g4 program (:point-a shader-defs/Locations) (first a) (second a) 0 1)
    (uniforms/update-vec4 g4 program (:point-b shader-defs/Locations) (first b) (second b) 0 1)
    (uniforms/update-vec4 g4 program (:point-c shader-defs/Locations) (first c) (second c) 0 1)
    (uniforms/update-vec4 g4 program (:fill-color shader-defs/Locations) (:red rgba) (:green rgba) (:blue rgba) (:alpha rgba))
    (jogl/draw-vertex-array-with-program g4 (:quad-va graphics-state) program)))

(defn draw-rect-xy [graphics-state
                    corner-a corner-b pixel-width rgba]
  (let [ax (min (first corner-a) (first corner-b))
        ay (min (second corner-a) (second corner-b))
        bx (max (first corner-a) (first corner-b))
        by (max (second corner-a) (second corner-b))
        stroke {:width pixel-width
                :linecap :butt
                :linejoin :miter
                :color rgba}
        shapes (svg/parse-path (str "M "
                                    ax "," ay " " bx "," ay " "
                                    bx "," by " "
                                    ax "," by " Z") stroke)]
    (doseq [s shapes]
      (case (:shape s)
        :line (draw-line graphics-state
                         (:start s)
                         (:end s)
                         pixel-width
                         rgba)
        :triangle (draw-filled-triangle-xy graphics-state
                                           (:a s) (:b s) (:c s)
                                           rgba)))))

(defn draw-filled-rect-xy [graphics-state
                           corner-a corner-b pixel-width rgba fill-rgba]
  (draw-filled-quad-xy graphics-state
                       corner-a corner-b pixel-width fill-rgba)
  (draw-rect-xy graphics-state
                corner-a corner-b pixel-width rgba))
