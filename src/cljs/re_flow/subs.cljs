(ns re-flow.subs
  "Functions for subscribing to re-flow data."
  (:require [re-flow.db :as db]
            [re-flow.flow :as rff]
            [re-frame.core :as rf]
            [re-frame.interop :as interop]))

(rf/reg-sub ::flow
 (fn [db [_ name]]
   (db/flow db name)))

(defn sub-flow
  "Subscribes to the flow.

  If no flow-name is provided, subscribes to the default flow."
  ([]
   (sub-flow nil))
  ([flow-name]
   (rf/subscribe [::flow flow-name])))


(rf/reg-sub-raw
 ::flow-db
 (fn [_ [_ flow-name]]
   (interop/make-reaction
    (fn []
     (when-let [flow @(rf/subscribe [::flow flow-name])]
       (rff/db flow))))))

(defn sub-flow-db
  "Subscribes to the flow db.

  If no flow-name is provided, the default flow is used."
  ([]
   (sub-flow-db nil))
  ([flow-name]
   (rf/subscribe [::flow-db flow-name])))


(rf/reg-sub-raw
 ::flow-state
 (fn [_ [_ flow-name]]
   (interop/make-reaction
    (fn []
     (when-let [flow @(rf/subscribe [::flow flow-name])]
       (rff/current-state flow))))))

(defn sub-flow-state
  "Subscribes the the flow state.

  If no flow-name is provided, the default flow is used."
  ([]
   (sub-flow-state nil))
  ([flow-name]
   (rf/subscribe [::flow-state flow-name])))
