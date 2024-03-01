(ns com.daleroyer.clojure.2djogl.apps.2d-game-1.graphics.shapes
  (:require [clojure.string :as str])
  (:require [com.daleroyer.clojure.svg.svg :as svg]))

(defn rect? [node]
  (svg/tag? :rect node))

(defn circle? [node]
  (svg/tag? :circle node))

(defn ellipse? [node]
  (svg/tag? :ellipse node))

(defn path? [node]
  (svg/tag? :path node))

(defn shape? [node]
  (or (circle? node)
      (rect? node)
      (path? node)
      (ellipse? node)))

(defn all-shapes [node]
  (svg/all-nodes node shape?))

(defn shape-type [node]
  (when (shape? node) (svg/get-tag node)))

(defn frames-shapes [node]
  (for [shapes-group (svg/all-nodes node 
                                    (fn [n] 
                                      (let [label (svg/attr :label n)]
                                        (when label (str/starts-with? label "shapes")))))]
    (all-shapes shapes-group)))