(ns client-server.views
  (:require [re-frame.core :as re-frame]
            [re-flow.core :as re-flow]
            [reagent.core :as reagent]))

(defn question-panel [_state]
  (let [response (re-frame/subscribe [:response])]
    (fn [{:keys [question] :as state}]
      [:div
       [:h4 question]
       [:input {:type :text
                :value @response
                :on-change #(re-frame/dispatch [:set-response (.-target.value %)])}]
       [:button {:on-click #(re-flow/transition @response)} "That's easy!"]])))

(defn report-panel []
  (let [db (re-flow/sub-flow-db)]
    (fn [_]
      (let [{:keys [question-1 question-2 question-3]} @db]
        [:div
         [:h4 "Report"]
         [:p "Your answer to question 1 was: " question-1]
         [:p "Your answer to question 2 was: " question-2]
         [:p "Your answer to question 3 was: " question-3]]))))

(defn main-panel []
  (let [loading? (re-frame/subscribe [:loading?])
        error? (re-frame/subscribe [:error?])
        state (re-flow/sub-flow-state)]
    (fn []
      (cond
        @loading?                  [:h1 "Loading!"]
        @error?                    [:h1 "Error!"]
        (= :report (:name @state)) [report-panel]
        :else                      [question-panel @state]))))
