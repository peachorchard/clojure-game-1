(ns com.daleroyer.clojure.2djogl.apps.rotating-rectangle.init
  (:import [com.jogamp.opengl GL GL4])
  (:require [com.daleroyer.clojure.2djogl.shaders.vertex.per-vertex-color :as per-vertex-color] :reload-all)
  (:require [com.daleroyer.clojure.2djogl.shaders.fragment.color-pass-through :as color-pass-through] :reload-all)
  (:require [com.daleroyer.clojure.2djogl.shapes.quad-mesh :as quad-mesh] :reload-all)
  (:require [com.daleroyer.clojure.2djogl.jogl.jogl :as jogl] :reload-all)
  (:require [com.daleroyer.clojure.2djogl.jogl.uniforms :as uniforms] :reload-all))

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
                    per-vertex-color/source-code
                    color-pass-through/source-code
                    :solid-color-alpha)))

(defn init [gl-state ^GL4 g4]
  (let [updated-gl-state (load-shaders gl-state g4)]
    (.glEnable g4 GL/GL_DEPTH_TEST)
    (let [quad-result (quad-mesh/make updated-gl-state g4 1 1 {:red 1 :green 0 :blue 0 :alpha 1})]
      (assoc (:updated-gl-state quad-result)
             :quad (:vertex-array quad-result)))))
