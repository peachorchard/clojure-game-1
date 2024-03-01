(ns com.daleroyer.clojure.2djogl.apps.2d-game-1.opengl.init

  (:import [com.jogamp.opengl GL GL4])
  (:require [clojure.string :as str])
  (:require [clojure.pprint])
  (:require [com.daleroyer.clojure.svg.svg :as svg])
  (:require [com.daleroyer.clojure.2djogl.shaders.vertex.solid-color-alpha :as solid-color-alpha])
  (:require [com.daleroyer.clojure.2djogl.shaders.fragment.color-pass-through-alpha :as color-pass-through-alpha])
  (:require [com.daleroyer.clojure.2djogl.shaders.vertex.line-2d :as line-2d-vertex])
  (:require [com.daleroyer.clojure.2djogl.shaders.fragment.line-2d :as line-2d-fragment])
  (:require [com.daleroyer.clojure.2djogl.shaders.vertex.circle-2d :as circle-2d-vertex])
  (:require [com.daleroyer.clojure.2djogl.shaders.fragment.circle-2d :as circle-2d-fragment])
  (:require [com.daleroyer.clojure.2djogl.shaders.vertex.ellipse-2d :as ellipse-2d-vertex])
  (:require [com.daleroyer.clojure.2djogl.shaders.fragment.ellipse-2d :as ellipse-2d-fragment])
  (:require [com.daleroyer.clojure.2djogl.shaders.vertex.triangle-2d :as triangle-2d-vertex])
  (:require [com.daleroyer.clojure.2djogl.shaders.fragment.triangle-2d :as triangle-2d-fragment])
  (:require [com.daleroyer.clojure.2djogl.shapes.quad-mesh :as quad-mesh])
  (:require [com.daleroyer.clojure.2djogl.jogl.jogl :as jogl])
  (:require [com.daleroyer.clojure.2djogl.jogl.uniforms :as uniforms]))

(def bindings {:projection-matrix 1
                :view-matrix 2
                :model-matrix 3
                :pixel-info 4})

(defn frames [node]
  (filter (fn [n] (let [s (svg/attr :label n)]
                    (when s
                      (str/starts-with? s "frame"))))
          (svg/all-nodes-with-attr :label node)))

(defn create-program [gl-state g4 vert-source frag-source]
  (uniforms/bind-program
   []
   (jogl/make-program gl-state g4 vert-source frag-source)))

(defn load-program [gl-state g4 vert-source frag-source program-key]
  (let [result-pair (create-program gl-state g4 vert-source frag-source)]
    (assoc (:gl-state result-pair)
           :shader-programs (assoc (:shader-programs (:gl-state result-pair))
                                   program-key (:gl-program result-pair)))))

(defn load-shaders [gl-state g4]
  (-> gl-state
      (load-program g4
                    solid-color-alpha/source-code
                    color-pass-through-alpha/source-code
                    :solid-color-alpha)
      (load-program g4
                    line-2d-vertex/source-code
                    line-2d-fragment/source-code
                    :line-2d)
      (load-program g4
                    circle-2d-vertex/source-code
                    circle-2d-fragment/source-code
                    :circle-2d)
      (load-program g4
                    ellipse-2d-vertex/source-code
                    ellipse-2d-fragment/source-code
                    :ellipse-2d)
      (load-program g4
                    triangle-2d-vertex/source-code
                    triangle-2d-fragment/source-code
                    :triangle-2d)))

(defn init [gl-state ^GL4 g4]
  ;;(.setSwapInterval g4 0)

  (let [size-x 1
        size-y 1
        color {:red 0 :green 0 :blue 0 :alpha 1}
        updated-gl-state (load-shaders gl-state g4)]
    
    (.glEnable g4 GL/GL_DEPTH_TEST)
    (.glClearColor g4 1 1 1 0)

    (let [quad-result (quad-mesh/make updated-gl-state g4 size-x size-y color)]
      (assoc (:updated-gl-state quad-result)
             :quad (:vertex-array quad-result)))))
