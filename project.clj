(defproject clojure-2djogl "0.1.0-SNAPSHOT"
  :resource-paths ["resources"]
  :profiles {:shared
             {:dependencies [[org.clojure/clojure "1.11.1"]
                             [org.clojure/core.async "1.6.673"]
                             [dali "1.0.2"]
                             [org.clojure/data.xml "0.2.0-alpha9"]
                             [org.joml/joml "1.10.5"]
                             [org.openjfx/javafx-media "21.0.2"]
                             ]
              :resource-paths ["3rdparty/jogamp-fat.jar"
                               "3rdparty/jinput-2.0.10.jar"
                               "3rdparty/jinput-2.0.10-natives-all.jar"]
              }
             :dev [:shared {:jvm-opts ["-Djava.library.path=3rdparty"]
                            :native-path "3rdparty"}]
             :uberjar [:shared
                       {:aot :all
                        :manifest {"Class-Path"
                                   "../../3rdparty/jogamp-fat.jar 
                                    ../../3rdparty/jinput-2.0.10.jar"}}]}
  :clean-targets ^{:protect false} ["target"]

  :target-path "target/%s/"

  :uberjar-exclusions [#"jogamp-fat.jar"
                       #"jinput-2.0.10.jar"
                       #"jinput-2.0.10-natives-all.jar"]

  ;; for visualvm
  ;; :jvm-opts ["-Xverify:none"]  

  ;; optimize JVM and disable tiered compilation
  ;;:jvm-opts ^:replace []

  ;; Emit warnings on all reflection calls.
  :global-vars {*warn-on-reflection* true}

  :main com.daleroyer.clojure.entry)
