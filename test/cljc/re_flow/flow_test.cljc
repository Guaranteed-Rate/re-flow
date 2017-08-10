(ns re-flow.flow-test
  (:require [clojure.spec :as s]
            [clojure.spec.test :as stest]
            [clojure.test :refer [deftest is testing]]
            [re-flow.flow :as sut]
            [re-flow.test-util :refer [check]]))

;;
;; For the purpose of testing, we'll define a sample flow that covers all the
;; cases for which we test. A depiction of the flow is provided below. Note that
;; * indicates a default transition.
;;
;;        A <------|
;;       / \       |
;;   :b /   \ :c   | :a
;;     /     \     |
;;    V       V    |
;;    B       C ---|
;;     \     /^
;;    * \   / |
;;       \ /  | :c
;;        V   |
;;        D---|
;;       /^
;;      / |
;;      ---
;;       *
;;
;; This flow definition will guide how we generate data for tests. We will limit
;; the data generated for tests be limited to that depicted in the figure above.
;;

(def A {:name :A :transition {:b :B :c :C}})
(def B {:name :B :transition {:re-flow.transition/default :D}})
(def C {:name :C :transition {:a :A :d :D}})
(def D {:name :D :transition {:c :C :re-flow.transition/default :D}})

(s/def ::state (s/with-gen :re-flow.flow/state #(s/gen #{A B C D})))

(stest/instrument `sut/add-state {:spec {:re-flow.flow/state ::state}})
(stest/instrument `sut/add-transition)
(stest/instrument `sut/start-state)
(stest/instrument `sut/start)
(stest/instrument `sut/transition)
(stest/instrument `sut/current-state)

(deftest test-add-state
;  (check (stest/check `sut/add-state))

  (testing "when the flow is empty it is initialized"
    (is (= {:states {:A A}} (-> (sut/flow)
                                (sut/add-state A)))))

  (testing "when the flow is not empty values are added"
    (is (= {:states {:A A :B B}} (-> (sut/flow)
                                     (sut/add-state A)
                                     (sut/add-state B)))))

  (testing "when a key exists its value is overwritten"
    (let [new-A {:name :A :value :some-value}]
      (is (= {:states {:A new-A}} (-> (sut/flow)
                                      (sut/add-state A)
                                      (sut/add-state new-A)))))))

(deftest add-transition-test
  (let [expected {:states {:A {:name :A :transition {:b :B}} :B {:name :B}}}]
   (is (= expected (-> (sut/flow)
                       (sut/add-state {:name :A})
                       (sut/add-state {:name :B})
                       (sut/add-transition :A :b :B))))

   (testing "when states are missing"
     (is (= expected (-> (sut/flow)
                         (sut/add-transition :A :b :B)))))

   (testing "when no transition value is provided, the transition is assumed as default"
     (is (= {:states {:A {:name :A :transition {:re-flow.transition/default :B}} :B {:name :B}}}
            (-> (sut/flow)
                (sut/add-transition :A :B)))))

   (testing "when a transition of the same value already exists on node from, it is overwritten"
     (is (= (sut/add-state expected {:name :C})
            (-> (sut/flow)
                (sut/add-transition :A :b :C)
                (sut/add-transition :A :b :B)))))))

(deftest start-test
  (let [flow {:states {:A A :B B :C C :D D} :start :A :db {:some :state :another :value}}]

    (is (= :A (:state (sut/start flow))))

    (testing "when there is no start state specified"
      (is (nil? (sut/start (dissoc flow :start)))))

    (testing "when the specified start state is missing"
      (is (nil? (sut/start (assoc flow :start :E)))))

    (testing "when a starting state is specified"
      (is (= :B (:state (sut/start flow :B)))))))

(deftest transition-test
  (let [raw {:states {:A A :B B :C C :D D} :start :A :db {:some :state :another :value}}
        flow (sut/start raw)]

    (is (= :B (:state (sut/transition flow :b))))

    (testing "when the transition is missing"
      (is (nil? (sut/transition flow :e))))

    (testing "when no transition value is specified, the default transition is used"
      (is (= :D (:state (-> flow
                            (sut/transition :b)
                            (sut/transition))))))

    (testing "when the flow is not started"
      (is (= (sut/transition raw))))))

(deftest value-test
  (= A (-> (sut/flow)
           (sut/add-state A)
           (sut/start-state :A)
           (sut/start)
           (sut/current-state))))
