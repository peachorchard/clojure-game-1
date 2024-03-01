(ns com.daleroyer.clojure.2djogl.apps.2d-game-1.sound.clip
  (:import [javafx.scene.media AudioClip]))

(defn play [game-state ^AudioClip audio-clip]
  (.play audio-clip)
  game-state)
