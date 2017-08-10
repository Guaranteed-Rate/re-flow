(ns re-flow.core
  "Convenience functions for building and executing flows."
  (:require [re-flow.coeffects]
            [re-flow.db]
            [re-flow.events :as ev]
            [re-flow.flow :as rff]
            [re-flow.interceptors]
            [re-flow.subs :as rfs]))

(def flow
  "Creates a flow using a collection of states and flow options if specified.

  A convenience wrapper of [[re-flow.flow/flow]]."
  rff/flow)

(def start
  "Dispatches an event to start a flow, with an optional starting db and name.

  If no flow-name is provided, the flow is treated as the default flow.

  A convience wrapper of [[re-flow.events/start]]."
  ev/start)

(def set-transition-interceptors
  "Dispatches an event to set the vector of transition interceptors.

  See [[re-flow.interceptors]] for more information.

  A convenience wrapper of [[re-flow.events/set-transition-interceptors]]."
  ev/set-transition-interceptors)

(def transition
  "Dispatches an event to transition the flow with the provided transition data.

  A convenience wrapper of [[re-flow.events/transition]]."
  ev/transition)

(def sub-flow
  "Subscribes to the flow.

  If no flow-name is provided, subscribes to the default flow.

  A convenience wrapper of [[re-flow.subs/sub-flow]]."
  rfs/sub-flow)

(def sub-flow-db
  "Subscribes to the flow db.

  If no flow-name is provided, the default flow is used.

  A convenience wrapper of [[re-flow.subs/sub-flow-db]]."
  rfs/sub-flow-db)

(def sub-flow-state
  "Subscribes the the flow state.

  If no flow-name is provided, the default flow is used.

  A convenience wrapper of [[re-flow.subs/sub-flow-state]]."
  rfs/sub-flow-state)
