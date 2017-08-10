(ns re-flow.interceptors-test
  (:require [clojure.spec :as s]
            [clojure.test :refer [deftest is testing]]
            [re-flow.db :as db]
            [re-flow.flow :as rff]
            [re-flow.spec :refer [td-spec]]
            [re-flow.interceptors :as sut]))

(def f (rff/flow [{:name :no-db-spec
                   :transition {:re-flow.transition/default :with-simple-db-spec}}
                  {:name :with-simple-db-spec
                   :db-spec (td-spec keyword?)
                   :transition {:re-flow.transition/default :with-complex-db-spec}}
                  {:name :with-no-transition-spec
                   :transition {:re-flow.transition/default :with-transition-spec}}
                  {:name :with-transition-spec
                   :transition-spec ::over-under-ts
                   :transition {:re-flow.transition/default :with-no-transition-spec}}]
                 {:start :no-db-spec}))

(s/def ::over-under-ts (s/and (fn [{td :td}] (integer? td))
                              (s/conformer (fn [{:keys [db td]}]
                                             (if (> td 100) :over :under)))))

(defn flow-db-delta [context]
  (::sut/flow-db-delta context))

(deftest test-update-flow-db-before-fn

  (let [bfn (:before sut/update-flow-db)]

    (testing "when a flow is started"
      ;; TODO What should happen if a flow is already started? We're treating
      ;;      start as just a special case of transition, but that may not be the
      ;;      best model.
      (let [context {:coeffects {:event [:re-flow.events/start nil f]
                                 :db {}}}
            before-ctx (bfn context)]

        (is (nil? (flow-db-delta before-ctx)))))

    (testing "when no db-spec is provided a key or name is used"
      (let [context {:coeffects {:event [:re-flow.events/transition nil :td]
                                 :db (db/set-flow {} (rff/start f))}}
            before-ctx (bfn context)]

        (is (= {:no-db-spec :td} (flow-db-delta before-ctx)))))

    (testing "when the transition value does not conform to db-spec"
      (let [context {:coeffects {:event [:re-flow.events/transition nil 123]
                                 :db (db/set-flow {} (assoc f :state :with-simple-db-spec))}
                     :queue [:some :values]}
            before-ctx (bfn context)]

        (is (empty? (:queue before-ctx)))
        (is (= :re-flow.events/dispatch-error (-> before-ctx :effects :dispatch first)))))

    (testing "when the db-spec is satisfied"
      (let [context {:coeffects {:event [:re-flow.events/transition nil :some-value]
                                 :db (db/set-flow {} (assoc f :state :with-simple-db-spec))}}
            before-ctx (bfn context)]

        (is (= {:with-simple-db-spec :some-value} (::sut/flow-db-delta before-ctx)))))))


(deftest test-update-flow-db-after-fn
  (let [afn (:after sut/update-flow-db)]

    (testing "when there is a delta value"
      (let [context {::sut/flow-db-delta {:a :b}
                     :effects {:db (db/set-flow {} (rff/start f))}}
            after-ctx (afn context)
            new-db (get-in after-ctx [:effects :db])]

        (= {:a :b} (-> (db/flow new-db)
                       (rff/db)))))

   (testing "when there is not a delta value"
     (let [context {:effects {:db (db/set-flow {} (rff/start f))}}
           after-ctx (afn context)
           new-db (get-in after-ctx [:effects :db])]

       (= {} (-> (db/flow new-db)
                      (rff/db)))))))


(deftest test-conform-transition-data-interceptor
  (let [bfn (:before sut/conform-transition-data)]

    (testing "when a flow is started"
     (let [event [:re-flow.events/start nil f]
           context {:coeffects {:event event :db {}}}
           before-ctx (bfn context)]

       (is (= event (get-in before-ctx [:coeffects :event])) "event should not change")))

    (testing "when there is no transition-spec"
      (let [event [:re-flow.events/transition nil :td]
            context {:coeffects {:event event :db (db/set-flow {} (assoc f :state :with-no-transition-spec))}}
            before-ctx (bfn context)]

        (is (= event (get-in before-ctx [:coeffects :event])) "event should not change")))

    (testing "when the transition-spec is not satisfied"
      (let [event [:re-flow.events/transition nil :invalid-value]
            context {:coeffects {:event event :db (db/set-flow {} (assoc f :state :with-transition-spec))}
                     :queue [:more :interceptors]}
            before-ctx (bfn context)]

        (is (empty? (:queue before-ctx)) "queue should be empty")
        (is (= :re-flow.events/dispatch-error (-> before-ctx :effects :dispatch first)) "should report an error")))

    (testing "when the transition-spec is satisfied"
      (let [event [:re-flow.events/transition nil 9001]
            context {:coeffects {:event event :db (db/set-flow {} (assoc f :state :with-transition-spec))}
                     :queue [:more :interceptors]}
            before-ctx (bfn context)]

        (is (= [:more :interceptors] (:queue before-ctx)) "queue should be intact")
        (is (= [:re-flow.events/transition nil :over] (get-in before-ctx [:coeffects :event])) "event should be rewritten")))))
