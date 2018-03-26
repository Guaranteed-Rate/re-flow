(ns re-flow.flow
  "Functions for defining, querying, and executing flows.

  A flow is a data structure that describes a potential progression of state,
  accretion of auxiliary data, and a marker that describes which state is
  current. In practice, a flow is very similar to a finite state machine.

  The potential progression of state is expressed as a collection of individual
  states, each of which must contain a name and may optionally contain a map
  that describes to which state the flow should progress given some value.

  The following is a simple state definition.

  ```clojure
  {:name       :my-state
   :transition {:value-a                    :my-next-state
                :value-b                    :some-other-state
                :re-flow.transition/default :yet-another-state}
  ```

  The `:re-flow.transition/default` is matched when a transition value is
  provided that does not match any of the other transition values.

  Note that states are not limited to these keys; they may contain any other
  data necessary for processing. Refer to the documentation for [[re-flow.core]]
  for additional information. We highly encourage you to ensure that your state
  definitions comprise only simple values (i.e., those that are serializable to
  JSON or transit). The flow API is designed to be runnable on both server and
  client so that flows may passed between the two.

  In addition to defining the states, you must also provide the name of the
  starting state. This is accomplished by either calling [[start-state]] and
  providing the name of the starting state, or by providing an option map with
  the starting state name associated with the `:start` key.

  Once a flow has been started (e.g., by calling [[start]]), you may query a
  flow for its current state by calling [[current-state]].

  Finally, a flow may maintain a database that accretes data when a transition
  occurs. The database is maintained independently of the state, and it persists
  over the lifetime of the flow. Use [[db]] to get the current value of a flow's
  database. The initial database may be set by using a value associated with the
  `:db` key in the option map passed to [[flow]].

  Following is a short example that demonstrates constructing a somewhat complex
  flow using the flow-descriptor approach. The flow that we wish to construct is
  depicted below. Capital letters represent state names, and lowercase keywords
  represent transition values between states. `*` indicates a default transition.

  ```
        start
          |
          V
          A <------|
         / \\       |
    :b  /   \\ :c   | :a
       /     \\     |
      V       V    |
      B       C ---|
       \\  :d/ ^
     *  \\  /  |
         \\/   | :c
          V   |
          D---|
         /^
        / |
        ---
         *
  ```

  The code to express this flow is as follows. Note that the result of this call
  can be used to start a flow using the re-frame events provided in the client
  part of re-flow.

  ```clojure
  (re-flow.flow/flow
    [{:name :A
      :transition {:b :B
                   :c :C}}
     {:name :B
      :transition {:re-flow.transition/default :D}}

     {:name :C
      :transition {:a :A
                   :d :D}

     {:name :D
      :transition {:c :C
                   :re-flow.transition/default :D}]

    {:start :A})
  ```"
  (:require [clojure.spec.alpha :as s]))

;; -- Flow definition specs

(s/def :re-flow.flow/flow
  (s/keys :req-un [:re-flow.flow/states]
          :opt-un [:re-flow.flow/current-state
                   :re-flow.flow/start
                   :re-flow.flow/db]))

(s/def :re-flow.flow/states
  (s/map-of keyword? :re-flow.flow/state))

;; TODO Determine if the current-state should be an actual state
(s/def :re-flow.flow/current-state keyword?)

(s/def :re-flow.flow/start keyword?)

(s/def :re-flow.flow/db map?)

(s/def :re-flow.flow/state
  (s/keys :req-un [:re-flow.flow.state/name]
          :opt-un [:re-flow.flow.state/transition]))

(s/def :re-flow.flow.state/name keyword?)

(s/def :re-flow.flow.state/transition (s/map-of (constantly true) keyword?))


;; -- Helpers

(def ^:private empty-flow {:states {}})

(declare add-state)

(defn- ensure-state-exists
  "Creates an empty state if state-name does exist in the flow."
  [flow state-name]
  (if (get flow state-name)
    flow
    (add-state flow {:name state-name})))


;; -- API

(defn flow
  "Creates a flow using a collection of states and flow options if specified."
  ([]
   (flow [] {}))
  ([states]
   (flow states {}))
  ([states opts]
   (->> (reduce add-state empty-flow states)
        (merge opts))))


(s/fdef add-state
  :args (s/cat :flow :re-flow.flow/flow
               :state :re-flow.flow/state)
  :ret :re-flow.flow/flow)

(defn add-state
  "Adds a state to the flow.

  If a state with the same name already exists in the flow, it will be
  completely replaced by the new state."
  [flow state]
  (assoc-in flow [:states (:name state)] state))


(s/fdef add-transition
  :args (s/or :arity_3 (s/cat :flow :re-flow.flow/flow
                              :from :re-flow.flow.state/name
                              :to :re-flow.flow.state/name)

              :arity_4 (s/cat :flow :re-flow.flow/flow
                              :from :re-flow.flow.state/name
                              :value :re-flow.flow.state/name
                              :to :re-flow.flow.state/name))
  :ret :re-flow.flow/flow)

(defn add-transition
  "Adds a transition between states whose names are identifed by from and to.

  If no transition value is provided, it is set as the default transition.
  If a transition exists between from and any other node under the same
  transition value, that transition will be replaced."
  ([flow from to]
   (add-transition flow from :re-flow.transition/default to))
  ([flow from value to]
   (-> flow
       (ensure-state-exists from)
       (ensure-state-exists to)
       (assoc-in [:states from :transition value] to))))


(s/fdef start-state
  :args (s/cat :flow :re-flow.flow/flow
               :state-name :re-flow.flow.state/name)
  :ret :re-flow.flow/flow)

(defn start-state
  "Updates the flow to indicate which state to use as the starting state."
  [flow state-name]
  (assoc flow :start state-name))


(s/fdef current-state
  :args (s/cat :flow :re-flow.flow/flow)
  :ret (s/nilable :re-flow.flow/state))

(defn current-state
  "Gets the current state of the flow."
  [flow]
  (get-in flow [:states (:state flow)]))


(s/fdef db
  :args (s/cat :flow :re-flow.flow/flow)
  :ret (s/nilable :re-flow.flow/db))

(defn db
  "Gets the database of the flow."
  [flow]
  (:db flow))


(s/fdef set-db
  :args (s/cat :flow :re-flow.flow/flow :db :re-flow.flow/db)
  :ret :re-flow.flow/flow)

(defn set-db
  [flow db]
  (assoc flow :db db))


;; TODO Write some flow validation functions

(defn- transition-to-state
  [flow next-state-name]
  (when (get-in flow [:states next-state-name])
   (assoc flow :state next-state-name)))


(s/fdef start
  :args (s/or :arity_1 (s/cat :flow :re-flow.flow/flow)

              :arity_2 (s/cat :flow :re-flow.flow/flow
                              :start (s/nilable :re-flow.flow.state/name)))
  :ret (s/nilable :re-flow.flow/flow))

(defn start
  "Transitions the flow into the starting state.

  If provided, initial-db will be merged with the db in the flow definition.
  Returns nil if no starting state is specified within the flow."
  ([flow]
   (start flow (:start flow)))
  ([flow start]
   (when start
     (transition-to-state flow start))))


(s/fdef transition
  :args (s/or :arity_1 (s/cat :flow :re-flow.flow/flow)

              :arity_2 (s/cat :flow :re-flow.flow/flow
                              :transition-key (constantly true)))
  :ret :re-flow.flow/flow)

(defn transition
  "Transitions to the next state in the flow.

  The next state is determined by the specified transition key. If the key
  matches a value in the current state's transition table, then the
  corresponding state is set as the next state. However, if no value matches
  then this function looks for a transition named :re-flow.transition/default.
  Returns nil if no suitable transition is found."
  ([flow]
   (transition flow :re-flow.transition/default))
  ([flow transition-key]
   (let [transition (get-in flow [:states (:state flow) :transition])]
     (if-let [next-state-name (or (get transition transition-key)
                                  (get transition :re-flow.transition/default))]
      (transition-to-state flow next-state-name)))))
