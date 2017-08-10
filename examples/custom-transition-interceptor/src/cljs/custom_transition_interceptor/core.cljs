(ns custom-transition-interceptor.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [re-flow.core :as re-flow]
            [re-flow.db :as db]        ;; for working with re-frame dbs
            [re-flow.flow :as flow]))  ;; for working with flows

;; Transition interceptors are the mechanism by which you can change or extend
;; the behavior of re-flow. Transition interceptors are simply re-frame
;; interceptors that are executed when a flow is started (which is a special
;; kind of transition) and when a transition event is handled.
;;
;; The base behavior of re-flow is very simple: when a transition event is
;; handled, update the appropriate flow to a new state according to the
;; transition data provided. All other behavior, such as handling db-specs
;; and transition-specs, is added via transition interceptors.
;;
;; There are several namespaces in re-flow that provide functions for building
;; interceptors and using some interesting default ones (re-flow.interceptors),
;; manipulating re-frame dbs (re-flow.db), and manipulating flows
;; (re-flow.flow). We will use the latter two to build a transition interceptor
;; that will update a flow-db with each ping-pong cycle.
;;
(def update-cycle-count
  (re-frame/->interceptor
   :id ::update-cycle-count
   :before
   (fn [context]
     (let [{:keys [coeffects :re-flow.transition/flow-name]} context
           {:keys [event db]} coeffects]
       (if (= :pong (last event))
         (let [flow (db/flow db flow-name)  ;; get the flow from the db
               fdb (flow/db flow)           ;; get the flow-db from the flow
               new-flow (flow/set-db flow (update fdb :cycle-count inc))
               new-db (db/set-flow db flow-name new-flow)]
           (assoc-in context [:coeffects :db] new-db))
         context)))))

;; It can get a little confusing keeping the behavior of some functions like
;; db/flow and flow/db straight, but the way to think about it is that the
;; namespace describes the data on which you are operating and the function
;; describes what you are pulling from the data. So db/flow takes a db and
;; returns a flow, and flow/db takes a flow and returns its internal db.

;; When you have selected the transition interceptors you wish to use for your
;; application, you set them by calling re-flow.core/set-transition-interceptors.
;; This will be done in the init function at the end of this example.



;; This is the same flow as we saw in the ping-pong example. Adding behavior on
;; transition is basically transparent to the flow so long as the interceptors
;; on which the flow depends are satisfied.
(def ping-pong-flow
  (re-flow/flow [{:name :ping
                  :transition {:re-flow.transition/default :pong}}
                 {:name :pong
                  :transition {:re-flow.transition/default :ping}}]
                {:start :ping}))

;; In addition to subscribing to the flow's state as we have done in previous
;; examples, we can also subscribe to a flow's db by calling sub-flow-db and
;; optionally providing a flow-name.
(defn view [flow-name]
  (let [state (re-flow/sub-flow-state flow-name)
        fdb (re-flow/sub-flow-db flow-name)]
    (fn []
      [:div
       [:p "Cycle count: " (:cycle-count @fdb)]
       [:p (str "state of " (name (or flow-name "default")) ": " (:name @state))]
       [:button {:on-click #(re-flow/transition flow-name (:name @state))} "Transition!"]])))

(defn main-panel []
  [:div
   [view]
   [view :named-flow]])

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  ;; Here we set the transition interceptors to include only our custom
  ;; interceptor. There are several default interceptors in the
  ;; re-flow.interceptors namespace, including ones to add behaviors for
  ;; db-specs and transition-specs.
  (re-flow/set-transition-interceptors [update-cycle-count])

  ;; To avoid having to worry about dealing with nil in our interceptor, we will
  ;; provide a starting flow-db to each flow with :cycle-count set to 0.
  (re-flow/start ping-pong-flow nil {:cycle-count 0})
  (re-flow/start ping-pong-flow :named-flow {:cycle-count 0})

  (mount-root))
