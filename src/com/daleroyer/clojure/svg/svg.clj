(ns com.daleroyer.clojure.svg.svg
  (:require [clojure.java.io :as io])
  (:require [clojure.string :as str])
  (:require [com.daleroyer.clojure.xml.io :as xml-io])
  (:require [com.daleroyer.clojure.svg.initial :as initial]))

(defn key-like [m k]
  (let [s (name k)]
    (some #(when (str/starts-with? (name (key %)) s)
             (val %))
          m)))

(defn get-key [m k]
  (let [s (name k)]
    (some #(when (= (name (key %)) s)
             (val %))
          m)))

(defn get-tag [n]
  (when (map? n)
    (some-> n :tag name keyword)))

(defn tag-data [tag node]
  (when (map? node)
    (let [tag-data (tag node)]
      tag-data)))

(defn do-node [n]
  (filter some? (concat
                 (flatten [(get-tag n)])
                 (flatten (when (some? (tag-data :content n))
                            (mapv do-node (tag-data :content n)))))))

(defn tags [xml-data]
  (do-node xml-data))

(defn group? [node]
  (= :g (get-tag node)))

(defn node-attrs [node]
  (when (map? node)
    (tag-data :attrs node)))

(defn tag? [tag node]
  (= tag (get-tag node)))

(defn attr [attr node]
  (get-key (node-attrs node) attr))

(defn attr-float [attr node]
  (Float/parseFloat (get-key (node-attrs node) attr)))

(defn all-nodes
  ([node] (all-nodes node (fn [n] n)))
  ([node test]
   (let [sofar (if (test node) [node] [])
         content (tag-data :content node)]
     (if content
       (into sofar (for [content-node content
                         sub-groups (all-nodes content-node test)]
                     sub-groups))
       sofar))))

(defn all-nodes-with-hierarchy
  ([node] (all-nodes-with-hierarchy node (fn [n] n) nil))
  ([node test parent]
   (let [node-with-parent (if (:node node) node {:node node :parent parent})
         sofar (if (test (:node node-with-parent)) [node-with-parent] [])
         content (tag-data :content (:node node-with-parent))]
     (if content
       (into sofar (for [content-node content
                         sub-groups (all-nodes-with-hierarchy content-node test node-with-parent)]
                     sub-groups))
       sofar))))


(defn all-nodes-with-tag
  [node tag]
  (all-nodes node (fn [n] (tag? tag n))))

(defn all-nodes-with-attr
  [a node]
  (all-nodes node (fn [n] (some? (attr a n)))))

(defn all-nodes-with-attr-and-hierarchy
  [a node]
  (all-nodes-with-hierarchy node (fn [n] (some? (attr a n))) nil))

(defn content-nodes [node]
  (tag-data :content node))

(defn group-id [group]
  (:id (node-attrs group)))

(defn strip-namespace [n]
  (last (str/split n #"/")))

(defn node-label [node]
  (get-key (node-attrs node) :label))

(defn print-attrs-keys [node]
  (println node)
  (let [attrs (node-attrs node)]
    (when attrs
      (doall (map (fn [key] (println key)) (keys attrs))))))

(defn print-content-keys [node]
  (when (map? node) (apply println (keys (tag-data :content node)))))

(defn read-file [resource-filename]
  (let [svg-string (slurp (io/resource resource-filename))]
    (xml-io/parse-str svg-string)))

(defn style-item [name node]
  (let [style-str (attr :style node)]
    (when style-str
      (let [item-str (filter #(= name (first (str/split % #":"))) (str/split style-str #";"))]
        (when (seq item-str)
          (second (str/split
                   (first item-str)
                   #":")))))))

(defn transform-center [node]
  (let [tx-str (attr :transform-center-x node)
        ty-str (attr :transform-center-y node)]
    [(if tx-str (Float/parseFloat tx-str) 0)
     (if ty-str (Float/parseFloat ty-str) 0)
     0]))

(defn transform-items [node]
  (let [transform-str (attr :transform node)]
    (when transform-str
      (let [rot-str (second (re-find #"rotate\((.+)\)" transform-str))
            tra-str (re-find #"translate\((.+),(.+)\)" transform-str)
            sca-str (re-find #"scale\((.+),(.+)\)" transform-str)
            mat-str (re-find #"matrix\((.+),(.+),(.+),(.+),(.+),(.+)\)" transform-str)]
        (merge
         (when tra-str
           {:translate [(Float/parseFloat (nth tra-str 1))
                        (Float/parseFloat (nth tra-str 2))]})
         (when rot-str
           {:rotate (* Math/PI (/ (Float/parseFloat rot-str) 180))})
         (when sca-str
           {:scale [(Float/parseFloat (nth sca-str 1))
                    (Float/parseFloat (nth sca-str 2))]})
         (when mat-str
           {:matrix [(Float/parseFloat (nth mat-str 1))
                     (Float/parseFloat (nth mat-str 2))
                     (Float/parseFloat (nth mat-str 3))
                     (Float/parseFloat (nth mat-str 4))
                     (Float/parseFloat (nth mat-str 5))
                     (Float/parseFloat (nth mat-str 6))]}))))))

(defn parse-hex [hex]
  (if hex
    (let [byte-values (mapv
                       (fn [[x y]]
                         (Integer/parseInt (str x y) 16))
                       (partition 2 hex))
          values (mapv + byte-values (mapv #(* 256 %) (range (count byte-values))))]
      (reduce + values))
    0))

(defn svg-color-to-rgb [svg-color]
  (when (str/starts-with? svg-color "#")
    (let [hex-pairs (partition 2 (rest svg-color))]
      {:red (/ (parse-hex (first hex-pairs)) 255.0)
       :green (/ (parse-hex (second hex-pairs)) 255.0)
       :blue (/ (parse-hex (nth hex-pairs 2)) 255.0)})))

(defn style-item-rgb [name node]
  (svg-color-to-rgb (style-item name node)))

(defn style-item-float [name node]
  (let [item-data (style-item name node)]
    (when item-data (Float/parseFloat item-data))))

(defn path-cmd? [s]
  (or (= "d" s)
      (= "m" s)
      (= "M" s)
      (= "z" s)
      (= "Z" s)
      (= "l" s)
      (= "L" s)
      (= "h" s)
      (= "H" s)
      (= "v" s)
      (= "V" s)
      (= "c" s)
      (= "C" s)
      (= "s" s)
      (= "S" s)
      (= "q" s)
      (= "Q" s)
      (= "t" s)
      (= "T" s)
      (= "a" s)
      (= "A" s)))

(defn parse-float [v]
  (when v
    (Float/parseFloat v)))

(defn parse-xy [xy-str]
  (when xy-str
    (let [xy (str/split xy-str #",")
          x (first xy)
          y (second xy)]
      (when (and x y)
        [(parse-float x)
         (parse-float y)]))))

(defn add-z [v]
  (conj v 0))

(defn next-cmd [begin position cmd args]
  (if (path-cmd? (first args))
    [begin position (first args) (rest args)]
    [begin position cmd args]))

(defn parse-cmd [begin position cmd args]
  (concat
   (case cmd
     "m"  ;; relative moveto
     (let [end (parse-xy (first args))]
       (when (seq (rest args))
         (apply parse-cmd (next-cmd (mapv + position end)
                                    (mapv + position end) cmd (concat ["l"] (rest args))))))

     "M"  ;; absolute moveto
     (let [end (parse-xy (first args))]
       (when (seq (rest args))
         (apply parse-cmd (next-cmd end end cmd (concat ["L"] (rest args))))))
     ;; (M 296,48 320,16)
     ;; end <- 296,48
     ;; cmd "M"
     ;; (L 296,48 320,16)
     ;; 296,48 296,48 L (320,16)

     "v" ;; relative vertical lineto
     (if position
       (let [end [0 (parse-float (first args))]]
         (if end (into [{:shape :line :start (add-z position) :end (add-z (mapv + position end))}]
                       (when (seq (rest args))
                         (apply parse-cmd (next-cmd begin (mapv + position end) cmd (rest args)))))
             (when (seq args) (apply parse-cmd (next-cmd begin position cmd args)))))
       (when (seq args) (apply parse-cmd (next-cmd begin (parse-xy (first args))
                                                   cmd (rest args)))))

     "V" ;; absolute vertical lineto
     (if position
       (let [end [0 (parse-float (first args))]]
         (if end (into [{:shape :line
                         :start (add-z position)
                         :end (add-z end)}]
                       (when (seq (rest args))
                         (apply parse-cmd (next-cmd begin end cmd (rest args)))))
             (when (seq args) (apply parse-cmd (next-cmd begin position cmd args)))))
       (when (seq args) (apply parse-cmd (next-cmd begin (parse-xy (first args))
                                                   cmd (rest args)))))

     "l" ;; relative lineto
     (let [end (parse-xy (first args))]
       (if end (into [{:shape :line
                       :start (add-z position)
                       :end (add-z (mapv + position end))}]
                     (when (seq (rest args))
                       (apply parse-cmd (next-cmd begin (mapv + position end) cmd (rest args)))))
           (when (seq args) (apply parse-cmd (next-cmd begin position cmd args)))))

     ;; 296,48 296,48 L (320,16)

     "L" ;; absolute lineto
     (let [end (parse-xy (first args))]
       (if end (into [{:shape :line
                       :start (add-z position)
                       :end (add-z end)}]
                     (when (seq (rest args))
                       (apply parse-cmd (next-cmd begin end cmd (rest args)))))
           (when (seq args) (apply parse-cmd (next-cmd begin position cmd args)))))

     "z" ;; close path (TODO: linecap linejoin)
     (into [{:shape :line
             :start (add-z position)
             :end (add-z begin)
             :closes true}]
           (when (seq (rest args))
             (apply parse-cmd (next-cmd begin position cmd (rest args)))))

     "Z" ;; close path (TODO: linecap linejoin)
     (into [{:shape :line
             :start (add-z position)
             :end (add-z begin)
             :closes true}]
           (when (seq (rest args))
             (apply parse-cmd (next-cmd begin position cmd (rest args)))))

     (println "unknown path element:" cmd))))

(defn line? [path-element]
  (= (:shape path-element) :line))

(defn mul-2d [s v]
  [(* s (first v)) (* s (second v))])

(defn add-2d [a b]
  [(+ (first a) (first b)) (+ (second a) (second b))])

(defn sub-2d [a b]
  [(- (first a) (first b)) (- (second a) (second b))])

(defn dot-2d [a b]
  (+ (* (first a) (first b))
     (* (second a) (second b))))

(defn length-2d [v]
  (Math/sqrt (dot-2d v v)))

(defn normalize-2d [v]
  (let [len (length-2d v)]
    (if (not (zero? len))
      [(/ (first v) len) (/ (second v) len)]
      v)))

(defn rays-intersection-2d [a-start a-dir b-start b-dir]
  (let [dx (- (first b-start) (first a-start))
        dy (- (second b-start) (second a-start))
        det (- (* (first b-dir) (second a-dir))
               (* (second b-dir) (first a-dir)))]
    (when (not (zero? det))
      (let [u (/ (- (* dy (first b-dir))
                    (* dx (second b-dir)))
                 det)
            v (/ (- (* dy (first a-dir))
                    (* dx (second a-dir)))
                 det)]
        (when (and (>= u 0) (>= v 0))
          (add-2d (mul-2d u b-dir) b-start)
          )
        ))))

(defn make-line-join [one two stroke-width]
  (let [half-stroke-width (/ stroke-width 2)
        bi-sector-point (mul-2d 0.5 (add-2d (:start one) (:end two)))
        bi-sector-v (normalize-2d (sub-2d (:end one) bi-sector-point))
        v-one (normalize-2d (sub-2d (:end one) (:start one)))
        v-two (normalize-2d (sub-2d (:start two) (:end two)))
        v-one-90 [(- (second v-one)) (first v-one)]
        v-two-90 [(- (second v-two)) (first v-two)]
        outer-one (if (pos? (dot-2d v-one-90 bi-sector-v))
                    (add-2d (mul-2d half-stroke-width v-one-90) (:end one))
                    (sub-2d (:end one) (mul-2d half-stroke-width v-one-90)))
        outer-two (if (pos? (dot-2d v-two-90 bi-sector-v))
                    (add-2d (mul-2d half-stroke-width v-two-90) (:start two))
                    (sub-2d (:start two) (mul-2d half-stroke-width v-two-90)))
        mid-miter-point (rays-intersection-2d outer-one v-one outer-two v-two)]
    (if mid-miter-point
      [{:shape :triangle
        :a mid-miter-point
        :b outer-one
        :c (:end one)}
       {:shape :triangle
        :a mid-miter-point
        :b (:end one)
        :c outer-two}]
      [])))

(defn process-line-ends [path-elements start-element stroke-width stroke-linecap stroke-linejoin]
  (let [one (first path-elements)
        two (second path-elements)
        start (cond
                (nil? start-element) one
                :else start-element)]
    (cond
      (and (line? one) (line? two) (= :miter stroke-linejoin))
      (concat
       [one]
       (make-line-join one two stroke-width)
       (process-line-ends (rest path-elements)
                          start
                          stroke-width
                          stroke-linecap
                          stroke-linejoin))

      (and (line? one) (:closes one) (= :miter stroke-linejoin))
      (concat
       [one]
       (make-line-join one start stroke-width)
       (process-line-ends (rest path-elements)
                          nil
                          stroke-width
                          stroke-linecap
                          stroke-linejoin))

      :else (cond (and one (seq (rest path-elements)))
                  (concat [one]
                          (process-line-ends (rest path-elements)
                                             start stroke-width stroke-linecap stroke-linejoin))
                  one [one]
                  :else []))))

(defn parse-path [path-string stroke]
  (when path-string
    (let [space-splits (str/split path-string #" ")
          cmd (first space-splits)
          args (rest space-splits)]
      (when (and cmd args)
        (process-line-ends
         (into [] (parse-cmd nil nil cmd args))
         nil
         (:width stroke)
         (:linecap stroke)
         (:linejoin stroke))))))

(defn stroke-width [node]
  (style-item-float "stroke-width" node))

(defn stroke-linecap [node]
  (or (attr :stroke-linecap node) (:stroke-linecap initial/values)))

(defn stroke-linejoin [node]
  (or (attr :stroke-linejoin node) (:stroke-linejoin initial/values)))

(defn document-bounds [document-node]
  {:x 0
   :y 0
   :width (attr-float :width document-node)
   :height (attr-float :height document-node)})