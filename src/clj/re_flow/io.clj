(ns re-flow.io
  "Functions for building visualizations of flows.

  To use the functions in the namespace, you should require aysylu/loom version
  1.0.0 or higher in your project."
  (:require [loom.graph :refer :all]
            [loom.io :as lio]
            [loom.label :refer [add-label]]))

(defn- add-transition [from g [label to]]
  (let [label (if (= :re-flow.transition/default label) "*" label)]
   (-> (add-nodes g from to)
       (add-edges [from to])
       (add-label [from to] label))))

(defn- add-state [g {:keys [name transition]}]
  (reduce (partial add-transition name) g (seq transition)))

(defn flow-digraph
  "Generates a loom digraph from the flow."
  [flow]
  (reduce add-state (digraph) (-> flow :states vals)))

(defn view
  "Converts the flow to a temporary PNG file using GraphViz and opens it."
  [flow]
  (lio/view (flow-digraph flow)))
