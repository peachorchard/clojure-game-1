(ns com.daleroyer.clojure.2djogl.shaders.vertex.line-2d
  (:require [com.daleroyer.clojure.2djogl.shaders.defs :as shader-defs]))

(def source-code (str
  "#version 450
   
   layout (location = " (:position shader-defs/Locations) ") in vec2 position;
   layout (location = " (:color shader-defs/Locations) ") in vec4 color;

   layout (location = " (:proj-matrix shader-defs/Locations) ") uniform mat4 proj;
   layout (location = " (:view-matrix shader-defs/Locations) ") uniform mat4 view;
   layout (location = " (:model-matrix shader-defs/Locations) ") uniform mat4 model;

   layout (location = " (:resolution shader-defs/Locations) ") uniform vec4 resolution;

   layout (location = " (:stroke-color shader-defs/Locations) ") uniform vec4 line_color;

   out mat4 mv_transform;
   out mat4 inv_mvp_transform;
   out vec4 interpolatedColor;
   out vec4 interpolated_screen_position;

   void main() {
      gl_Position = vec4((position*2)-1, 0, 1);
      interpolated_screen_position = gl_Position;
      mv_transform = view * model;
      inv_mvp_transform = inverse(proj * view * model);

      interpolatedColor = line_color;
   }
   "))
