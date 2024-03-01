(ns com.daleroyer.clojure.2djogl.shapes.quad-mesh
  (:import [com.jogamp.opengl GL])
  (:require [com.daleroyer.clojure.2djogl.jogl.jogl :as jogl])
  (:require [com.daleroyer.clojure.2djogl.shaders.defs :as shader-defs]))

(defn make
  "Create a quad mesh with two triangles a and b:
   
   2 = (0,y-size,0) o----o 1 = (x-size,y-size,0)\n
                    | a /|
                    |  / |
                    | / b|
        0 = (0,0,0) o/___o 3 = (x-size,0,0)
   
   a = (0 1 2)
   b = (0 3 1)
   "
  [gl-state g4 x-size y-size color]
  (let [array-buffer-result (jogl/create-buffers gl-state g4 1 GL/GL_ARRAY_BUFFER)
        vertex-buffer (first (:buffers array-buffer-result))
        element-buffer-result (jogl/create-buffers (:updated-gl-state array-buffer-result)
                                                   g4 1 GL/GL_ELEMENT_ARRAY_BUFFER)
        element-buffer (first (:buffers element-buffer-result))
        updated-gl-state (:updated-gl-state element-buffer-result)
        r (:red color)
        g (:green color)
        b (:blue color)
        a (:alpha color)]
    (jogl/create-vertex-array updated-gl-state g4
                              {:vertex-buffer (jogl/load-floats g4 vertex-buffer (float-array
                                                                                  [0 0, r g b a
                                                                                   x-size y-size, r g b a
                                                                                   0 y-size, r g b a
                                                                                   x-size 0, r g b a]))
                               :elements-buffer (jogl/load-shorts g4 element-buffer (short-array [0 1 2 0 3 1]))
                               :element-type GL/GL_TRIANGLES
                               :element-count 6
                               :element-format GL/GL_UNSIGNED_SHORT
                               :attributes [{:description "position"
                                             :index (:position shader-defs/Locations)
                                             :binding 0
                                             :format GL/GL_FLOAT
                                             :format-size 4
                                             :normalize false
                                             :count 2
                                             :offset 0}
                                            {:description "color"
                                             :index (:color shader-defs/Locations)
                                             :binding 0
                                             :format GL/GL_FLOAT
                                             :format-size 4
                                             :normalize false
                                             :count 4
                                             :offset (* 2 4)}]})))
