(ns com.daleroyer.clojure.2djogl.apps.base.app-draw
  (:import [com.jogamp.opengl GL GL4]))

(defn gl-draw
  [gl-state draw-function ^GL4 g4]
  (.glClearDepth g4 1)
  (.glClear g4 (bit-or GL/GL_COLOR_BUFFER_BIT GL/GL_DEPTH_BUFFER_BIT))

  (when draw-function 
    (if (sequential? draw-function)
      (apply (first draw-function)
             (concat [gl-state g4] (rest draw-function)))
      (draw-function gl-state g4))))
