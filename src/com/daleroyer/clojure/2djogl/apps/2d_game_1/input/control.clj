(ns com.daleroyer.clojure.2djogl.apps.2d-game-1.input.control
  (:import [com.jogamp.newt.event KeyEvent])
  (:require [com.daleroyer.clojure.2djogl.apps.2d-game-1.model.player :as player]))

(defn initialize-actions [game-state]
  (assoc game-state
         :actions {:key-codes
                   {KeyEvent/VK_SPACE {:description "jump"
                                       :action player/jump}
                    KeyEvent/VK_UP {:description "move up"
                                    :action player/move-up}
                    KeyEvent/VK_DOWN {:description "move down"
                                      :action player/move-down}
                    KeyEvent/VK_LEFT {:description "move left"
                                      :action player/move-left}
                    KeyEvent/VK_RIGHT {:description "move right"
                                       :action player/move-right}}
                   :gamepad-codes
                   {:gamepad-a {:description "jump"
                                 :action player/jump}
                    :gamepad-xy {:description "move xy"
                                  :action player/move-xy}}}))
