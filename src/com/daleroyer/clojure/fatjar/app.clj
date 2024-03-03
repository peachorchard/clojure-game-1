(ns com.daleroyer.clojure.fatjar.app
  (:require [clojure.java.io :as io])
  (:import [java.util.zip ZipFile ZipEntry ZipOutputStream]
           org.apache.commons.io.IOUtils))

(defn get-output-path [^String base ^String name]
  (if (zero? (.indexOf name base))
    name
    (str base name)))

(defn copy [^String from ^ZipOutputStream to ^String base skip-manifest?]
  (when-not (empty? base)
    (.putNextEntry to (ZipEntry. base)))
  (let [from (ZipFile. from)]
    (doseq [^ZipEntry entry (enumeration-seq (.entries from))
            :let [^String name (get-output-path base (.getName entry))]
            :when (or (not skip-manifest?)
                      (-> name .toLowerCase (.contains "meta-inf") false?))]
      (println name)
      (.putNextEntry to (ZipEntry. name))
      (IOUtils/copy (.getInputStream from entry) to))))

(defn process-jar [in-name out]
  (if-let [[_ type] (re-find #"natives-([^\.]+)" in-name)]
    (copy in-name out (str "natives/" type "/") true)
    (copy in-name out "" false)))

(defn fat-jar [in-name out-name]
  (with-open [out (ZipOutputStream. (io/output-stream out-name))]
    (process-jar in-name out)))

(defn app [jar-file-prefixes]
  (let [to-process jar-file-prefixes
        ;;[ "lib/jinput/jinput-platform-2.0.7-natives-linux"]
        ;; ["resources/jogamp-all-platforms/jar/gluegen-rt"
        ;;  "resources/jogamp-all-platforms/jar/gluegen-rt-natives-linux-amd64"
        ;;  "resources/jogamp-all-platforms/jar/jogl-all"
        ;;  "resources/jogamp-all-platforms/jar/jogl-all-natives-linux-amd64"]
        ]
  (doseq [b to-process] 
    (fat-jar (str b ".jar") (str b "-fat.jar")))))
