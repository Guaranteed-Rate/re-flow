(ns client-server.interceptors
  (:require [re-frame.core :as re-frame]
            [client-server.db :as db]))

(def clear-response-interceptor
 (re-frame/->interceptor
  :id :clear-response-interceptor
  :after (fn [context]
           (update-in context [:effects :db] db/clear-response))))
