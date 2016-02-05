(ns testvg.chart
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as reagent :refer [atom]]
            [clojure.string :as string]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [testvg.utils :as utils]))

(defonce height 300)
(defonce width 600)
(defonce project-id "vgteam-TV_Shows")

(defn get-info! [project-id info]
  (go (let [response (<! (http/get (str "http://api.vigiglobe.com/api/resources/projects/" project-id)))]
        ; (.log js/console (:status response) (:timezone (:body response)))
        (reset! info (:body response)))))

(defn get-volume! [project-id data times]
  (go (let [response (<! (http/get (str "http://api.vigiglobe.com/api/statistics/v1/volume?granularity=minute&project_id=" project-id)))]
        (when (= (:status response) 200)
          (let [messages (-> response :body :data :messages)
                start-time (-> messages first first utils/get-time)
                end-time (-> messages last first utils/get-time)
                path (mapv #(second %) messages)
                ; path-sec (mapv #(reduce + %) (partition 60 60 [0] path)) ; test for granularity=second
                ]
            (.log js/console "Get volume: " start-time " " end-time " " (count path))
            (reset! data path) ; (reset! data (utils/mock-rand-path 100)) ; (reset! data path-sec)
            (swap! times assoc :start start-time :end end-time))))))

(defn get-max-value [data]
  (let [max-value (apply max data)
        max-ceil-value (if (= max-value 0) 2
                    (* 10 (.ceil js/Math (/ max-value 10))))]
    ; (.log js/console "Max ceil value " max-ceil-value max-value)
    max-ceil-value))

(defn add-random-value! [data]
  (let [value (rand-int 15)]
    (swap! data #(let [new-data (conj % value)]
                   (if (> (count new-data) 61)
                     (vec (drop 1 new-data))
                     new-data)))))

(defn data->path [data step-y]
  (let [step-x 10
        path-list (keep-indexed (fn [index value]
                             [(if (= index 0) "M " "L ")
                              (* step-x index) " "
                              (- height (* step-y value))]) data)
        path (string/join " " (flatten path-list))]
    path))

(defn chart-page []
  (let [data (atom [])
        info (atom {:name "Loading..."})
        times (atom {:start "" :end ""})]
    ; (js/setInterval #(add-random-value! data) 100)
    (js/setInterval #(get-volume! project-id data times) 1000)
    (get-info! project-id info)
    (get-volume! project-id data times)
    (fn []
      (let [max-value (get-max-value @data)
            step-y (/ height max-value)
            line-coords-y (map #(* step-y %) (range (inc max-value)))]
        [:div
         [:h4 (:name @info) " "
          [:small (:timezone @info)]]
         [:h5 "Volume"]
         [:svg {:width (+ 80 width)
                           :height (+ 80 height)
                           :style {:display "block"}}
          [:g {:transform "translate(40 40)" :width width :height height :style {:display "block"}}

           [:line {:x1 0 :y1 height :x2 width :y2 height :style {:stroke "#AAA" :stroke-width 1 }}]
           [:line {:x1 0 :y1 0 :x2 0 :y2 height :style {:stroke "#AAA" :stroke-width 1 }}]

           (when (<= (count line-coords-y) 21)
             (for [y line-coords-y]
               ^{:key y}
               [:line {:x1 0 :y1 y :x2 width :y2 y :style {:stroke "#EEE" :stroke-width 1 }}]))

           [:path {:fill "none" :stroke "#FF0055" :stroke-width 2  :d (data->path @data step-y)}]

           [:g {:transform "translate(10 0)"}
            (if (<= (count line-coords-y) 21)
              (for [y line-coords-y]
                ^{:key y}
                [:text.chart-text {:x -15 :y (+ (- height y) 4)} (/ y step-y)])
              [:g
               [:text.chart-text {:x -15 :y (+ height 4)} "0"]
               [:text.chart-text {:x -15 :y 4} max-value]])]

           [:g
            [:text.chart-text {:x 15 :y (+ height 20)} (:start @times)]
            [:text.chart-text {:x (+ 15 width) :y (+ height 20)} (:end @times)]]
           ]]
         ]))))
