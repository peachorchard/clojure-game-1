(ns com.daleroyer.clojure.2djogl.apps.2d-template.app
  (:require [com.daleroyer.clojure.2djogl.apps.base.state :as state])
  (:require [com.daleroyer.clojure.2djogl.apps.2d-template.init :refer [init]])
  (:require [com.daleroyer.clojure.2djogl.apps.2d-template.reshape :refer [reshape]])
  (:require [com.daleroyer.clojure.2djogl.apps.2d-template.draw :refer [draw]])
  (:require [com.daleroyer.clojure.2djogl.apps.base.app :refer [create-main-window]]))

(def app-state (state/create))

(defn -main [& _];; args]
  (create-main-window app-state
                      {:window-title "2D Template"
                       :window-size {:width 1280 :height 720}
                       :init init
                       :reshape reshape
                       :draw draw
                       :debug true}))
