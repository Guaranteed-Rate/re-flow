(ns re-flow.db
  "Functions for interacting with re-frame dbs."
  (:require [clojure.spec.alpha :as s]
            [re-flow.flow :as rff]
            [re-flow.util :as u]))

;; -- Flows

(s/def ::db (s/keys :opt [::flows]))
(s/def ::flows (s/map-of ::name ::rff/flow))
(s/def ::name (s/nilable any?))

(s/fdef path
  :args (s/or :arity_0 (s/cat)
              :arity_1 (s/cat :flow-name ::name))
  :ret (s/and vector? (s/coll-of (s/nilable keyword?))))

(defn path
  "Gets the path within a db to the flow specified by name.

  Uses the default flow if no flow-name is provided."
  ([]
   (path nil))
  ([name]
   [::flows name]))

(s/fdef flow
  :args (s/or :arity_1 (s/cat :db ::db)
              :arity_2 (s/cat :db ::db :name ::name))
  :ret (s/nilable ::rff/flow))

(defn flow
  "Gets a flow from the db.

  Returns nil if no corresponding flow is found."
  ([db]
   (flow db nil))
  ([db name]
   (get-in db [::flows name])))

(s/fdef set-flow
  :args (s/or :arity_2 (s/cat :db ::db :flow ::rff/flow)
              :arity_3 (s/cat :db ::db :name ::name :flow ::rff/flow))
  :fn (fn [{[_arity args] :args ret :ret}] (= (:flow args) (flow ret (:name args))))
  :ret ::db)

(defn set-flow
  "Sets a flow in db."
  ([db flow]
   (set-flow db nil flow))
  ([db name flow]
   (assoc-in db [::flows name] flow)))


;; -- Transition interceptors

(s/def ::transition-interceptors (s/coll-of ::transition-interceptor))
(s/def ::transition-interceptor any?) ;; TODO Figure out how to specify this better

(s/fdef transition-interceptors
  :args (s/cat :db ::db)
  :ret (s/nilable ::transition-interceptors))

(defn transition-interceptors
  "Gets the transition interceptors stored in db."
  [db]
  (::transition-interceptors db))

(s/fdef set-transition-interceptors
  :args (s/cat :db ::db :transition-interceptors ::transition-interceptors)
  :ret ::db)

(defn set-transition-interceptors
  "Sets the transition interceptors in db."
  [db transition-interceptors]
  (assoc db ::transition-interceptors transition-interceptors))


;; -- Error handler

(s/fdef error-handler
  :args (s/cat :db ::db)
  :ret vector?)

(defn error-handler
  "Gets the error handler in db.

  If no error handler is found, an event that performs a noop is returned."
  [db]
  (or (::error-handler db) [:re-flow.events/noop]))

(s/fdef set-error-handler
  :args (s/cat :db ::db :handler (s/or :vector vector? :keyword keyword?))
  :ret ::db)

(defn set-error-handler
  "Stores the handler in db.

  Handler can be a keyword or a vector. If handler is a keyword, it is stored
  in db as a vector containing only the keyword."
  [db handler]
  (assoc db ::error-handler (u/ensure-vector handler)))
