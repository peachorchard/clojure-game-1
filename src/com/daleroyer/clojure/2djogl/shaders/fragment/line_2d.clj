(ns com.daleroyer.clojure.2djogl.shaders.fragment.line-2d
  (:require [com.daleroyer.clojure.2djogl.shaders.defs :as shader-defs]))

(def source-code (str
  "#version 450

   layout (location = " (:proj-matrix shader-defs/Locations) ") uniform mat4 proj;
   layout (location = " (:view-matrix shader-defs/Locations) ") uniform mat4 view;
   layout (location = " (:model-matrix shader-defs/Locations) ") uniform mat4 model;

   layout (location = " (:resolution shader-defs/Locations) ") uniform vec4 resolution;

   layout (location = " (:start-point shader-defs/Locations) ") uniform vec4 start;
   layout (location = " (:end-point shader-defs/Locations) ") uniform vec4 end;

   layout (location = " (:line-width shader-defs/Locations) ") uniform float line_width;

   in mat4 mv_transform;
   in mat4 inv_mvp_transform;
   in vec4 interpolatedColor;
   in vec4 interpolated_screen_position;
   in vec4 start_screen;
   in vec4 end_screen;

   layout (location = 0) out vec4 outputColor;

   vec4 normalized_position(vec4 projected_position) {
      return ((projected_position+1)/2.0);
   }

   vec2 pixel_position(vec4 projected_position, vec2 resolution) {
      vec2 n = normalized_position(projected_position).xy;
      return n*resolution;
   }

   float near(float a, float b) {
      float r = abs(a/b);
      float diff = a-b;
      if(a > b && abs(diff) > 2) {
         return 0;
      }
   
      if(abs(diff) <= 2) {
         return clamp(1.0-((diff+2)/4.0), 0, 1);
      }
   
      return 1;
   }

   float within(float value, float distance) {
      return value <= distance ? 1 : 0;
   }

   void main() {
      vec3 p1 = (mv_transform*start).xyz;
      vec3 p2 = (mv_transform*end).xyz;
      vec3 p3 = (mv_transform*(inv_mvp_transform*interpolated_screen_position)).xyz;
      vec3 pdiff = p2-p1;
      float u = ((p3.x - p1.x)*(p2.x-p1.x)
                 + (p3.y-p1.y)*(p2.y-p1.y))
                /dot(pdiff, pdiff);
      if(u < 0 || u > 1) {
         discard;
      }

      vec3 on_line = p1+u*(p2-p1);
      vec3 on_line_diff = p3-on_line;
      float dist2 = dot(on_line_diff, on_line_diff);
   
      vec3 line_width_v = (mv_transform*vec4(normalize(pdiff)*(line_width/2), 0)).xyz;
      float half_line_width2 = dot(line_width_v, line_width_v);
      float mag = near(dist2, half_line_width2);
   
      if(mag <= 0) {
         discard;
      }

      outputColor = vec4(interpolatedColor.rgb, mag*interpolatedColor.a);
   }
   "))
