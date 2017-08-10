(ns client-server.db)

(defn loading? [db]
  (:loading db))

(defn set-loading [db val]
  (assoc db :loading val))

(defn error? [db]
  (and (not (loading? db)) (:error db)))

(defn set-error [db val]
  (assoc db :error val))

(defn clear-response [db]
  (dissoc db :response))

(defn response [db]
  (:response db))
