(ns com.daleroyer.clojure.2djogl.shaders.vertex.per-vertex-color
  (:require [com.daleroyer.clojure.2djogl.shaders.defs :as shader-defs]))

(def source-code (str
  "#version 450
   
   layout (location = " (:position shader-defs/Locations) ") in vec2 position;
   
   layout (location = " (:color shader-defs/Locations) ") in vec3 color;

   layout (location = " (:proj-matrix shader-defs/Locations) ") uniform mat4 proj;
   layout (location = " (:view-matrix shader-defs/Locations) ") uniform mat4 view;
   layout (location = " (:model-matrix shader-defs/Locations) ") uniform mat4 model;

   layout (location = 0) out vec3 interpolatedColor;

   void main() {
      gl_Position = proj * (view * (model *vec4(position, 0, 1)));
      interpolatedColor = color;
   }
   "))
