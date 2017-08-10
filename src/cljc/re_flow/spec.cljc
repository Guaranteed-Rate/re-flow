(ns re-flow.spec
  "Functions to aid in building specs for use with re-flow."
  (:require [clojure.spec :as s]))

(defn td-spec
  "Creates a spec that conforms a flow db and transition data into a value.

  s is a spec that conforms the transition data."
  [s]
  (s/and (s/keys :req-un [::db ::td]) (s/conformer #(s/conform s (:td %)))))
