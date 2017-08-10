(ns re-flow.coeffects
  "Functions for injecting and extracting coeffects."
  (:require [re-flow.db :as db]
            [re-flow.flow :as rff]
            [re-frame.core :as rf]))

;; -- flow db

(rf/reg-cofx
  ::flow-db
  (fn [cofx name]
    (assoc cofx ::flow-db (-> (db/flow (:db cofx) name)
                             (rff/db)))))

(defn inject-flow-db
  "Injects the named flow's db into the cofx."
  ([]
   (inject-flow-db nil))
  ([flow-name]
   (rf/inject-cofx ::flow-db flow-name)))

(defn flow-db
  "Gets the flow db from the cofx."
  [cofx]
  (::flow-db cofx))


;; -- Error handler

(rf/reg-cofx ::error-handler
  (fn [cofx name]
    (->> (db/error-handler (:db cofx))
         (assoc cofx ::error-handler))))

(defn ^:no-doc inject-error-handler
  "Infrastructure, for internal use.

  Injects the error handler into the cofx."
  []
  (rf/inject-cofx ::error-handler))

(defn ^:no-doc error-handler
  "Infrastructure, for internal use.

  Returns the error-handler stored in cofx, or nil if not found."
  [cofx]
  (::error-handler cofx))
