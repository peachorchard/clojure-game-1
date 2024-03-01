(ns com.daleroyer.clojure.2djogl.jogl.mat4
  (:import [org.joml Matrix4f]))

(defn ortho [min-x max-x min-y max-y min-z max-z]
  (.get (.setOrtho (new Matrix4f) min-x max-x min-y max-y max-z min-z) (float-array 16)))
