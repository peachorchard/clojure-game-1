(ns com.daleroyer.clojure.2djogl.apps.2d-example-1.app
  (:require [com.daleroyer.clojure.2djogl.apps.base.state :as state])
  (:require [com.daleroyer.clojure.2djogl.apps.2d-example-1.init :refer [init]])
  (:require [com.daleroyer.clojure.2djogl.apps.2d-example-1.reshape :refer [reshape]])
  (:require [com.daleroyer.clojure.2djogl.apps.2d-example-1.draw :refer [draw]])
  (:require [com.daleroyer.clojure.2djogl.apps.base.app :refer [create-main-window]]))

(def app-state (state/create))

(defn app [& _] ;; args]
  (create-main-window app-state
                      {:window-title "2D Example 1"
                       :window-size {:width 1280 :height 720}
                       :init init
                       :reshape reshape
                       :draw draw
                       :debug false}))
