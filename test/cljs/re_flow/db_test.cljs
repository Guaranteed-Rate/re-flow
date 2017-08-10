(ns re-flow.db-test
  (:require [clojure.spec.test :as stest]
            [clojure.test :refer [deftest is testing]]
            [re-flow.db :as sut]
            [re-flow.flow :as rff]
            [re-flow.test-util :refer [check]]))

(stest/instrument `sut/flow)
(stest/instrument `sut/set-flow)
(stest/instrument `sut/transition-interceptors)
(stest/instrument `sut/set-transition-interceptors)

(deftest test-flow
  ;; (check (stest/check `sut/flow))

  (testing "when no flow is found"
    (is (nil? (sut/flow {})))
    (is (nil? (sut/flow {} :some-name))))

  (testing "when the flow exists in the db"
    (let [default-flow (rff/flow)
          named-flow (rff/flow [{:name :some-state}])
          db {::sut/flows {nil default-flow :named named-flow}}]

      (is (= default-flow (sut/flow db)))
      (is (= named-flow (sut/flow db :named))))))

(deftest test-set-flow
  ;; (check (stest/check `sut/set-flow))

  (let [flow (rff/flow [{:name :A}])]
   (testing "when a flow is set with no name"
     (is (= {::sut/flows {nil flow}} (sut/set-flow {} flow))))

   (testing "when a flow is set with a name"
     (is (= {::sut/flows {nil flow :named flow}}
            (sut/set-flow {::sut/flows {nil flow}} :named flow))))

   (testing "when a flow exists, it is overwritten"
     (is (= {::sut/flows {nil flow}} (sut/set-flow {::sut/flows {nil (rff/flow [{:name :A}])}} flow))))

   (testing "existing values are preserved"
     (is (= {:a :b ::sut/flows {nil flow}} (sut/set-flow {:a :b} flow))))))
