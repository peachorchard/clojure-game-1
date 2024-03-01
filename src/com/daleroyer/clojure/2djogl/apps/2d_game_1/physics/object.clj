(ns com.daleroyer.clojure.2djogl.apps.2d-game-1.physics.object
  (:import [org.joml Matrix4f])
  (:require [com.daleroyer.clojure.2djogl.apps.2d-game-1.graphics.transforms :as transforms]))

(defn add-physics [object]
  (assoc object
         :x 0
         :y 0
         :vel-x 0
         :vel-y 0
         :max-acc-x 0
         :max-acc-y 0
         :max-vel-x 0
         :max-vel-y 0))

(defn get-transform [object]
  (.translate (new Matrix4f)
              (:x object)
              (:y object)
              0))

(defn set-position [object x y]
  (assoc object
         :x x
         :y y))

(defn clamp [value min-value max-value]
  (max min-value
       (min max-value value)))

(defn update-vel-x [object dt]
  (let [vx (clamp (+ (:vel-x object) (* dt (:acc-x object)))
                  (- (:max-vel-x object)) (:max-vel-x object))]
    (assoc object
           :vel-x vx)))

(defn update-vel-y [object dt]
  (let [vy (clamp (+ (:vel-y object) (* dt (:acc-y object)))
                  (- (:max-vel-y object)) (:max-vel-y object))]
    (assoc object
           :vel-y vy)))

(defn set-acc-x [object ax]
  (assoc object
         :acc-x
         (clamp ax
                (- (:max-acc-x object)) (:max-acc-x object))))

(defn set-acc-y [object ay]
  (assoc object
         :acc-y
         (clamp ay
                (- (:max-acc-y object)) (:max-acc-y object))))

(defn set-vel-x [object vx]
  (assoc object 
         :vel-x
         (clamp vx
                (- (:max-vel-x object)) (:max-vel-x object))))

(defn set-vel-y [object vy]
  (assoc object
         :vel-y
         (clamp vy
                (- (:max-vel-y object)) (:max-vel-y object))))

(defn apply-force [object force]
  (cond (:acc-y force)
        (apply-force (set-acc-y object (+ (:acc-y object) (:acc-y force)))
                     (dissoc force :acc-y))
        (:acc-x force)
        (apply-force (set-acc-x object (+ (:acc-x object) (:acc-x force)))
                     (dissoc force :acc-x))
        (:vel-x force)
        (apply-force (set-vel-x object (+ (:vel-x object) (:vel-x force)))
                     (dissoc force :vel-x))
        (:vel-y force)
        (apply-force (set-vel-y object (+ (:vel-y object) (:vel-y force)))
                     (dissoc force :vel-y))
        :else object))

(defn apply-forces [object forces]
  (if forces
    (if (pos? (count forces))
      (apply-forces (apply-force object (first forces))
                    (rest forces))
      object)
    object))

(defn apply-damper [object damper initial-object]
  (cond (and (:stop-x damper)
             (not (zero? (:stop-x damper))))
        (let [damp-x (cond (neg? (:vel-x object))
                           (:stop-x damper)
                           (pos? (:vel-x object))
                           (- (:stop-x damper))
                           :else
                           0)
              vx (+ (:vel-x object) damp-x)]
          (if (or (and (<= vx 0) (>= (:vel-x initial-object) 0))
                  (and (>= vx 0) (<= (:vel-x initial-object) 0)))
            (apply-damper
             (assoc object
                    :vel-x 0
                    :acc-x 0)
             (dissoc damper :stop-x)
             initial-object)
            (assoc object
                   :vel-x vx)))
        :else
        object))

(defn apply-dampers [object dampers initial-object]
  (if dampers
    (if (pos? (count dampers))
      (apply-dampers (apply-damper object (first dampers) initial-object)
                     (rest dampers) initial-object)
      object)
    object))

(defn update-motion [object time global-forces]
  (let [forces (:forces object)
        dampers (:dampers object)]
    (-> object
        (apply-forces global-forces)
        (apply-forces forces)
        (apply-dampers dampers object)
        (update-vel-x (:delta-time time))
        (update-vel-y (:delta-time time)))))

(defn current-solids [object]
  (mapv (fn [s]
          (assoc s
                 :player-controlled (:player-controlled object)
                 :transform (.mul ^Matrix4f (get-transform object)
                                  ^Matrix4f (:transform s)
                                  (new Matrix4f))))
        (:solids (:active-frame object))))

(defn ascending [a b]
  (if (<= a b)
    [a b]
    [b a]))

(defn shape-aabb [shape]
  (case (:shape shape)
    :circle (println "circle")
    :ellipse (println "ellipse")
    :rect (let [shape-transform (:transform shape)
                p1 (transforms/apply-transforms4
                    [(:x shape) (:y shape) 0 1]
                    shape-transform)
                x1 (first p1)
                y1 (second p1)
                p2 (transforms/apply-transforms4
                    [(+ (:x shape) (:width shape))
                     (+ (:y shape) (:height shape)) 0 1]
                    shape-transform)
                x2 (first p2)
                y2 (second p2)
                xs (ascending x1 x2)
                ys (ascending y1 y2)]
            {:x1 (first xs) :x2 (second xs)
             :y1 (first ys) :y2 (second ys)})))

(defn range-overlaps? [a b]
  (let [s (if (< (first a) (first b))
            {:a a
             :b b}
            {:a b
             :b a})]
    (and (<= (first (:a s)) (second (:b s)))
         (>= (second (:a s)) (first (:b s))))))

(defn collides-aabb? [a b]
  (and (range-overlaps? (ascending (:x1 a) (:x2 a))
                        (ascending (:x1 b) (:x2 b)))
       (range-overlaps? (ascending (:y1 a) (:y2 a))
                        (ascending (:y1 b) (:y2 b)))))

(defn collides-aabbs? [a b]
  (some true?
        (mapv (fn [aa]
                (some true?
                      (mapv (fn [bb]
                              (collides-aabb? aa bb))
                            b)))
              a)))

(defn union-aabb
  ([] nil)
  ([aabb]
   aabb)
  ([aabb-a aabb-b]
     {:x1 (min (:x1 aabb-a) (:x2 aabb-a) (:x1 aabb-b) (:x2 aabb-b))
      :y1 (min (:y1 aabb-a) (:y2 aabb-a) (:y1 aabb-b) (:y2 aabb-b))
      :x2 (max (:x1 aabb-a) (:x2 aabb-a) (:x1 aabb-b) (:x2 aabb-b))
      :y2 (max (:y1 aabb-a) (:y2 aabb-a) (:y1 aabb-b) (:y2 aabb-b))}))

(defn with-updated-position [object time]
  (let [dx (* (:vel-x object) (:delta-time time))
        dy (* (:vel-y object) (:delta-time time))
        x (+ (:x object) dx)
        y (+ (:y object) dy)]
    (set-position object x y)))

(defn sweep-object-aabb [object time]
  (let [solids (current-solids object)
        moved-object (with-updated-position object time)
        moved-solids (current-solids moved-object)
        aabbs (for [s solids] (shape-aabb s))
        moved-aabbs (for [s moved-solids] (shape-aabb s))
        result (reduce union-aabb
                       (mapv (fn [i]
                               (union-aabb (nth aabbs i) (nth moved-aabbs i)))
                             (range (count aabbs))))]
    result))

(defn with-collision-aabbs [object other-aabbs time]
  (if (collides-aabbs? [(sweep-object-aabb (assoc object :vel-y 0) time)]
                       other-aabbs)
    ;; collision x maybe y
    (if (collides-aabbs? [(sweep-object-aabb (assoc object :vel-x 0) time)]
                         other-aabbs)
      ;; collision x and y
      (with-updated-position
        (assoc object
               :vel-x 0
               :vel-y 0
               :on-ground (neg? (:vel-y object)))
        time)
      ;; collision x only
      (with-updated-position (assoc object
                                    :vel-x 0
                                    :on-ground false) 
        time))
    ;; collision y only
    (with-updated-position (assoc object
                                  :vel-y 0
                                  :on-ground (neg? (:vel-y object))) 
      time)))

(defn move-object [object other-objects time]
  (let [swept-aabbs [(sweep-object-aabb object time)]
        other-aabbs (apply concat
                           (mapv (fn [o]
                                   (mapv (fn [solid]
                                           (shape-aabb solid))
                                         (current-solids o)))
                                 other-objects))]
    (if (collides-aabbs? swept-aabbs other-aabbs)
      (with-collision-aabbs object other-aabbs time)
      (if (not (zero? (:vel-y object)))
        (with-updated-position (assoc object
                                      :on-ground false) time)
        (with-updated-position object time)))))

(defn update-object [object other-objects time global-forces]
  (if (not (zero? (:delta-time time)))
    (move-object
     (update-motion object time global-forces)
     other-objects time)
    object))
