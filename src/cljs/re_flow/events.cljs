(ns re-flow.events
  "Functions for dispatching re-flow events.

  The primary events are :re-flow.events/start and :re-flow.events/transition.
  For convenience, there are corresponding functions to dispatch the events."
  (:require [clojure.spec :as s]
            [re-frame.core :as rf]
            [re-flow.db :as db]
            [re-flow.coeffects :as rfe]
            [re-flow.flow :as rff]
            [re-flow.interceptors :refer [transition-interceptors]]
            [re-flow.util :as u]))

;; -- noop

(rf/reg-event-db ::noop
 (fn [db _] db))


;; -- Setting transition interceptors

(defn set-transition-interceptors-event
  "Creates an event to set the transition interceptors."
  [interceptors]
  [::set-transition-interceptors interceptors])

(rf/reg-event-db ::set-transition-interceptors
 (fn [db [_ transition-interceptors]]
   (db/set-transition-interceptors db transition-interceptors)))

(defn set-transition-interceptors
  "Dispatches an event to set the vector of transition interceptors.

  See [[re-flow.interceptors]] for more information."
  [interceptors]
  (rf/dispatch (set-transition-interceptors-event interceptors)))


;; -- Error handling

(rf/reg-event-fx ::dispatch-error
  [(rfe/inject-error-handler)]
  (fn [cofx [_ message]]
    {:dispatch (conj (rfe/error-handler cofx) message)}))

(defn dispatch-error
  "Dispatches an error event with the specified message."
  [message]
  (rf/dispatch (u/error-event message)))

(rf/reg-event-db ::set-error-handler
  (fn [db [_ error-handler]]
    (db/set-error-handler db error-handler)))

(defn set-error-handler
  "Sets the event which will be dispatched when an error occurs."
  [error-handler]
  (rf/dispatch [::set-error-handler error-handler]))


;; -- Starting a flow

(defn start-event
  "Creates an event to start the flow with optional name and starting db."
  ([flow]
   (start-event flow nil))
  ([flow name]
   (start-event flow name {}))
  ([flow name db]
   [::start name {:flow flow :flow-db db}]))

(rf/reg-event-db ::start
 [transition-interceptors]
 (fn [db [_ flow-name {:keys [flow flow-db] :or {flow-db {}}}]]
   (let [new-flow (rff/start flow)
         fdb (rff/db flow)
         new-flow (rff/set-db new-flow (merge fdb flow-db))]
     (db/set-flow db flow-name new-flow))))

(defn start
  "Creates and dispatches an event to start a flow.

  Options include a flow name and starting db value. If no flow-name is
  provided, the flow is treated as the default flow."
  ([flow]
   (start flow nil))
  ([flow name]
   (start flow name {}))
  ([flow name db]
   (rf/dispatch (start-event flow name db))))


;; -- Transitioning a flow

(defn transition-event
  "Creates an event to transition a flow with the specified transition data."
  ([]
   (transition-event nil))
  ([td]
   (transition-event nil td))
  ([flow-name td]
   [::transition flow-name td]))

(rf/reg-event-fx ::transition
 [transition-interceptors]
 (fn [{db :db :as cofx} [_ flow-name next-state]]
   (if-let [flow (some-> (db/flow db flow-name)
                         (rff/transition next-state))]
     {:db (db/set-flow db flow-name flow)}
     {:dispatch (u/error-event [:re-flow.error/transition
                                (str "state not found for transition " next-state)])})))

(defn transition
  "Creates and dispatches a transition event."
  ([]
   (transition nil))
  ([td]
   (transition nil td))
  ([flow-name td]
   (rf/dispatch (transition-event flow-name td))))
