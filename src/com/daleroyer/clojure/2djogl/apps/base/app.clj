(ns com.daleroyer.clojure.2djogl.apps.base.app
  (:import [com.jogamp.newt.opengl GLWindow])
  (:import [com.jogamp.newt.event WindowAdapter])
  (:import [com.jogamp.nativewindow WindowClosingProtocol$WindowClosingMode])
  (:import [com.jogamp.opengl.util Animator])
  (:import [com.jogamp.opengl GL4 GLCapabilities GLProfile GLContext GLEventListener])
  (:require [com.daleroyer.clojure.2djogl.jogl.jogl :as jogl] :reload-all)
  (:require [com.daleroyer.clojure.2djogl.apps.base.app-init :refer [gl-init]] :reload-all)
  (:require [com.daleroyer.clojure.2djogl.apps.base.app-reshape :refer [gl-reshape]] :reload-all)
  (:require [com.daleroyer.clojure.2djogl.apps.base.app-draw :refer [gl-draw]] :reload-all)
  (:require [com.daleroyer.clojure.2djogl.apps.base.app-dispose :refer [gl-dispose]] :reload-all))

(defn update-gl-state [app-state gl-state]
  (swap! app-state assoc
         :gl-state gl-state))

(defn create-window [app-state]
  (let [caps (GLCapabilities. (GLProfile/get GLProfile/GL4))]
    (.setBackgroundOpaque caps true)
    (.setDoubleBuffered caps true)
    (.setRedBits caps 8)
    (.setGreenBits caps 8)
    (.setBlueBits caps 8)
    (.setAlphaBits caps 8)
    (let [window (GLWindow/create caps)]
      (update-gl-state app-state
                       (assoc (:gl-state @app-state)
                              :window window)))))

(defn create-animator [app-state]
  (let [gl-state (:gl-state @app-state)
        ^GLWindow window (:window gl-state)]
    (update-gl-state app-state
                     (assoc gl-state
                            :animator (new Animator window)))))

(defn init-window [app-state properties]
  (let [size (or (:window-size properties) {:width 1024 :height 768})]
    (when (:debug properties)
      (println "enabled gl debug")
      (.setContextCreationFlags ^GLWindow (:window (:gl-state @app-state))
                                GLContext/CTX_OPTION_DEBUG))
    (doto ^GLWindow (:window (:gl-state @app-state))
      (.setTitle (or (:window-title properties) "2D-JOGL app"))
      (.setSize (:width size) (:height size))
      (.addGLEventListener
       (reify GLEventListener
         (init [_ drawable]
           (println "is hardware rasterizer = "
                    (.isHardwareRasterizer (.getGLProfile drawable)))
           (.setUpdateFPSFrames (.getAnimator drawable) 3 nil)
           (let [g4 (jogl/get-g4 drawable)]
             (update-gl-state app-state (gl-init (:gl-state @app-state)
                                                 (:init properties) g4))))
         (reshape [_ drawable x y width height]
           (let [g4 (jogl/get-g4 drawable)]
             (update-gl-state app-state (gl-reshape (:gl-state @app-state)
                                                    (:reshape properties) g4
                                                    x y width height))))
         (display [_ drawable]
           (let [^GL4 g4 (jogl/get-g4 drawable)
                 fps (.getLastFPS (.getAnimator drawable))]
             (when (and (> fps 0) (< fps 30)) (println fps))
             (gl-draw (:gl-state @app-state) (:draw properties) g4)
             (.glFlush g4)
               ;;(.swapBuffers drawable)
             ))
         (dispose [_ drawable]
           (let [g4 (jogl/get-g4 drawable)]
             (update-gl-state app-state (gl-dispose (:gl-state @app-state)
                                                    (:dispose properties) g4))))))
      (.addWindowListener
       (proxy [WindowAdapter] []
         (windowDestroyed [e]
           (.stop ^Animator (:animator (:gl-state @app-state)))
           (.setAnimator ^GLWindow (:window (:gl-state @app-state)) nil))))
      (.addKeyListener (reify com.jogamp.newt.event.KeyListener
                         (keyPressed [_ event]
                           (when (and (:key-down properties) (not (.isAutoRepeat event)))
                             (if (sequential? (:key-down properties))
                               (apply (first (:key-down properties))
                                      (concat [(.getKeyCode event)]
                                              (rest (:key-down properties))))
                               ((:key-down properties) (.getKeyCode event)))))
                         (keyReleased [_ event]
                           (when (and (:key-up properties) (not (.isAutoRepeat event)))
                             (if (sequential? (:key-up properties))
                               (apply (first (:key-up properties))
                                      (concat [(.getKeyCode event)]
                                              (rest (:key-up properties))))
                               ((:key-up properties) (.getKeyCode event)))))))
      (.setDefaultCloseOperation WindowClosingProtocol$WindowClosingMode/DISPOSE_ON_CLOSE)
      (.setVisible true))))

(defn start-animator [app-state]
  (.start ^Animator (:animator (:gl-state @app-state))))

(defn run [app-state properties]
  (when (:run properties)
    ((:run properties) app-state)))

(defn create-main-window
  [app-state properties]
  (create-window app-state)
  (create-animator app-state)
  (init-window app-state properties)
  (start-animator app-state)
  (run app-state properties))
