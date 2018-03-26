(ns counter.core
  (:require [clojure.spec.alpha :as s]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [re-flow.core :as re-flow]))

;; In this example, we introduce the mechanisms re-flow uses to transform data:
;; db-spec and transition-spec. Please note that this is just the default
;; behavior. re-flow is an open system, and you can easily add and remove
;; functionality as you deem fit. Check out the custom-transition-interceptor
;; example for more information.
;;
;; A flow can accumulate data as it executes and store the data in an internal
;; db. You will sometimes see this called the flow-db in the docs. A flow may
;; update its flow-db when a transition occurs, and it does so by conforming the
;; transition data and the current flow-db to generate either a map or some
;; other value. We call this spec a db-spec because it is used to update a
;; flow's db.
;;
;; If the conform operation fails then the transition also fails, and an error
;; event is dispatched. If the conformed value is a map, then the map is merged
;; with the existing flow-db. If the conformed value is not a map, then it is
;; paired with a state `:key` if provided, or the state `:name` to create a map
;; and then is merged with the existing flow-db.
;;
;; The spec itself is provided a map with keys `:db` and `:td`, where the value
;; of `:db` is the current flow-db and the value of `:td` is the transition
;; data. If you only need to operate on the `:td` key, then re-flow.spec/td-spec
;; is a helper function that will make building specs easier.
;;
;;
;; In this example, we'll be building a counter that responds to :increment and
;; :decrement transition values and updates the flow-db, so we'll build a full
;; db-spec. While we could build specs to ensure that the correct transition
;; data is provided, we'll keep it simple for this example and just build a
;; conformer.

(s/def ::counter-ds
  (s/conformer
   (fn [{:keys [td db]}]
     (let [f (cond (= td :increment) inc
                   (= td :decrement) dec
                   :else identity)
           c (:current-count db)]
       {:current-count (f c)}))))


;; Similarly, we can provide a transition-spec. The transition spec will accept
;; the current flow-db and the transition data and is expected to conform to a
;; value in the state's transition table (or any value to match a default).
;;
;; The result is used to rewrite the currently executing event, and the value is
;; value is provided to the underlying transition function. The fact that the
;; event is rewritten is important to remember when writing your own transition
;; interceptors.
;;
;;
;; For this example, let's transition a :done value when the counter gets to
;; ten.

(s/def ::counter-ts
  (s/conformer
   (fn [{:keys [db td]}]
     (if (and (= 9 (:current-count db)) (= :increment td))
       :done
       td))))

;; As an aside, I like to use the -ds and -ts suffixes to note that these specs
;; are db-specs and transition-specs, respectively.



;; Now we can use these specs to make a simple counter flow.

(def counter-flow
  (re-flow/flow [{:name :counting
                  :db-spec ::counter-ds
                  :transition-spec ::counter-ts
                  :transition {:done :done
                               :re-flow.transition/default :counting}}
                 {:name :done}]
                {:start :counting}))


;; Sometimes it's useful to display different views based on the which state
;; is the current one. You can use a multimethod to choose which to display.
;; We'll do that here.

(defn counting-view []
  (let [db (re-flow/sub-flow-db)]
    (fn []
     [:div
      [:button {:on-click #(re-flow/transition :decrement)} "-"]
      [:span (:current-count @db)]
      [:button {:on-click #(re-flow/transition :increment)} "+"]])))

(defn done-view []
  [:p "Done!"])

(defmulti panels (fn [state-name] state-name))
(defmethod panels :default [_] [:div])
(defmethod panels :counting [_] [counting-view])
(defmethod panels :done [_] [done-view])


(defn main-panel []
  (let [state (re-flow/sub-flow-state)]
    (fn []
      (panels (or (:name @state) :default)))))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [main-panel]
                  (.getElementById js/document "app")))

;; When we start the flow, we want to set the starting flow-db, so we
;; can provide it here.
(defn ^:export init []
  (re-flow/start counter-flow nil {:current-count 0})
  (mount-root))


;; In practice, I have used db-specs and transition-specs very frequently, so
;; I would encourage you to spend some time with this example to make sure you
;; understand them completely. As a challenge, try adding some states and using
;; re-flow.spec/td-spec to make your specs more terse.
