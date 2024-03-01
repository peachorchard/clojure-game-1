(ns com.daleroyer.clojure.2djogl.shaders.fragment.triangle-2d)

(def source-code
  "#version 450

   in vec4 interpolatedColor;
   in vec2 interpolated_pixel_position;
   in vec2 a;
   in vec2 b;
   in vec2 c;

   layout (location = 0) out vec4 outputColor;

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

   vec2 project_onto_line(vec2 p, vec2 start, vec2 end) {
      vec2 line_vector = end-start;
      vec2 p_vector = p-start;
      float c = (dot(p_vector, line_vector)/dot(line_vector, line_vector));
      return c*line_vector+start;
   }

   int which_side(vec2 p, vec2 start, vec2 end) {
      // optimized for 2-d on xy-plane using d = z value of cross product
      float d = (end.x - start.x) * (p.y - start.y) -
                 (end.y - start.y) * (p.x - start.x);
      if(d > 0) {
         return 1;
      }

      return 0;
   }

   int which_side0(vec2 p, vec2 start, vec2 end) {
      vec2 p_vec = p-start;
      vec2 l_vec = end-start;
      vec2 n_vec = vec2(-l_vec.y, l_vec.x);
      if(dot(n_vec, p_vec) > 0) {
         return 1;
      }
      return 0;
   }

   void main() {
      int side = which_side(interpolated_pixel_position, a, b) +
                 which_side(interpolated_pixel_position, b, c) +
                 which_side(interpolated_pixel_position, c, a);

      //vec2 diffa = interpolated_pixel_position-project_onto_line(interpolated_pixel_position, a, b);
      //vec2 diffb = interpolated_pixel_position-project_onto_line(interpolated_pixel_position, b, c);
      //vec2 diffc = interpolated_pixel_position-project_onto_line(interpolated_pixel_position, c, a);
      //float minDist2 = min(min(dot(diffa, diffa), dot(diffb, diffb)), dot(diffc, diffc));
   
      if(side == 1 || side == 2) {
         discard;
      }

      float mag = 1;
      //if(minDist2 < 1) {
      //   if(minDist2 <= 0) {
      //      discard;
      //   } else {
      //      mag = clamp(minDist2*8, 0, 1);
      //   }
      //}

      //if(mag <= 0) {
      //   discard;
      //}

      //float alpha = 1;
      outputColor = vec4(interpolatedColor.rgb, mag*interpolatedColor.a);
      //outputColor = vec4(1, 0, 1, 1);
   }
   ")
