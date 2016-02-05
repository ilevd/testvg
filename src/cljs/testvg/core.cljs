(ns testvg.core
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]
            [testvg.bonus :as bonus]
            [testvg.chart :as chart]))

;; -------------------------
;; Views

(defn home-page []
  [:div [:h2 "Welcome to testvg"]
   [:div [:a {:href "/about"} "About"]]
   [:div [:a {:href "/test1"} "Test 1"]]
   [:div [:a {:href "/test2"} "Test 2"]]
   [:div [:a {:href "/test3"} "Test 3"]]
   [:div [:a {:href "/bonus"} "Bonus"]]
   [:div [:a {:href "/chart"} "Chart"]]])


(defn about-page []
  [:div [:h2 "About testvg"]
   [:div [:a {:href "/"} "go to the home page"]]
   [:p "Hello! The test is very insteresting.
    First three tasks are easy, and last two more complicated.
    The last one I made simple. It can require more time to do smoothing, beautiful auto-resizable grid,
    some interactivity etc, like the chart on Vigiblobe main page - I really like it."]
   [:p "By Igor."]])


(defn current-page []
  [:div [(session/get :current-page)]])


(defn timer-component []
  (let [seconds-elapsed (atom 0)]
    (fn []
      (js/setTimeout #(swap! seconds-elapsed inc) 1000)
      [:div "Seconds Elapsed:" @seconds-elapsed])))

(defn timer-component2 []
  (let [seconds-elapsed (atom 0)]
    (fn []
      (js/setTimeout #(swap! seconds-elapsed inc) 1000)
      (.log js/console "update")
      [:div "Seconds Elapsed:" @seconds-elapsed
       [:div "Same value:" @seconds-elapsed]])))

(defn timer-component3 []
  (let [seconds-elapsed (atom 0)
        timer-id (atom 0)
        is-run (atom true)]
    (fn []
      (when @is-run
        (reset! timer-id (js/setTimeout #(swap! seconds-elapsed inc) 1000)))
      [:div
       [:button {:on-click (fn [] (when @is-run
                                    (js/clearTimeout @timer-id))
                             (swap! is-run #(not %)))}
        (if @is-run "Stop" "Start")]
       [:div "Seconds Elapsed:" @seconds-elapsed]
       [:div "Same value:" @seconds-elapsed]])))



;; -------------------------
;; Routes

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))

(secretary/defroute "/test1" []
  (session/put! :current-page #'timer-component))

(secretary/defroute "/test2" []
  (session/put! :current-page #'timer-component2))

(secretary/defroute "/test3" []
  (session/put! :current-page #'timer-component3))

(secretary/defroute "/bonus" []
  (session/put! :current-page #'bonus/bonus-page))

(secretary/defroute "/chart" []
  (session/put! :current-page #'chart/chart-page))

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!)
  (accountant/dispatch-current!)
  (mount-root))
