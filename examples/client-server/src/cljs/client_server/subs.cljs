(ns client-server.subs
  (:require [re-frame.core :as re-frame]
            [client-server.db :as db]))

(re-frame/reg-sub
 :loading?
 (fn [db _]
   (db/loading? db)))

(re-frame/reg-sub
 :error?
 (fn [db _]
   (db/error? db)))

(re-frame/reg-sub
 :response
 (fn [db _]
   (db/response db)))
