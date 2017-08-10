(ns re-flow.util
  "Utility functions for building re-flow.")

(defn ensure-vector
  "If x is a vector, return it, else wrap in a vector."
  [x]
  (if (vector? x) x [x]))

(defn error-event
  "Creates an error event with the specified message."
  [message]
  [:re-flow.events/dispatch-error message])
