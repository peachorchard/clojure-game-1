(ns com.daleroyer.clojure.2djogl.apps.rotating-rectangle.reshape
  (:import [org.joml Matrix4f]))

(defn reshape [gl-state _ _ _ width height] ;; g4 x y width height]
  (let [ratio (:window-aspect-ratio gl-state)
        u (if (< ratio 1) ratio 1)
        v (if (> ratio 1) (/ 1 ratio) 1)
        projection-matrix (.ortho (new Matrix4f) (- 0 u) u (- 0 v) v 1 -1)]
    (assoc gl-state
           :proj-matrix projection-matrix
           :resolution [width height])))
