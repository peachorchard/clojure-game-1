(ns com.daleroyer.clojure.2djogl.apps.rotating-rectangle.app
  (:require [com.daleroyer.clojure.2djogl.apps.base.state :as state])
  (:require [com.daleroyer.clojure.2djogl.apps.rotating-rectangle.init :refer [init]])
  (:require [com.daleroyer.clojure.2djogl.apps.rotating-rectangle.reshape :refer [reshape]])
  (:require [com.daleroyer.clojure.2djogl.apps.rotating-rectangle.draw :refer [draw]])
  (:require [com.daleroyer.clojure.2djogl.apps.base.app :refer [create-main-window]]))

(def app-state (state/create))

(defn app [& _] ;; args]
  (create-main-window app-state
                      {:window-title "Rotating Rectangle"
                       :window-size {:width 1280 :height 720}
                       :init init
                       :reshape reshape
                       :draw draw}))
