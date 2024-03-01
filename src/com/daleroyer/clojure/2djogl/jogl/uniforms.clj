(ns com.daleroyer.clojure.2djogl.jogl.uniforms
  (:import [com.jogamp.opengl GL4])
  (:import [org.joml Matrix4f])
  (:require [com.daleroyer.clojure.2djogl.jogl.uniforms :as uniforms])
  (:require [com.daleroyer.clojure.2djogl.shaders.defs :as shader-defs]))

(defn set-proj-matrix [^GL4 g4 program ^Matrix4f matrix]
  (.glProgramUniformMatrix4fv g4 (:name program)
                              (:proj-matrix shader-defs/Locations)
                              1 false
                              (.get matrix (float-array 16))
                              0))

(defn set-view-matrix [^GL4 g4 program ^Matrix4f matrix]
  (.glProgramUniformMatrix4fv g4 (:name program)
                              (:view-matrix shader-defs/Locations)
                              1 false
                              (.get matrix (float-array 16))
                              0))

(defn set-model-matrix [^GL4 g4 program ^Matrix4f matrix]
  (.glProgramUniformMatrix4fv g4 (:name program)
                              (:model-matrix shader-defs/Locations)
                              1 false
                              (.get matrix (float-array 16))
                              0))

(defn update-float [^GL4 g4 program location value]
  (.glProgramUniform1f g4 (:name program) location value))

(defn update-vec2 [^GL4 g4 program location v1 v2]
  (.glProgramUniform2f g4 (:name program) location v1 v2))

(defn update-vec3 [^GL4 g4 program location v1 v2 v3]
  (.glProgramUniform3f g4 (:name program) location v1 v2 v3))

(defn update-vec4 [^GL4 g4 program location v1 v2 v3 v4]
  (.glProgramUniform4f g4 (:name program) location v1 v2 v3 v4))

(defn update-mat4 [^GL4 g4 program location transpose values]
  (.glProgramUniformMatrix4fv g4 (:name program) location 1 transpose values 0))

(defn bind-program [buffer-bindings program]
  (assoc program
         :buffers (into (:buffers program)
                        (for [pair buffer-bindings
                              :let [binding (first pair)
                                    buffer (second pair)]]
                          {:type (:type buffer)
                           :binding binding
                           :name (:name buffer)}))))