(ns com.daleroyer.clojure.2djogl.jogl.debug
  (:import [com.jogamp.opengl GL GL4 GL2ES2 GLDebugListener GLContext]))

(defn debug-listener []
  (proxy [GLDebugListener] []
    (messageSent [event]
      (println event))))

(defn init [^GL4 g4 ^GLContext context]
  (.addGLDebugListener context (debug-listener))
  (let [count 0
        ids nil]
    ;; glDebugMessageControl(int source, int type, int severity, int count, IntBuffer ids, boolean enabled)
    ;; disable all messages by default (false)
    (.glDebugMessageControl g4 GL/GL_DONT_CARE GL/GL_DONT_CARE GL/GL_DONT_CARE count ids false)
    ;; enalbe high and medium severity messages
    (.glDebugMessageControl g4 GL/GL_DONT_CARE GL/GL_DONT_CARE GL2ES2/GL_DEBUG_SEVERITY_HIGH count ids true)
    (.glDebugMessageControl g4 GL/GL_DONT_CARE GL/GL_DONT_CARE GL2ES2/GL_DEBUG_SEVERITY_MEDIUM count ids false)))