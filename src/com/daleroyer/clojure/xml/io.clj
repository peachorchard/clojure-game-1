(ns com.daleroyer.clojure.xml.io
  (:require [clojure.data.xml :as xml]))

(defn parse-str [s]
  (xml/parse-str s))