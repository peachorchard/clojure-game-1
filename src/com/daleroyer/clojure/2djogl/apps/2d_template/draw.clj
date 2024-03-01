(ns com.daleroyer.clojure.2djogl.apps.2d-template.draw
  (:import [com.jogamp.opengl GL GL4]))

(defn draw-shapes [_])

(defn draw [gl-state ^GL4 g4]
  (.glDisable g4 GL/GL_DEPTH_TEST)
  (.glBlendFunc g4 GL/GL_SRC_ALPHA GL/GL_ONE_MINUS_SRC_ALPHA)
  (.glEnable g4 GL/GL_BLEND)

  (draw-shapes gl-state)

  (.glDisable g4 GL/GL_BLEND)
  (.glEnable g4 GL/GL_DEPTH_TEST)
  )
