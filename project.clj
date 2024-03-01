(defproject clojure-2djogl "0.1.0-SNAPSHOT"
  :repositories {"local" {:url "file:lib" :username "" :password ""}}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/core.async "1.6.673"]
                 [dali "1.0.2"]
                 [org.clojure/data.xml "0.2.0-alpha9"]
                 ;;[org.clojure/data.xml "0.0.8"]
                 [org.joml/joml "1.10.5"]
                 [net.java.jinput/jinput "2.0.9"]
                 [org.openjfx/javafx-media "21.0.2"]]
  :resource-paths ["resources"]
  :profiles {:uberjar
             {:dependencies [[com.daleroyer/clojure.deps.gluegen-rt-fat.jar "1.0.0" :native-prefix ""]
                             [com.daleroyer/clojure.deps.gluegen-rt-natives-linux-amd64-fat.jar "1.0.0" :native-prefix ""]
                             [com.daleroyer/clojure.deps.jogl-all-fat.jar "1.0.0" :native-prefix ""]
                             [com.daleroyer/clojure.deps.jogl-all-natives-linux-amd64-fat.jar "1.0.0" :native-prefix ""]
                             [com.daleroyer/clojure.deps.jinput-platform-2.0.7-natives-linux.jar "1.0.0" :native-prefix ""]]
              :aot [com.daleroyer.clojure.entry]}
             :dev
             {:dependencies [[com.daleroyer/clojure.deps.gluegen-rt.jar "1.0.0" :native-prefix ""]
                             [com.daleroyer/clojure.deps.gluegen-rt-natives-linux-amd64.jar "1.0.0" :native-prefix ""]
                             [com.daleroyer/clojure.deps.jogl-all.jar "1.0.0" :native-prefix ""]
                             [com.daleroyer/clojure.deps.jogl-all-natives-linux-amd64.jar "1.0.0" :native-prefix ""]
                             [com.daleroyer/clojure.deps.jinput-platform-2.0.7-natives-linux.jar "1.0.0" :native-prefix ""]]}}
  :clean-targets ^{:protect false} ["target"]
  ;; :jvm-opts ["-Xverify:none"]  ;; for visualvm
  :jvm-opts ^:replace [] ;; optimize JVM and disable tiered compilation
  ;; Emit warnings on all reflection calls.
  :global-vars {*warn-on-reflection* true}
  :main com.daleroyer.clojure.entry)
