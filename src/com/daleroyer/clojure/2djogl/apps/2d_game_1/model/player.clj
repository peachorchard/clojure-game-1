(ns com.daleroyer.clojure.2djogl.apps.2d-game-1.model.player
  (:require [com.daleroyer.clojure.2djogl.apps.2d-game-1.physics.object :as physics]
            [com.daleroyer.clojure.2djogl.apps.2d-game-1.events :as events])
  (:require [com.daleroyer.clojure.2djogl.apps.2d-game-1.sound.clip :as clip]))

(defn stopping-x? [player-state]
  (not (or (:moving-left player-state)
           (:moving-right player-state))))

(defn acc-x [player player-state]
  (cond (and (:moving-left player-state)
             (not (:moving-right player-state)))
        (- (:max-acc-x player))
        (and (:moving-right player-state)
             (not (:moving-left player-state)))
        (:max-acc-x player)
        :else
        0))

(defn current-animation [player]
  (cond (pos? (:vel-x player))
        ((:animations player) "walk-right")
        (neg? (:vel-x player))
        ((:animations player) "walk-left")
        :else
        ((:animations player) "stand")))

(defn update-player [player game-state]
  (let [player-state (:player-state game-state)
        ax (acc-x player player-state)]
    (assoc player
           :forces
           (remove nil? [(when (not (stopping-x? player-state))
                           {:acc-x ax})])
           :dampers
           (remove nil? [(when (stopping-x? player-state)
                           {:stop-x (:max-acc-x player)})])
           :active-animation
           (current-animation player))))

(defn make-player [animations]
  (assoc (physics/add-physics
          {:player-controlled true
           :animations animations
           :active-animation (animations "stand")
           :update-fn update-player})
         :max-acc-x 2000
         :max-acc-y 2000
         :max-vel-x 200
         :max-vel-y 1000))

(defn jump [game-state enabled?]
  (if enabled?
    (do
      (when (some #(and (:player-controlled %)
                        (:on-ground %)) (:objects game-state))
        (events/post-game-event {:function clip/play
                                 :args [(:chirp (:sounds game-state))]}))
      (assoc game-state
             :objects
             (for [o (:objects game-state)]
               (if (:player-controlled o)
                 (if (:on-ground o)
                   (assoc o
                          :vel-y (+ (:vel-y o) 500))
                   o)
                 o))))
    game-state))

(defn move-x [player-state x]
  (cond (neg? x) (-> player-state
                     (assoc :moving-left true)
                     (assoc :moving-right false))
        (pos? x) (-> player-state
                     (assoc :moving-left false)
                     (assoc  :moving-right true))
        :else (->  player-state
                   (assoc :moving-left false)
                   (assoc :moving-right false))))

(defn move-y [player-state y]
  (cond (neg? y) (-> player-state
                     (assoc :moving-up false)
                     (assoc :moving-down true))
        (pos? y) (-> player-state
                     (assoc :moving-up true)
                     (assoc :moving-down false))
        :else
        (-> player-state
            (assoc :moving-up false)
            (assoc :moving-down false))))

(defn move-xy [game-state [x y]]
  (assoc game-state :player-state
         (-> (:player-state game-state)
             (move-x x)
             (move-y y))))

(defn move-up [game-state enabled?]
  (assoc game-state :player-state
         (assoc (:player-state game-state) :moving-up enabled?)))

(defn move-down [game-state enabled?]
  (assoc game-state :player-state
         (assoc (:player-state game-state) :moving-down enabled?)))

(defn move-left [game-state enabled?]
  (assoc game-state :player-state
         (assoc (:player-state game-state) :moving-left enabled?)))

(defn move-right [game-state enabled?]
  (assoc game-state :player-state
         (assoc (:player-state game-state) :moving-right enabled?)))
