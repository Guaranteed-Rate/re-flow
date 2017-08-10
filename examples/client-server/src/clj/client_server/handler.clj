(ns client-server.handler
  (:require [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [resources]]
            [ring.util.response :refer [resource-response response]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.format-response :refer [wrap-transit-json-response]]
            [client-server.core :refer [app-flow]]))

(defroutes routes
  (GET "/" [] (resource-response "index.html" {:root "public"}))
  (wrap-transit-json-response (GET "/flow" [] (response app-flow)))
  (resources "/"))

(def dev-handler (-> #'routes wrap-reload))

(def handler routes)
