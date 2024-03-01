(ns com.daleroyer.clojure.2djogl.apps.2d-game-1.graphics.transforms
  (:import [org.joml Matrix4f Vector4f]))

(defn make-transform-matrix ^Matrix4f [^Matrix4f mat transforms]
  (if transforms
    (let [tmat (new Matrix4f)]
      (doseq [t transforms]
        (when (:translate t)
          (.translate tmat
                      (first (:translate t))
                      (second (:translate t))
                      0))
        (when (:rotate t)
          (.rotate tmat (:rotate t) 0 0 1))
        (when (:scale t)
          (.scale tmat
                  (first (:scale t))
                  (second (:scale t))
                  1))
        (when (:matrix t)
          (.mul tmat
                (new Matrix4f
                     (nth (:matrix t) 0) (nth (:matrix t) 2) 0 (nth (:matrix t) 4)
                     (nth (:matrix t) 1) (nth (:matrix t) 3) 0 (nth (:matrix t) 5)
                     0 0 1 0
                     0 0 0 1))))
      (.mul tmat mat (new Matrix4f)))
    mat))

(defn apply-transforms4 [point ^Matrix4f matrix]
  (let [v (new Vector4f (nth point 0) (nth point 1) (nth point 2) (nth point 3))
        vm (.mul v matrix (new Vector4f))]
    [(.x vm) (.y vm) (.z vm) (.w vm)]))
