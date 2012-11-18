(ns analytics.server
  (:require [noir.server :as server]))

(server/load-views-ns 'analytics.views)

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "9333"))]
    (server/start port {:mode mode
                        :ns 'analytics})))

