(ns re-flow.core-test
  (:require [clojure.spec :as s]
            [clojure.spec.test :as stest]
            [clojure.test :refer-macros [deftest testing is]]
            [day8.re-frame.test :refer-macros [run-test-sync]]
            [re-flow.core :as sut]
            [re-flow.flow :as rff]
            [re-flow.interceptors :as i]
            [re-flow.spec :refer [td-spec]]
            [re-flow.subs :as subs]
            [re-frame.core :as rf]))

(stest/instrument `sut/flow)

(def counter-flow (sut/flow [{:name :counter
                              :db-spec (td-spec (s/conformer (fn [x] {:value (inc x)})))
                              :transition {:re-flow.transition/default :counter}}]
                            {:start :counter
                             :db {:value 0}}))

(rf/reg-event-db ::log-message
 (fn [db [_ msg]]
   (update db ::messages (fnil conj []) msg)))

(rf/reg-sub ::messages
  (fn [db _]
    (or (::messages db) [])))


(deftest test-flow

  (is (= (rff/flow) (sut/flow [])))

  (is (= {:states {:A {:name :A :transition {:b :B}}
                   :B {:name :B}}
          :start :A
          :db {:some :value}}
         (sut/flow [{:name :A
                     :transition {:b :B}}
                    {:name :B}]
                   {:start :A
                    :db {:some :value}}))))


(deftest test-counter-flow
  (run-test-sync
   (let [flow-db (subs/sub-flow-db)
         state (subs/sub-flow-state)]

    (sut/start counter-flow)

     (is (= {:value 0} @flow-db))
     (is (= :counter (:name @state)))

     (sut/transition (:value @flow-db))

     (is (= {:value 1} @flow-db))
     (is (= :counter (:name @state))))))

(deftest test-named-flow
  (run-test-sync
   (let [flow-db (subs/sub-flow-db :named-flow)
         state (subs/sub-flow-state :named-flow)]

     (sut/start counter-flow :named-flow)

     (is (= {:value 0} @flow-db))
     (is (= :counter (:name @state)))

     (sut/transition :named-flow (:value @flow-db))

     (is (= {:value 1} @flow-db))
     (is (= :counter (:name @state))))))

(deftest test-state-change-dispatch
  (run-test-sync
   (let [messages (rf/subscribe [::messages])
         flow (sut/flow [{:name :A
                          :dispatch [::log-message "A"]
                          :transition {:b :B}}
                         {:name :B
                          :dispatch [::log-message "B"]
                          :transition {:a :A}}]
                        {:start :A})]

     (sut/start flow)
     (is (= ["A"] @messages))

     (sut/transition :b)
     (is (= ["A" "B"] @messages)))))

(deftest test-multi-flows
  (run-test-sync
   (let [default-db (sut/sub-flow-db)
         named-db (sut/sub-flow-db :named)]

     (sut/start counter-flow)
     (sut/start counter-flow :named)

     (is (= {:value 0} @default-db))
     (is (= {:value 0} @named-db))

     (sut/transition 0)

     (is (= {:value 1} @default-db))
     (is (= {:value 0} @named-db))

     (sut/transition :named 0)

     (is (= {:value 1} @default-db))
     (is (= {:value 1} @named-db))

     )))

(deftest counter-with-ts
  (run-test-sync
   (let [flow (sut/flow [{:name :counting
                          :db-spec (s/conformer
                                    (fn [{:keys [td db]}]
                                      (let [f (cond (= td :increment) inc
                                                    (= td :decrement) dec
                                                    :else identity)
                                            c (:current-count db)]
                                        {:current-count (f c)})))
                          :transition-spec (s/conformer
                                            (fn [{:keys [db td]}]
                                              (let [d (if (and (= 1 (:current-count db)) (= :increment td))
                                                        :done
                                                        td)]
                                                d)))
                          :transition {:done :done
                                       :re-flow.transition/default :counting}}
                         {:name :done}]
                        {:start :counting})
         flow-db (subs/sub-flow-db)
         state (subs/sub-flow-state)]

     (sut/start flow nil {:current-count 0})

     (is (= {:current-count 0} @flow-db))
     (is (= :counting (:name @state)))

     (sut/transition :increment)

     (is (= {:current-count 1} @flow-db))
     (is (= :counting (:name @state)))

     (sut/transition :increment)

     (is (= {:current-count 2} @flow-db))
     (is (= :done (:name @state))))))
