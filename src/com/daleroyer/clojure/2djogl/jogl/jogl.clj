(ns com.daleroyer.clojure.2djogl.jogl.jogl
  (:import [java.nio IntBuffer FloatBuffer])
  (:import [com.jogamp.opengl.util GLBuffers])
  (:import [com.jogamp.opengl GL GL4 GLAutoDrawable])
  (:import [com.jogamp.opengl.util.glsl ShaderCode ShaderProgram]) 
  (:require [com.daleroyer.clojure.2djogl.jogl.jogl :as jogl]))

(defn get-g4 [^GLAutoDrawable drawable]
  (.getGL4 (.getGL drawable)))

(defn add-gl-names [gl-state g4 key names]
  (assoc gl-state
         :gl-context
         (assoc (:gl-context gl-state) key
                (concat (key (:gl-context gl-state))
                        (for [n names]
                          {:context g4
                           :name n})))))

(defn remove-gl-names [gl-state key names]
  (assoc gl-state
         :gl-context
         (assoc (:gl-context gl-state) key
                (remove #(some (:name %) names)))))

(defn get-gl-names [gl-state g4 key]
  (if (seq? (key (:gl-context gl-state)))
    (keep #(when (= (:context %) g4) %) (key (:gl-context gl-state)))
    []))

(defn create-buffers [gl-state ^GL4 g4 ^long count type]
  (let [^IntBuffer direct-names (GLBuffers/newDirectIntBuffer count)]
    (.glCreateBuffers g4 count direct-names)
    (let [names (for [i (range count)] (.get direct-names ^long i))]
      {:updated-gl-state (add-gl-names gl-state g4 :buffers names)
       :buffers (for [n names]
                  {:name n
                   :type type})})))

(defn create-vertex-array [gl-state ^GL4 g4 geometry]
  (let [vertex-buffer (:vertex-buffer geometry)
        attributes (:attributes geometry)
        elements-buffer (:elements-buffer geometry)
        va-names (GLBuffers/newDirectIntBuffer 1)]
    (.glCreateVertexArrays g4 1 va-names)
    (let [va-name (.get va-names 0)]
      (mapv (fn [attrib]
              (doto g4
                (.glVertexArrayAttribBinding va-name (:index attrib) (:binding attrib))
                (.glVertexArrayAttribFormat va-name
                                            (:index attrib)
                                            (:count attrib)
                                            (:format attrib)
                                            (:normalize attrib)
                                            (:offset attrib))
                (.glEnableVertexArrayAttrib va-name (:index attrib)))) attributes)
      (.glVertexArrayElementBuffer g4 va-name (:name elements-buffer))
      (.glVertexArrayVertexBuffer g4
                                  va-name
                                  ;; (:binding-index vertex-buffer) 
                                  0
                                  (:name vertex-buffer)
                                  0
                                  (apply (fn [a b]
                                           (+ (* (:count a) (:format-size a))
                                              (* (:count b) (:format-size b)))) attributes))
      (.glBindVertexArray g4 0)
      {:updated-gl-state (add-gl-names gl-state g4 :vertex-arrays [va-name])
       :vertex-array {:name va-name
                      :geometry geometry
                      :vertex-buffer vertex-buffer
                      :attributes attributes
                      :elements-buffer elements-buffer}})))

(defn load-floats [^GL4 g4 buffer ^floats floats]
  (let [^FloatBuffer direct-floats (GLBuffers/newDirectFloatBuffer floats)
        size (* (.capacity direct-floats) Float/BYTES)]
    (.glBindBuffer g4 (:type buffer) (:name buffer))
    (.glBufferStorage g4 (:type buffer) size direct-floats 0)
    (.glBindBuffer g4 (:type buffer) 0)
    (assoc buffer
           :format :float
           :size size
           :direct direct-floats)))

(defn load-shorts [^GL4 g4 buffer ^shorts shorts]
  (let [direct-shorts (GLBuffers/newDirectShortBuffer shorts)
        size (* (.capacity direct-shorts) Short/BYTES)]
    (.glBindBuffer g4 (:type buffer) (:name buffer))
    (.glBufferStorage g4 (:type buffer) size direct-shorts 0)
    (.glBindBuffer g4 (:type buffer) 0)
    (assoc buffer
           :format :short
           :size size
           :direct direct-shorts)))

(defn allocate-buffer-storage [^GL4 g4 buffer number-of-bytes]
  (.glBindBuffer g4 (:type buffer) (:name buffer))
  (.glBufferStorage g4 (:type buffer) number-of-bytes nil
                    (bit-or GL/GL_MAP_WRITE_BIT
                            GL4/GL_MAP_PERSISTENT_BIT
                            GL4/GL_MAP_COHERENT_BIT))
  (.glBindBuffer g4 (:type buffer) 0)
  (assoc buffer :size number-of-bytes))

(defn map-buffer [app-state ^GL4 g4 buffer offset size]
  (add-gl-names app-state g4 :mapped-buffers [(:name buffer)])
  (.glMapNamedBufferRange g4 (:name buffer) offset size
                          (bit-or GL/GL_MAP_WRITE_BIT
                                  GL4/GL_MAP_PERSISTENT_BIT
                                  GL4/GL_MAP_COHERENT_BIT
                                  GL4/GL_MAP_FLUSH_EXPLICIT_BIT)))

(defn string-to-2d-array
  ^"[[Ljava.lang.CharSequence;"
  [matrix]
  (into-array
   (map (fn [array]
          (into-array array))
        [[matrix]])))

(defn make-program [gl-state g4 ^String vertex-source ^String fragment-source]
  (let [vertex-shader (new ShaderCode GL4/GL_VERTEX_SHADER 1 (string-to-2d-array vertex-source))
        fragment-shader (new ShaderCode GL4/GL_FRAGMENT_SHADER 1 (string-to-2d-array fragment-source))
        shader-program (new ShaderProgram)]
    (when-not (.add shader-program g4 vertex-shader System/err)
      (println "vertex shader ^^^vvv")
      (println vertex-source))
    (when-not (.add shader-program g4 fragment-shader System/err)
      (println "fragment shader ^^^vvv")
      (println fragment-source))
    (.init shader-program g4)
    (.link shader-program g4 System/err)
    {:gl-state (add-gl-names gl-state g4 :programs [(.program shader-program)])
     :gl-program {:name (.program shader-program)
                  :value shader-program}}))

(defn bind-program-buffers [^GL4 g4 program]
  (doall (map (fn [buffer]
                (let [type (:type buffer)
                      binding (:binding buffer)
                      name (:name buffer)]
                  (.glBindBufferBase g4 type binding name))) (:buffers program))))

(defn draw-vertex-array-with-program [^GL4 g4 vertex-array program]
  (.glUseProgram g4 (:name program))
  (bind-program-buffers g4 program)
  (.glBindVertexArray g4 (:name vertex-array))
  ;; (doall (map (fn [attribute]
  ;;               (.glEnableVertexArrayAttrib g4 (:name vertex-array) (:index attribute))) (:attributes vertex-array)))
  (let [geometry (:geometry vertex-array)]
    (.glDrawElements g4
                     (:element-type geometry)
                     (:element-count geometry)
                     (:element-format geometry)
                     0))
  (.glUseProgram g4 0)
  (.glBindVertexArray g4 0))
