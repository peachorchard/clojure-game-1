(ns com.daleroyer.clojure.2djogl.shaders.fragment.ellipse-2d
  (:require [com.daleroyer.clojure.2djogl.shaders.defs :as shader-defs]))

(def source-code (str
  "#version 450

   layout (location = " (:line-width shader-defs/Locations) ") uniform float line_width;
   
   in vec4 presolution;
   in mat4 mv_transform;
   in mat4 mvp_transform;
   in mat4 inv_mvp_transform;
   in vec4 strokeColor;
   in vec4 fillColor;
   in vec2 center_pixel_position;
   in vec4 center;
   in vec2 radii;
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
      vec4 center_screen = mvp_transform*vec4(center.xy, 0, 1);
      vec2 current_pixel = interpolated_pixel_position;
      vec4 current_world = (inv_mvp_transform * vec4(interpolated_position.xy, center_screen.z, 1));
      vec2 dir_vec = normalize(current_world.xy-center.xy);

      vec2 xy = (current_world-center).xy;
      float a = radii.x;
      float b = radii.y;
      float a2 = a*a;
      float b2 = b*b;
      vec2 ellipse = vec2(xy.x*xy.x/(a2), xy.y*xy.y/(b2));
      float in_out = ellipse.x+ellipse.y;
      
      float xp2 = (dir_vec.x*dir_vec.x);
      float yp2 = (dir_vec.y*dir_vec.y);
      float d = sqrt(1/(xp2/a2+yp2/b2));
      vec2 on_ellipse = (dir_vec*d)+center.xy;
      vec4 on_ellipse_screen = mvp_transform*vec4(on_ellipse.xy, center.z, 1);
      vec2 on_ellipse_pixel = pixel_position(on_ellipse_screen, presolution);

      vec2 diff = (current_pixel-on_ellipse_pixel);
      float dist = dot(diff, diff);

      vec4 cv = mvp_transform*vec4(normalize(current_world.xyz-center.xyz)*line_width, 0);
      //vec2 pcv = pixel_position(cv, presolution);
      float line_pixel_width = dot(cv, cv);

      float mag = near(dist, line_pixel_width);

      vec3 strokePortion = (strokeColor.rgb * strokeColor.a);
      vec3 fillPortion = (fillColor.rgb * (fillColor.a));
      if(in_out < 1) {
         float imag = clamp(1.0-mag, 0, 1);
         outputColor = vec4((imag*fillPortion)+(mag*strokePortion), max(imag*strokeColor.a, fillColor.a));
      } else {
         if(mag <= 0) {
            discard;
         }
         outputColor = vec4(mag*strokePortion, mag*strokeColor.a);
      }
   }
   "))
