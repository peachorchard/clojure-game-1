(ns com.daleroyer.clojure.2djogl.apps.2d-game-1.app
  (:import [javafx.scene.media AudioClip])
  (:require [clojure.java.io :as io])
  (:require [com.daleroyer.clojure.2djogl.apps.2d-game-1.state :as state])
  (:require [com.daleroyer.clojure.2djogl.apps.2d-game-1.game-loop :as game-loop])
  (:require [com.daleroyer.clojure.svg.svg :as svg])
  (:require [com.daleroyer.clojure.2djogl.apps.2d-game-1.opengl.init :refer [init]])
  (:require [com.daleroyer.clojure.2djogl.apps.2d-game-1.opengl.reshape :refer [reshape]])
  (:require [com.daleroyer.clojure.2djogl.apps.2d-game-1.opengl.draw :refer [draw]])
  (:require [com.daleroyer.clojure.2djogl.apps.base.app :refer [create-main-window]])
  (:require [com.daleroyer.clojure.2djogl.apps.2d-game-1.input.keyboard :as keyboard])
  (:require [com.daleroyer.clojure.2djogl.apps.2d-game-1.graphics.frame-sheet :as frame-sheet])
  (:require [com.daleroyer.clojure.2djogl.apps.2d-game-1.events :as events]))

(def test-style "fill:none;stroke:#38d185;stroke-width:0.50000001;stroke-opacity:1;stroke-dasharray:none")

(def test-rect {:tag "rect"
                :attrs {:style test-style
                :id "rect-1"
                :width "46.123764"
                :height "46.123764"
                :x "42.982372"
                :y "81.836319"}})

(defn test-svg []
    (let [svg-color (svg/style-item-rgb "stroke" test-rect)
          svg-alpha (svg/style-item-float "stroke-opacity" test-rect)]
      (println (assoc svg-color :alpha svg-alpha))))

(defn test-regex []
  (let [label-str "frame-27"
        prefix-str "frame-"]
    ;;(println (nth (re-matches #"(frame-)(\d+)" label-str) 2))
    ;;(println (str "(" prefix-str ")(\\d+)"))
    (println (nth (re-matches (re-pattern (str "(" prefix-str ")(\\d+)")) label-str) 2))))

(defn load-animations [svg-filename]
  (let [svg-data (svg/read-file svg-filename)]
    (frame-sheet/animations svg-data)))

(defn load-graphics [game-state]
  (assoc game-state :animations
         (-> (:animations game-state)
             (assoc :player (load-animations "svg/character1.svg"))
             (assoc :map (load-animations "svg/map1.svg")))))

(defn load-sound-clip [path-str]
  (AudioClip. (.toString (io/resource path-str))))

(defn load-sounds [game-state]
  (assoc game-state :sounds
         {:chirp (load-sound-clip "audio/chirp.wav")}))

(defn make-main-window [app-state]
  (create-main-window app-state
                      {:window-title "2D Game 1"
                       :window-size {:width 1280 :height 720}
                       :init init
                       :reshape reshape
                       :draw [draw app-state]
                       :dispose (fn [gl-state
                                     _ ;;g4
                                     ]
                                  (swap! app-state assoc :should-quit true)
                                  (events/close)
                                  (while (not (:has-quit @app-state))
                                    (Thread/sleep 1))
                                  gl-state)
                       :key-down keyboard/key-down
                       :key-up keyboard/key-up
                       :debug true}))

(defn initialize-game-state [app-state]
  (swap! app-state assoc
         :game-state (-> {}
                         (load-graphics)
                         (load-sounds)))
  app-state)

(defn app [& _] ;;args]
  (.setProperty (java.lang.System/getProperties)
                "jinput.loglevel", "OFF")
  ;;(test-regex)
  ;; (let [svg-data (svg/read-file "2d_game_1/svg/characters/character1.svg")]
  ;;   (println (count (frame-sheet/frames svg-data)))
  ;;   (println (frame-sheet/frames svg-data))
  ;;   )

  (-> state/app-state
      (initialize-game-state)
      (game-loop/initialize-game-loop)
      (make-main-window)))
