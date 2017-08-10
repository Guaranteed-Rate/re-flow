(ns multi-flow.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [re-flow.core :as re-flow]))

;; This example builds upon the ping-pong example. If you haven't looked at it
;; yet, I would encourage you to do that first.
;;
;; In this example, we are going to execute two flows currently. To do that, we
;; have to give at least one of the flows a name when we start it. There can
;; only be one default (nameless) flow at any given time.
;;
;; We're going to use the same flow as last time, but we're going to run it
;; multiple times.
(def ping-pong-flow
  (re-flow/flow [{:name :ping
                  :transition {:re-flow.transition/default :pong}}
                 {:name :pong
                  :transition {:re-flow.transition/default :ping}}]
                {:start :ping}))

;; First, we'll extract the flow view into its own view and parameterize it
;; on the flow's name. Remember: nil is the "name" of the default flow.
(defn view [flow-name]
  ;; Note that in our subscription, we provide the flow-name. This will return
  ;; a subscription that watches for changes associated with the flow. In this
  ;; case, we're pulling out the current state of the flow.
  (let [state (re-flow/sub-flow-state flow-name)]
    (fn []
      [:div
       [:p (str "state of " (name (or flow-name "default")) ": " (:name @state))]

       ;; In order to transition a non-default flow, we have to provide the flow
       ;; name. We'll just use the flow-name that was provided in the view. It
       ;; is important to note that if you are performing a transition on a
       ;; named flow, you have to provide a transition value, even if that value
       ;; is nil. Since we don't need a transition value, we'll just pass nil
       ;; here.
       [:button {:on-click #(re-flow/transition flow-name nil)} "Transition!"]])))

(defn main-panel []
  [:div
   [view]
   [view :named-flow]])

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [main-panel]
                  (.getElementById js/document "app")))

;; Now we just have to start the two flows. The second one, we will provide a
;; name. For everything to hook up correctly, we have to use the same name we
;; used above, :named-flow.
(defn ^:export init []
  (re-flow/start ping-pong-flow)
  (re-flow/start ping-pong-flow :named-flow)
  (mount-root))

;; If you run this application, when you press the transition buttons, you'll
;; see that only the associated flow states are updated.
