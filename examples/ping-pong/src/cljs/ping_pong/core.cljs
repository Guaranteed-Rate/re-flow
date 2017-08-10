(ns ping-pong.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [re-flow.core :as re-flow])) ;; <-- start here!


;; First, we want to create a flow.
;;
;; For our simple ping-pong app, we just need a flow with two states: ping and
;; pong. To create the flow, we'll call re-flow.core/flow and pass it two
;; arguments.
;;
;; The first argument is a vector of state definitions. In this vector, we'll
;; create a map for each of the states. A state map must contain at least a
;; :name key. In this case, we'll also provide a transition map in each state.
;; A transition map describes to which state the flow will transition when
;; given a particular value.
;;
;; In this case, we don't care what the value is since we will always be
;; toggling between the two states, so we'll use the special key
;; :re-flow.transition/default. If a transition occurs with a value that does
;; not match any key in the transition map, it will use the special key
;; instead.
;;
;; The second argument is a map of options. We need to at least designate one
;; of the states as the starting state. We'll treat :ping as the starting state.
(def ping-pong-flow
  (re-flow/flow [{:name :ping
                  :transition {:re-flow.transition/default :pong}}
                 {:name :pong
                  :transition {:re-flow.transition/default :ping}}]
                {:start :ping}))

;; Next, we'll create a view that uses some data out of our flow.
;;
;; re-flow provides some convenience functions that help set up
;; subscriptions. You do not have to use the functions; you can set up
;; subscriptions manually if you wish. But give the functions a chance first!
;; We have found that providing the functions is convenient for documentation
;; as well as having fspecs to help catch errors.
;;
;; In this case, we want to display the name of the state, so we'll grab
;; the flow's current state by calling re-flow.core/sub-flow-state.
;;
;; It is important to note that re-flow supports running multiple flows
;; concurrently. To do this, each flow must have a name. There is a default
;; flow that is nameless, and that is the one that we are using here. If you
;; were executing a named flow, you would pass the flow-name as the argument
;; to sub-flow-state.
(defn main-panel []
  (let [state (re-flow/sub-flow-state)]
    (fn []
      [:div
       ;; State here is one of the maps defined in ping-pong-flow, so we'll
       ;; just pull out the name to display
       [:p (:name @state)]

       ;; If we click the button, we want the flow to transition from one
       ;; state to the next. We can accomplish that by calling the transition
       ;; function. You will normally provide transition data to go along with
       ;; this call, and you can provide a flow-name as well.
       ;;
       ;; If you are using a named flow, you have to provide some transition
       ;; data, but nil is a perfectly valid value to pass. In fact, that is
       ;; what is happening here.
       [:button {:on-click #(re-flow/transition)} "Transition!"]])))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [main-panel]
                  (.getElementById js/document "app")))

;; When we start up the application, we also want to start the flow. To do that
;; we will simply call re-flow.core/start, which dispatches an event with the
;; flow and optionally a name. Here, we're setting ping-pong-flow to be the
;; default flow.
(defn ^:export init []
  (re-flow/start ping-pong-flow)
  (mount-root))

;; And that's it! I would highly encourage you to try to add states and
;; maybe some more complex transitions. Be sure to check out the other examples
;; for some of the more advanced features re-flow has to offer.
