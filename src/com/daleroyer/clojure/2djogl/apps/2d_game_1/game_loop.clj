(ns com.daleroyer.clojure.2djogl.apps.2d-game-1.game-loop
  (:require [clojure.core.async :as a])
  (:require [com.daleroyer.clojure.2djogl.apps.2d-game-1.events :as events])
  (:require [com.daleroyer.clojure.2djogl.apps.2d-game-1.input.control :as control])
  (:require [com.daleroyer.clojure.2djogl.apps.2d-game-1.physics.object :as physics])
  (:require [com.daleroyer.clojure.2djogl.apps.2d-game-1.input.gamepad :as gamepad])
  (:require [com.daleroyer.clojure.2djogl.apps.2d-game-1.graphics.animation :as animation])
  (:require [com.daleroyer.clojure.2djogl.apps.2d-game-1.model.map :as map])
  (:require [com.daleroyer.clojure.2djogl.apps.2d-game-1.model.player :as player]))

(defn update-time [game-state]
  (let [current-millis (java.lang.System/currentTimeMillis)
        previous-time (or (:time game-state) {:start-time (/ current-millis 1000)
                                              :current-time (/ current-millis 1000)
                                              :last-time-system current-millis})
        diff (- current-millis
                (:last-time-system previous-time))]
    (assoc game-state
           :time
           (assoc (if (>= diff 0)
                    (assoc previous-time
                           :start-time (or (:start-time previous-time)
                                           (:current-time previous-time))
                           :current-time (+ (/ diff 1000)
                                            (:current-time previous-time))
                           :delta-time (/ diff 1000))
                    previous-time)
                  :last-time-system current-millis))))

(defn update-objects [game-state]
  (assoc game-state 
         :objects
         (mapv (fn [o]
                 ((:update-fn o) o game-state))
               (:objects game-state))
         :player-objects
         (filter #(:player-controlled %) (:objects game-state))))

(defn update-physics-object [object others time global-forces]
  (physics/update-object
   (assoc object
          :acc-x 0
          :acc-y 0
          :forces (:forces object))
   others
   time
   global-forces))

(defn update-physics-objects
  ([game-state]
   (let [time (:time game-state)]
     (if (not (zero? (:delta-time time)))
       (assoc game-state :objects
              (update-physics-objects time
                                      (:global-forces game-state)
                                      (:objects game-state)))
       game-state)))
  ([time global-forces objects]
   (update-physics-objects time global-forces (first objects) [] (rest objects)))
  ([time global-forces object others-done others-not-done]
   (if object
     (let [object-done (update-physics-object
                        object
                        (concat others-done others-not-done)
                        time
                        global-forces)]
       (update-physics-objects time
                               global-forces
                               (first others-not-done)
                               (conj others-done object-done)
                               (rest others-not-done)))
     others-done)))

(defn input-loop [app-state]
  (while (not (:should-quit @app-state))
    (let [event (events/dequeue-input)]
      (when event
        (case (:type event)
          :keyboard (events/post-keyboard-event event)
          :gamepad (events/post-gamepad-event event))))))

(defn game-loop [game-state]
  (let [updated-game-state
        (-> game-state
            (update-time)
            (gamepad/poll-controllers)
            (update-objects)
            (animation/update-animations)
            (animation/update-active-shapes)
            (update-physics-objects))]
    (events/post-game-event {:function game-loop})
    updated-game-state))

(defn events-loop [app-state]
  (while (not (:should-quit @app-state))
    (let [event (events/dequeue-event)]
      (when event
        (let [game-state (:game-state @app-state)
              function (:function event)
              args (:args event)]
          (if args
            (swap! app-state assoc
                   :game-state (apply function (concat [game-state] args)))
            (swap! app-state assoc
                   :game-state (function game-state)))))))
  (swap! app-state assoc
         :has-quit true))

(defn add-example-map [game-state]
  (assoc game-state
         :objects
         (concat (:objects game-state)
                 [(map/make-map (:map (:animations game-state)))])))

(defn add-example-player [game-state]
  (assoc game-state
         :objects
         (concat (:objects game-state)
                 [(assoc
                   (player/make-player (:player (:animations game-state)))
                   :x 5
                   :y 150)])))

(defn initialize-game-state [game-state]
  (assoc
   game-state
   :objects []
   :global-forces [{:acc-y -900}]))

(defn start-threads [app-state]
  (a/thread (events-loop app-state))
  (a/thread (input-loop app-state))
  )

(defn initialize-game-loop [app-state]
  (swap! app-state assoc :game-state
         (-> (:game-state @app-state)
             (control/initialize-actions)
             (gamepad/init-controllers)
             (initialize-game-state)
             (add-example-map)
             (add-example-player)))
  (start-threads app-state)
  (events/post-game-event {:function game-loop})
  app-state)
