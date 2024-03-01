(ns com.daleroyer.clojure.2djogl.jogl.buffers)

(defn update-float-buffer 
  ([pointer values] (update-float-buffer pointer values 0))
  ([pointer values offset]
  (dotimes [index 16] 
    (.putFloat pointer (+ offset (* index 4)) (aget values index)))))
