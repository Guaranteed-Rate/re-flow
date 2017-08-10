(ns client-server.core
  (:require [re-flow.flow :refer [flow]]))

(def app-flow
  (flow [{:name :question-1
          :question "What is your name?"
          :transition {:re-flow.transition/default :question-2}}
         {:name :question-2
          :question "What is your quest?"
          :transition {:re-flow.transition/default :question-3}}
         {:name :question-3
          :question "What is the airspeed velocity of an unladen swallow?"
          :transition {:re-flow.transition/default :report}}
         {:name :report}]
        {:start :question-1}))
