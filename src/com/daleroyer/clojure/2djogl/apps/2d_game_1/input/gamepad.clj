(ns com.daleroyer.clojure.2djogl.apps.2d-game-1.input.gamepad
  (:import [net.java.games.input Event EventQueue ControllerEnvironment Controller
            Controller$Type Component Component$Identifier$Axis Component$Identifier$Button]) 
  (:require [com.daleroyer.clojure.2djogl.apps.2d-game-1.events :as events]))

(def epsilon 1e-6)

(defn init-controllers [game-state]
  (assoc game-state
         :controllers
         (.getControllers (ControllerEnvironment/getDefaultEnvironment))))

(defn get-component-type-axis [^Component component]
  (let [id (str (.getIdentifier component))]
    (cond
      (= id (str Component$Identifier$Axis/POV)) :axis-pov
      (= id (str Component$Identifier$Axis/X)) :axis
      (= id (str Component$Identifier$Axis/Y)) :axis
      :else nil)))

(defn get-component-type-button [^Component component]
  (let [id (str (.getIdentifier component))]
    (cond
      (= id (str Component$Identifier$Button/A)) :button
      (= id (str Component$Identifier$Button/B)) :button
      (= id (str Component$Identifier$Button/X)) :button
      (= id (str Component$Identifier$Button/Y)) :button
      :else nil)))


(defn get-component-type [^Component component]
  (or
   (get-component-type-axis component)
   (get-component-type-button component)
   :unknown))

(defn get-component-value [^Component component previous-value]
  (if (.isRelative component)
    (+ (.getPollData component)
       previous-value)
    (.getPollData component)))

(defn process-component [^Component component last-state]
  (let [type (get-component-type component)]
    {:analog (.isAnalog component)
     :type type
     :name (str (.getIdentifier component))
     :value (get-component-value component (:value last-state))}))

(defn get-pov [value]
  (if (zero? value) {:x 0 :y 0}
      {:x (* -1 (Math/cos (* value (* 2 Math/PI))))
       :y (Math/sin (* value (* 2 Math/PI)))}))

(defn make-button-event [gamepad-code value]
  {:gamepad-code gamepad-code
   :value (> value 0)})

(defn make-gamepad-event [state]
  (case (:type state)
    :axis-pov (let [xy (get-pov (:value state))
                    x (if (> (abs (:x xy)) epsilon) (:x xy) 0)
                    y (if (> (abs (:y xy)) epsilon) (:y xy) 0)]
                {:gamepad-code :gamepad-xy
                 :value [x y]})
    :button (cond
              (= (str Component$Identifier$Button/A) (:name state))
              (make-button-event :gamepad-a (:value state)))
    nil))

(defn poll-controllers [game-state]
  (let [updated-game-state (atom game-state)]
    (doseq [^Controller controller (:controllers @updated-game-state)]
      (when (= (.getType controller) Controller$Type/GAMEPAD)
        (.poll controller)
        (let [^EventQueue queue (.getEventQueue controller)
              event (Event.)]
          (while (.getNextEvent queue event)
            (let [^Component comp (.getComponent event)
                  updated-gamepad-state (process-component comp
                                                           ((or ((or (:controller-states
                                                                      @updated-game-state) {})
                                                                 controller) {})
                                                            (.getIdentifier comp)))
                  gamepad-event (make-gamepad-event updated-gamepad-state)]
              (events/gamepad-state-change (:gamepad-code gamepad-event)
                                           (:value gamepad-event)))))))
    @updated-game-state))
