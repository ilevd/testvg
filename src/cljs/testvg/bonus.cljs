(ns testvg.bonus
  (:require [reagent.core :as reagent :refer [atom]]))

(defn get-timer [timers id]
  (first (filter #(= id (:id %)) @timers)))

(defn update-seconds! [timers id]
  (swap! timers (fn [t]
                  (mapv #(if (= id (:id %))
                           (update % :seconds inc)
                           %) t))))

(defn add-new-data-timer! [timers id]
  (swap! timers conj {:id id
                      :seconds 0
                      :timer-id (js/setInterval #(update-seconds! timers id) 1000)
                      :is-run true}))

(defn start-timer! [timers id]
  (swap! timers (fn [t]
                  (mapv #(if (= id (:id %))
                           (assoc %
                             :timer-id (js/setInterval (fn [] (update-seconds! timers id)) 1000)
                             :is-run true) %) t))))

(defn stop-timer! [timers id]
  (js/clearInterval (:timer-id (get-timer timers id)))
  (swap! timers (fn [t]
                  (mapv #(if (= id (:id %))
                           (assoc % :is-run false) %) t))))

(defn remove-timer! [timers id]
  (let [timer (get-timer timers id)]
    (when (:is-run timer)
      (js/clearInterval (:timer-id timer))))
  (swap! timers (fn [t] (vec (remove #(= (:id %) id) t)))))

(defn bonus-page []
  (let [next-id (atom 0)
        timers (atom [])]
    (fn []
      [:div
       [:span "Click to add new timer: " ]
       [:button {:on-click #(do (add-new-data-timer! timers @next-id)
                              (swap! next-id inc))} "Add timer"]
       (for [timer @timers]
         ^{:key (:id timer)}
         [:div.timer
          [:div "Seconds Elapsed:" (:seconds timer)]
          [:button {:on-click #(if (:is-run timer)
                                 (stop-timer! timers (:id timer))
                                 (start-timer! timers (:id timer)))
                    :style {:color (if (:is-run timer) "red" "green") }}
           (if (:is-run timer) "Stop" "Start")]
          [:button {:on-click #(remove-timer! timers (:id timer))} "Remove"]])])))
