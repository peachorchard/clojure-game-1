(ns com.daleroyer.clojure.2djogl.apps.base.app-reshape
  (:import [com.jogamp.opengl GL4]))

(defn gl-reshape 
  ([gl-state reshape-function ^GL4 g4 x y width height]
   (.glViewport g4 x y width height)
   (let [updated-gl-state (assoc gl-state :window-aspect-ratio (/ width height))]
     (if reshape-function
       (reshape-function updated-gl-state g4 x y width height)
       updated-gl-state))))
