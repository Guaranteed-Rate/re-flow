(ns re-flow.interceptors
  "Functions to build and use interceptors within re-flow.

  Interceptors are the primary method for extending the behavior of re-flow.
  When a start or transition event are being handled, the event handlers will
  load any transition interceptors from the app-db and inject them into the
  re-frame interceptor queue. There are several interceptors that are used by
  default (see [[default-interceptors]]), but you can provide your own list
  by dispatching a `:re-flow.events/set-transition-interceptors` event or by
  calling [[re-flow.events/set-transition-interceptors]].

  The only guaranteed provided by re-flow is that the name of the flow will
  be in the context under the key `:re-flow.transition/flow-name`. You may also
  call [[flow-name]] to get this value.

  It is important to pay attention to the order of the interceptors since some
  of them (e.g., [[conform-transition-data]]) may change the transition data in
  such a way that data may be lost."
  (:require [clojure.spec.alpha :as s]
            [re-flow.db :as db]
            [re-flow.flow :as rff]
            [re-flow.spec :refer [td-spec]]
            [re-flow.util :as u]
            [re-frame.core :as rf]
            [re-frame.interop :refer [empty-queue]]))

;; -- Helpers

(defn flow-name
  "Gets the name of the flow that is the target of the current event."
  [context]
  (:re-flow.transition/flow-name context))

(defn- enqueue-front
  [context interceptors]
  (update context :queue #(into empty-queue (concat interceptors %))))


;; -- Interceptors

(declare default-interceptors)

(def ^:no-doc transition-interceptors
  "Infrastructure, for internal use only.

  This interceptor is what enables code outside re-flow to extend its behavior.
  Before the transition occurs, it pulls all the transition-interceptors from
  the context and places them at the front of the remaining interceptor queue.

  If no transition-interceptors are found, a default list specified by
  [[default-interceptors]] is used."
  (rf/->interceptor
   :id ::transition-interceptors
   :before (fn [context]
             (let [ints (or (db/transition-interceptors (rf/get-coeffect context :db))
                            default-interceptors)
                   [_ name _] (rf/get-coeffect context :event)]
               (-> context
                   (enqueue-front ints)
                   (assoc :re-flow.transition/flow-name name))))))

(def state-change-dispatch
  "Transition interceptor that dispatches a specified event on state change.

  This interceptor examines the new state after transition. If the new state is
  different from the old state, it pulls the `:dispatch` value from the state if
  one is provided and prepares the event to be dispatched.

  The `:dispatch` value may be a keyword or vector, where the vector is a partial
  event. The dispatch value with the new state as the final parameter.

  It appends the event to `:dispatch-n` in the effect map in order to avoid
  overwriting changes caused by other interceptors."
  (rf/->interceptor
   :id ::state-change-dispatch
   :after (fn [{:keys [coeffects effects] :as context}]
            (let [flow-name (flow-name context)
                  new-state (some-> (db/flow (:db effects) flow-name)
                                    (rff/current-state))
                  old-state (some-> (db/flow (:db coeffects) flow-name)
                                    (rff/current-state))
                  dispatch (:dispatch new-state)]
              (if (and (not= new-state old-state) dispatch)
                (->> (conj (u/ensure-vector dispatch) new-state)
                     (update-in context [:effects :dispatch-n] (fnil conj [])))
                context)))))

(def update-flow-db
  "Transition interceptor that updates the flow db.

  This interceptor updates the flow db by determining by conforming the
  transition value with a state's db-spec, if provided. If the value does not
  conform, an error is dispatched and the transition is abandoned.

  The spec is provided a map that contains a key `:db` with the current flow db
  and a key `:td` with the transition data. The spec should conform this value
  into either a single value or a map. If the return value is a simple value,
  then it will be treated as if in a map associated with a key or either the
  state's `:key` or `:name`.

  The resulting map will be merged into the db after the transition is
  complete."
  (rf/->interceptor
   :id ::update-flow-db
   :before (fn [{:keys [coeffects] :as context}]
             (if-let [flow (db/flow (:db coeffects) (flow-name context))]
               (let [{:keys [db-spec key name] :or {db-spec (td-spec any?)}} (rff/current-state flow)
                     [_ _ data] (:event coeffects)
                     to-conform {:db (rff/db flow) :td data}
                     d (s/conform db-spec to-conform)]
                 (if (not= ::s/invalid d)
                   (let [d (if (map? d) d {(or key name) d})]
                     (assoc context ::flow-db-delta d))

                   (-> (dissoc context :queue)
                       (assoc-in [:effects :dispatch]
                                 (u/error-event [:re-flow.error/db-spec
                                                 (s/explain-str db-spec to-conform)])))))
               context))
   :after (fn [{:keys [coeffects effects] :as context}]
            (or
             (when (:db effects)
              (if-let [d (::flow-db-delta context)]
                (let [flow-name (flow-name context)
                      db (:db effects)
                      flow (db/flow db flow-name)
                      flow (rff/set-db flow (merge (rff/db flow) d))]

                  (->> (db/set-flow db flow-name flow)
                       (assoc-in context [:effects :db])))))

             context))))

(def conform-transition-data
  "Transition interceptor that generates a transition value.

  This interceptor intends to take the transition data and conform it with
  a state's `:transition-spec`, if provided.

  The spec is provided a map that contains a key `:db` with the current flow db
  and a key `:td` with the transition data. The spec should conform this value
  into a value that can be used to transition. The event is updated to provide
  this value.

  [[re-flow.spec/td-spec]] is designed to make building simple transition
  specs.

  Since this interceptor transforms the transition event directly, you should
  ensure that you have stored or otherwise processed the transition data with
  other interceptors before executing this interceptor."
  (rf/->interceptor
   :id ::conform-transition-data
   :before (fn [{:keys [coeffects] :as context}]
             (or (let [{:keys [db event]} coeffects
                       [event-name flow-name data] event
                       flow (db/flow db flow-name)]

                   (when-let [ts (some-> flow
                                         (rff/current-state)
                                         :transition-spec)]

                     (let [to-conform {:db (rff/db flow) :td data}
                           d (s/conform ts to-conform)]
                       (if (not= ::s/invalid d)
                         (assoc-in context [:coeffects :event] [event-name flow-name d])
                         (-> (dissoc context :queue)
                             (assoc-in [:effects :dispatch]
                                       (u/error-event [:re-flow.error/transition-spec
                                                       (s/explain-str ts to-conform)])))))))
                 context))))

(def default-interceptors
  "A default list of interceptors.

  This list includes the following in listed order:

  * [[state-change-dispatch]]
  * [[update-flow-db]]
  * [[conform-transition-data]]"
  [state-change-dispatch
   update-flow-db
   conform-transition-data])
