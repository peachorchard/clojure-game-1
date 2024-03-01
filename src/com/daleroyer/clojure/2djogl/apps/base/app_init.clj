(ns com.daleroyer.clojure.2djogl.apps.base.app-init
  (:import [com.jogamp.opengl GL4 GLAutoDrawable])
  (:require [com.daleroyer.clojure.2djogl.jogl.debug :as debug] :reload-all))

(defn gl-init
  [gl-state init-function ^GL4 g4]
  (debug/init g4 (.getContext ^GLAutoDrawable (:window gl-state)))
  (.setSwapInterval g4 1)
  (let [updated-gl-state (assoc gl-state
                                :gl-context {:buffers ()
                                             :mapped-buffers ()}
                                :start-time (System/currentTimeMillis))]
    (if init-function
      (init-function updated-gl-state g4)
      updated-gl-state)))
