(ns com.daleroyer.clojure.2djogl.shaders.fragment.color-pass-through)

(def source-code
  "#version 450
   
   layout (location = 0) in vec3 interpolatedColor;

   layout (location = 0) out vec4 outputColor;

   void main() {
      outputColor = vec4(interpolatedColor, 1);
   }
   ")

