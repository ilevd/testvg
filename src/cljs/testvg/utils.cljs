(ns testvg.utils
  (:require [clojure.string :as string]))

(defn get-dividers [value]
  (filter #(= (mod value %) 0) (range (inc value))))

(defn get-time-sec [date]
  (.substring date (inc (.indexOf date "T")) (.indexOf date ".")))

(defn get-time [date]
  (.substr date (inc (.indexOf date "T")) 5))

(defn mock-rand-path [n]
  (take 61 (repeatedly #(rand-int n))))

(defn mock-line-path []
  (range 61))
