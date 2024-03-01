(ns com.daleroyer.clojure.2djogl.apps.2d-game-1.graphics.frame-sheet
  (:import [org.joml Matrix4f])
  (:require [clojure.string :as str])
  (:require [com.daleroyer.clojure.2djogl.apps.2d-game-1.graphics.shapes :as shapes])
  (:require [com.daleroyer.clojure.svg.svg :as svg])
  (:require [com.daleroyer.clojure.2djogl.apps.2d-game-1.graphics.transforms :as transforms]))

(defn label-number [prefix-str label-str]
  (let [number-string (nth (re-matches (re-pattern (str "(" prefix-str ")(\\d+).*")) label-str) 2)]
    (when number-string (Integer/parseInt number-string))))

(defn nodes-hierarchy-with-label-start [root-node start-string]
  (filter (fn [n] (let [s (svg/attr :label (:node n))]
                    (when s
                      (str/starts-with? s start-string))))
          (svg/all-nodes-with-attr-and-hierarchy :label root-node)))

(defn indexed-nodes-with-hierarchy [svg-node prefix]
  (let [nodes (nodes-hierarchy-with-label-start svg-node prefix)]
    (into {}
          (map (fn [n]
                 [(label-number prefix (svg/attr :label (:node n)))
                  n])
               nodes))))

(defn attr-rgba [attr-prefix svg-node]
  (assoc (or (svg/style-item-rgb attr-prefix svg-node) {:red 0 :green 0 :blue 0})
         :alpha (or (svg/style-item-float (str attr-prefix "-opacity") svg-node) 1)))

(defn parse-shape-graphic [shape-node]
  (cond
    (shapes/circle? shape-node) [{:shape :circle
                                  :center-x (svg/attr-float :cx shape-node)
                                  :center-y (svg/attr-float :cy shape-node)
                                  :radius (svg/attr-float :r shape-node)
                                  :stroke-rgba (attr-rgba "stroke" shape-node)
                                  :stroke-width (svg/style-item-float "stroke-width"
                                                                      shape-node)
                                  :fill-rgba (attr-rgba "fill" shape-node)}]
    (shapes/ellipse? shape-node) [{:shape :ellipse
                                   :center-x (svg/attr-float :cx shape-node)
                                   :center-y (svg/attr-float :cy shape-node)
                                   :radius-x (svg/attr-float :rx shape-node)
                                   :radius-y (svg/attr-float :ry shape-node)
                                   :stroke-rgba (attr-rgba "stroke" shape-node)
                                   :stroke-width (svg/style-item-float "stroke-width"
                                                                       shape-node)
                                   :fill-rgba (attr-rgba "fill" shape-node)}]
    (shapes/rect? shape-node) [{:shape :rect
                                :x (svg/attr-float :x shape-node)
                                :y (svg/attr-float :y shape-node)
                                :width (svg/attr-float :width shape-node)
                                :height (svg/attr-float :height shape-node)
                                :stroke-rgba (attr-rgba "stroke" shape-node)
                                :stroke-width (svg/style-item-float "stroke-width"
                                                                    shape-node)
                                :fill-rgba (attr-rgba "fill" shape-node)}]
    (shapes/path? shape-node) (map (fn [n]
                                     (cond (= :line (:shape n))
                                           (assoc n
                                                  :start (:start n)
                                                  :end (:end n)
                                                  (= :triangle (:shape n))
                                                  (assoc n
                                                         :a (second (:a n))
                                                         :b (second (:b n))
                                                         :c (second (:c n))
                                                         :else n)
                                                  :stroke-rgba (attr-rgba "stroke" shape-node)
                                                  :stroke-width (svg/stroke-width shape-node)
                                                  :fill-rgba (attr-rgba "fill" shape-node)
                                                  :stroke-linecap (svg/stroke-linecap shape-node)
                                                  :stroke-linejoin (svg/stroke-linejoin shape-node))))
                                   (svg/parse-path (svg/attr :d shape-node)
                                                   {:width (svg/stroke-width shape-node)
                                                    :linecap (svg/stroke-linecap shape-node)
                                                    :linejoin (svg/stroke-linejoin shape-node)}))
    :else (println "unknown shape:" shape-node)))

(defn add-shape-property [shapes property]
  (for [s shapes]
    (assoc s :properties (conj (or (:properties s) []) property))))

(defn add-shape-properties [shapes first-string rest-strings]
  (if first-string
    (let [property (cond (= "solid" first-string)
                         :solid)
          modified-shapes (if property
                            (add-shape-property shapes property)
                            shapes)]
      (if rest-strings
        (add-shape-properties modified-shapes (first rest-strings) (rest rest-strings))
        modified-shapes))
    shapes))

(defn parse-shape-properties [shapes shape-node]
  (let [label (svg/attr :label shape-node)]
    (if label
      (let [matcher (re-matcher #".*\[\s*(\S+)(?:(?:\s+)([\S][^\]]+))*\]" label)]
        (if (.matches matcher)
          (let [args (re-groups matcher)]
            (add-shape-properties shapes (first args) (rest args)))
          shapes))
      shapes)))

(defn parse-shape [shape-node]
  (-> (parse-shape-graphic shape-node)
      (parse-shape-properties shape-node)))

(defn parse-transforms [node shape]
  (let [transforms (svg/transform-items node)]
    (if (and transforms (> (count transforms) 0))
      (assoc shape :svg-transforms [transforms])
      shape)))

(defn transforms [svg-node]
  (let [transforms (svg/transform-items svg-node)]
    (if (and transforms (> (count transforms) 0))
      [transforms]
      [])))

(defn hierarchy-transforms [node-and-parent]
  (if (:parent node-and-parent)
    (concat
     (hierarchy-transforms (:parent node-and-parent))
     (transforms (:node node-and-parent)))
    (transforms (:node node-and-parent))))

(defn get-properties-text [shape-node]
  (let [label (svg/attr :label (:node shape-node))]
    (when label
      (let [result (second (re-find #"\[(.+)\]" label))]
        result))))

(defn display? [shape-node]
  (let [style-string (svg/attr :style shape-node)]
    (not (clojure.string/includes? style-string "display:none;"))))

(defn parse-shapes [shape-nodes]
  (mapcat
   (fn [shape-node]
     (remove nil?
             (for [s (parse-shape (:node shape-node))
                   :let [parsed (parse-transforms (:node shape-node) s)]]
               (let [result (assoc
                             (assoc parsed
                                    :transform
                                    (transforms/make-transform-matrix
                                     (new Matrix4f)
                                     (hierarchy-transforms shape-node)))
                             :properties-text (get-properties-text shape-node)
                             :display (display? (:node shape-node)))]
                 result))))
   shape-nodes))

(defn get-solids [shapes ^Matrix4f frame-transform]
  (remove nil?
          (for [s shapes]
            (when (and
                   (:properties-text s)
                   (clojure.string/includes? (:properties-text s) "solid"))
              (assoc s
                     :transform (.mul frame-transform
                                      ^Matrix4f (:transform s)
                                      (new Matrix4f)))))))

(defn frames [svg-node]
  (let [iframe-nodes (indexed-nodes-with-hierarchy svg-node "frame-")
        ibounds-nodes (indexed-nodes-with-hierarchy svg-node "bounds-")]
    (into (sorted-map)
          (mapv (fn [iframe-node]
                  (let [frame-number (first iframe-node)
                        frame-node (second iframe-node)
                        frame-bounds-node-and-parent (ibounds-nodes frame-number)
                        bounds-transforms (hierarchy-transforms frame-bounds-node-and-parent)
                        frame-x (svg/attr-float :x (:node frame-bounds-node-and-parent))
                        frame-y (svg/attr-float :y (:node frame-bounds-node-and-parent))
                        frame-height (svg/attr-float :height (:node frame-bounds-node-and-parent))
                        frame-matrix (transforms/make-transform-matrix
                                      (new Matrix4f)
                                      bounds-transforms)
                        location (transforms/apply-transforms4 [frame-x frame-y
                                                                0 1]
                                                               frame-matrix)
                        shapes-node (first (svg/all-nodes-with-hierarchy
                                            (:node frame-node)
                                            (fn [n] (= (str "shapes-" frame-number)
                                                       (svg/attr :label n))) (:parent frame-node)))
                        raw-shapes (svg/all-nodes-with-hierarchy
                                    (:node shapes-node)
                                    shapes/shape? (:parent shapes-node))
                        shapes (parse-shapes raw-shapes)
                        displayed-shapes (filter #(:display %) shapes)
                        frame-transform (.mul
                                         (.scale (new Matrix4f)
                                                 1 -1 1)
                                         (.translate (new Matrix4f)
                                                     (- (first location))
                                                     (- (- (second location))
                                                        frame-height)
                                                     0
                                                     (new Matrix4f))
                                         (new Matrix4f))
                        solids (get-solids (parse-shapes (svg/all-nodes-with-hierarchy
                                                          (:node frame-node)
                                                          shapes/shape? (:parent frame-node)))
                                           frame-transform)]
                    [frame-number {:transform frame-transform
                                   :shapes displayed-shapes
                                   :solids solids}]))
               iframe-nodes))))

(defn animations [svg-node]
  (let [animation-nodes (nodes-hierarchy-with-label-start svg-node "animation-")]
    (into (sorted-map)
          (map (fn [animation-node]
                 (let [animation-name (second (re-find #"animation-(.+)"
                                                       (svg/attr :label (:node animation-node))))]
                   [animation-name {:frames (frames (:node animation-node))}]))
               animation-nodes))))