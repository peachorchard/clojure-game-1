(ns com.daleroyer.clojure.2djogl.apps.base.app-dispose
  (:import [java.nio IntBuffer])
  (:import [com.jogamp.opengl GL4])
  (:require [com.daleroyer.clojure.2djogl.jogl.jogl :as jogl] :reload-all))

(defn remove-mapped-buffers [gl-state ^GL4 g4]
  (let [mapped-buffers (jogl/get-gl-names gl-state g4 :mapped-buffers)]
    (mapv (fn [buffer]
            (.glUnmapNamedBuffer g4 (:name buffer)))
          mapped-buffers)
    (jogl/remove-gl-names gl-state :mapped-buffers (for [b mapped-buffers] (:name b)))))

(defn delete-buffers [gl-state ^GL4 g4]
  (let [buffers (jogl/get-gl-names gl-state g4 :buffers)]
    (mapv (fn [buffer]
            (.glDeleteBuffers g4 1 (IntBuffer/wrap (int-array [(:name buffer)]))))
          buffers)
    (jogl/remove-gl-names gl-state :buffers (for [b buffers] (:name b)))))

(defn delete-vertex-arrays [gl-state ^GL4 g4]
  (let [vertex-arrays (jogl/get-gl-names gl-state g4 :vertex-arrays)]
    (mapv (fn [va]
            (.glDeleteVertexArrays g4 1 (IntBuffer/wrap (int-array [(:name va)]))))
          vertex-arrays)
    (jogl/remove-gl-names gl-state :vertex-arrays vertex-arrays)))

(defn gl-dispose
  [gl-state dispose-function ^GL4 g4]
    (let [updated-gl-state (if dispose-function
                             (dispose-function gl-state g4)
                             gl-state)]
      (delete-vertex-arrays
       (delete-buffers
        (remove-mapped-buffers updated-gl-state g4) g4) g4)))
