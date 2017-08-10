(ns client-server.events
  (:require [ajax.core :as ajax]
            [client-server.db :as db]
            [client-server.interceptors :as i]
            [cognitect.transit :as t]
            [re-frame.core :as re-frame]
            [re-flow.events :refer [start-event set-transition-interceptors-event]]
            [re-flow.interceptors :as rfi]))

(def transition-interceptors
  (conj rfi/default-interceptors i/clear-response-interceptor))

(re-frame/reg-event-fx
 :initialize-db
 (fn [_ _]
   {:db (db/set-loading {} true)
    :dispatch-n [(set-transition-interceptors-event transition-interceptors)
                 [:load-flow]]}))

(re-frame/reg-event-fx
 :load-flow
 (fn [_ _]
   {:http-xhrio {:method          :get
                  :uri             "/flow"
                  :response-format (ajax/transit-response-format)
                  :on-success      [:load-flow-success]
                  :on-failure      [:load-flow-failure]}}))

(re-frame/reg-event-fx
 :load-flow-success
 (fn [{db :db} [_ flow]]
   {:db (db/set-loading db false)
    :dispatch (start-event flow)}))

(re-frame/reg-event-db
 :load-flow-failure
 (fn [db _]
   (-> db
       (db/set-loading false)
       (db/set-error true))))

(re-frame/reg-event-db
 :set-response
 (fn [db [_e response]]
   (assoc db :response response)))
