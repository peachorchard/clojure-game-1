(ns com.daleroyer.clojure.2djogl.shaders.fragment.circle-2d
  (:require [com.daleroyer.clojure.2djogl.shaders.defs :as shader-defs]))

(def source-code (str
  "#version 450

   layout (location = " (:center shader-defs/Locations) ") uniform vec4 center_point;
                                                         
   layout (location = " (:line-width shader-defs/Locations) ") uniform float line_width;
   
   in vec4 presolution;
   in mat4 mv_transform;
   in mat4 mvp_transform;
   in mat4 inv_mvp_transform;
   in vec4 strokeColor;
   in vec4 fillColor;
   in vec2 center_pixel_position;
   in vec4 center;
   in float radius;
   in vec4 interpolated_position;
   in vec2 interpolated_pixel_position;

   layout (location = 0) out vec4 outputColor;

   vec4 normalized_position(vec4 projected_position) {
      return ((projected_position+1)/2.0);
   }

   vec2 pixel_position(vec4 projected_position, vec4 resolution) {
      vec2 n = normalized_position(projected_position).xy;
      return n*resolution.xy;
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
      vec4 center_screen = mvp_transform*center;
      vec2 current_pixel = interpolated_pixel_position;
      vec4 current_world = (inv_mvp_transform * vec4(interpolated_position.xy, center_screen.z, 1));
      vec4 dir_vec = vec4(normalize(current_world.xy-center.xy), 0, 0);
      vec4 on_circle_screen = mvp_transform * vec4(center.xy+dir_vec.xy*radius, center.z, 1);
      vec2 on_circle_pixel = pixel_position(on_circle_screen, presolution);
   
      vec2 diff = (current_pixel-on_circle_pixel);
      float dist2 = (dot(diff, diff));

      vec4 cv = mvp_transform*vec4(normalize(current_world.xyz-center.xyz) * line_width, 0);
      //vec2 pcv = pixel_position(cv, presolution);
      float line_pixel_width = dot(cv, cv);

      float mag = near(dist2, line_pixel_width);

      vec3 strokePortion = (strokeColor.rgb * strokeColor.a);//(mag*strokeColor.a));
      vec3 fillPortion = (fillColor.rgb * (fillColor.a));
      //float composedAlpha = mag*strokeColor.a;
      //vec4 composedColor = vec4(strokePortion+fillPortion, composedAlpha);

      vec2 diff_center = (current_world.xy-center.xy);
      float dist2_center = dot(diff_center, diff_center);
      float r2 = (radius*radius);

      if(dist2_center < r2) {
         float imag = clamp(1.0-mag, 0, 1);
         outputColor = vec4((imag*fillPortion)+(mag*strokePortion), max(mag*strokeColor.a, fillColor.a));
      } else {
         if(mag <= 0) {
            discard;
         }
         outputColor = vec4(mag*strokePortion, mag*strokeColor.a);
      }
   
      //float alpha = 1;
      //outputColor = vec4(strokeColor.rgb, mag*strokeColor.a);
      //outputColor = composedColor;
   }
   ")
)