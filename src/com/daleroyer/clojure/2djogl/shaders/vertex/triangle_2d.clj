(ns com.daleroyer.clojure.2djogl.shaders.vertex.triangle-2d
  (:require [com.daleroyer.clojure.2djogl.shaders.defs :as shader-defs]))

(def source-code (str
  "#version 450
   
   layout (location = " (:position shader-defs/Locations) ") in vec2 position;
   
   layout (location = " (:color shader-defs/Locations) ") in vec4 color;

   layout (location = " (:proj-matrix shader-defs/Locations) ") uniform mat4 proj;
   layout (location = " (:view-matrix shader-defs/Locations) ") uniform mat4 view;
   layout (location = " (:model-matrix shader-defs/Locations) ") uniform mat4 model;
   layout (location = " (:resolution shader-defs/Locations) ") uniform vec4 resolution;

   layout (location = " (:point-a shader-defs/Locations) ") uniform vec4 point_a;
   layout (location = " (:point-b shader-defs/Locations) ") uniform vec4 point_b;
   layout (location = " (:point-c shader-defs/Locations) ") uniform vec4 point_c;
   layout (location = " (:fill-color shader-defs/Locations) ") uniform vec4 fill_color;

   out vec4 interpolatedColor;
   out vec2 interpolated_pixel_position;
   out vec2 a;
   out vec2 b;
   out vec2 c;

   vec4 normalized_position(vec4 projected_position) {
      return ((projected_position+1)/2.0);
   }

   vec2 pixel_position(vec4 projected_position, vec2 resolution) {
      vec2 n = normalized_position(projected_position).xy;
      return n*resolution;
   }

   void main() {
      gl_Position = vec4((position*2)-1, 0, 1);
      interpolated_pixel_position = pixel_position(gl_Position, resolution.xy);

      a = pixel_position(proj * view * model * vec4(point_a.xy, 0, 1), resolution.xy);
      b = pixel_position(proj * view * model * vec4(point_b.xy, 0, 1), resolution.xy);
      c = pixel_position(proj * view * model * vec4(point_c.xy, 0, 1), resolution.xy);
   
      interpolatedColor = fill_color;
   }
   "))
