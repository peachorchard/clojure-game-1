(ns com.daleroyer.clojure.2djogl.apps.2d-game-1.model.camera
  (:import [org.joml Matrix4f]))

(defn object-bounds-center [object-bounds]
  {:x (+ (:x1 object-bounds)
         (/ (- (:x2 object-bounds)
               (:x1 object-bounds))
            2))
   :y (+ (:y1 object-bounds)
         (/ (- (:y2 object-bounds)
               (:y1 object-bounds))
            2))})

(defn focus-bounds [resolution focus-center]
  {:x1 (- (:x focus-center)
          (/ (first resolution) 2))
   :y1 (- (:y focus-center)
          (/ (second resolution) 2))
   :x2 (+ (:x focus-center)
          (/ (first resolution) 2))
   :y2 (+ (:y focus-center)
          (/ (second resolution) 2))})

(defn clamp-focus-center [resolution global-bounds focus-center]
  (let [fb (focus-bounds resolution focus-center)]
    {:x (if (< (:x1 fb) (:x1 global-bounds))
          (- (:x1 global-bounds))
          (if (> (:x2 fb) (:x2 global-bounds))
            (- (first resolution) (:x2 global-bounds))
            (- (/ (first resolution) 2) (:x focus-center))))
     :y (if (< (:y1 fb) (:y1 global-bounds))
          (- (:y1 global-bounds))
          (if (> (:y2 fb) (:y2 global-bounds))
            (- (second resolution) (:y2 global-bounds))
            (- (/ (second resolution) 2) (:y focus-center))))}))

(defn get-view-matrix [resolution global-bounds focus-object-bounds]
  (let [focus-center (object-bounds-center focus-object-bounds)
        t (clamp-focus-center resolution global-bounds focus-center)]
    (.translate (new Matrix4f)
                (:x t)
                (:y t)
                0)))