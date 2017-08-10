(ns client-server.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [re-flow.core]
            [day8.re-frame.http-fx]
            [client-server.db]
            [client-server.events]
            [client-server.interceptors]
            [client-server.subs]
            [client-server.views :as views]))


(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (re-frame/dispatch-sync [:initialize-db])
  (mount-root))
