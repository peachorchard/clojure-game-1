(ns com.daleroyer.clojure.2djogl.apps.2d-game-1.events
  (:require [clojure.core.async :as a :refer [<!! >!! close!]]) ;;<! >!
  )

(def events-queue
  (a/chan 1000))

(def input-queue
  (a/chan 1000))

(defn close []
  (close! events-queue)
  (close! input-queue))

(defn enqueue-event [event]
  (>!! events-queue event))

(defn dequeue-event []
  (<!! events-queue))

(defn enqueue-input [event]
  (>!! input-queue event))

(defn dequeue-input []
  (<!! input-queue))


(defn post-game-event [event]
  (enqueue-event event))

(defn generate-keyboard-game-event [game-state key-code value]
  (let [key-actions (:key-codes (:actions game-state))
        key-action (and key-actions (key-actions key-code))]
    (when key-action
      (post-game-event {:function (:action key-action)
                        :args [value]}))))

(defn generate-gamepad-game-event [game-state gamepad-code value]
  (let [gamepad-actions (:gamepad-codes (:actions game-state))
        gamepad-action (and gamepad-actions (gamepad-actions gamepad-code))]
    (when (:action gamepad-action)
      (post-game-event {:function (:action gamepad-action)
                        :args [value]}))))

(defn key-state-change [key-code value]
  (enqueue-input {:type :keyboard
                  :key-code key-code
                  :value value}))

(defn update-keyboard-state [game-state key-code value]
  (generate-keyboard-game-event game-state key-code value)
  (assoc game-state
         :keyboard-state
         (assoc (:keyboard-state game-state)
                :key-states (assoc (:key-states (:keyboard-state game-state))
                                   key-code value))))

(defn post-keyboard-event [event]
  (post-game-event
   {:function update-keyboard-state
    :args [(:key-code event)
           (:value event)]}))

(defn gamepad-state-change [gamepad-code value]
  (enqueue-input
           {:type :gamepad
            :gamepad-code gamepad-code
            :value value}))

(defn update-gamepad-state [game-state gamepad-code value]
  (generate-gamepad-game-event game-state gamepad-code value)
  (assoc game-state
         :controller-states (assoc (or (:controller-states
                                        game-state) {})
                                   gamepad-code value)))

(defn post-gamepad-event [event]
  (post-game-event
   {:function update-gamepad-state
    :args [(:gamepad-code event)
           (:value event)]}))
