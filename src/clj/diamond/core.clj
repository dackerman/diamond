(ns diamond.core
  (:use compojure.core
        org.httpkit.server
        org.httpkit.timer)
  (:require [compojure.handler :as handler]
            [compojure.route :as route])
  (:import (java.io ByteArrayOutputStream)
           (com.thinkaurelius.titan.core TitanFactory)
           (com.tinkerpop.blueprints.util.io.graphson GraphSONWriter)))

(defn websocket-test-handler [request]
  (println "request " (pr-str request))
  (println "websocket? " (:websocket? request))
  (with-channel request channel
    (println "channel " (pr-str channel))
    (on-close channel (fn [status] (println "channel closed " status)))
    (loop [id 0]
      (when (< id 10)
        (schedule-task (* id 200)
          (send! channel (str "message from server #" id) false))
        (recur (inc id))))
    (schedule-task 10000 (close channel))))



(defn make-lub-connection []
  (def g (TitanFactory/open "/tmp/titan"))
  (def david (.addVertex g nil))
  (.setProperty david "name" "David")
  (.setProperty david "size" "big")

  (def sweta (.addVertex g nil))
  (.setProperty sweta "name" "Sweta")
  (.setProperty sweta "size" "itsy-bitsy")

  (def married (.addEdge g nil david sweta "married"))
  (.setProperty married "lub" 99345.345)

  (.commit g))

(defn write-graph-to-graphson [request]
  (def g (TitanFactory/open "/tmp/titan"))
  (def baos (ByteArrayOutputStream.))
  (GraphSONWriter/outputGraph g baos)
  (.toString baos))

(defroutes app-routes
  (GET "/api/graph" [] write-graph-to-graphson)
  (GET "/ws" [] websocket-test-handler)
  (route/resources "/")
  (route/not-found "Page not found"))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (def g (TitanFactory/open "/tmp/titan"))
  (run-server (handler/site #'app-routes) {:port 3000}))
