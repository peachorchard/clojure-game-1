(ns com.daleroyer.clojure.2djogl.shaders.vertex.circle-2d
  (:require [com.daleroyer.clojure.2djogl.shaders.defs :as shader-defs]))

(def source-code (str
  "#version 450
   
   layout (location = " (:position shader-defs/Locations) ") in vec2 position;
   
   layout (location = " (:color shader-defs/Locations) ") in vec4 color;

   layout (location = " (:proj-matrix shader-defs/Locations) ") uniform mat4 proj;
   layout (location = " (:view-matrix shader-defs/Locations) ") uniform mat4 view;
   layout (location = " (:model-matrix shader-defs/Locations) ") uniform mat4 model;

   layout (location = " (:resolution shader-defs/Locations) ") uniform vec4 resolution;

   layout (location = " (:stroke-color shader-defs/Locations) ") uniform vec4 stroke_color;
   layout (location = " (:fill-color shader-defs/Locations) ") uniform vec4 fill_color;

   layout (location = " (:center shader-defs/Locations) ") uniform vec4 center_point;
   layout (location = " (:radii shader-defs/Locations) ") uniform float circle_radius;

   out vec4 presolution;
   out mat4 mv_transform;
   out mat4 mvp_transform;
   out mat4 inv_mvp_transform;
   out vec4 strokeColor;
   out vec4 fillColor;
   out vec2 center_pixel_position;
   out vec4 center;
   out float radius;
   out vec4 interpolated_position;
   out vec2 interpolated_pixel_position;

   vec4 normalized_position(vec4 projected_position) {
      return ((projected_position+1)/2.0);
   }

   vec2 pixel_position(vec4 projected_position, vec2 resolution) {
      vec2 n = normalized_position(projected_position).xy;
      return n*resolution;
   }

   void main() {
      presolution = resolution;
      center = center_point;
      radius = circle_radius;
      mv_transform = (view * model);
      mvp_transform = proj * (view * model);
      inv_mvp_transform = inverse(mvp_transform);
      gl_Position = vec4((position*2)-1, 0, 1);
      interpolated_position = gl_Position;

      vec4 center_position = proj * (view * (model * vec4(center_point.xyz, 1)));
      center_pixel_position = pixel_position(center_position, resolution.xy);
   
      interpolated_pixel_position = pixel_position(gl_Position, resolution.xy);

      strokeColor = stroke_color;
      fillColor = fill_color;
   }
   "))
