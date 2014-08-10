(ns diamond.core
  (:use compojure.core
        org.httpkit.server
        org.httpkit.timer)
  (:require [compojure.handler :as handler]
            [compojure.route :as route])
  (:import com.thinkaurelius.titan.core.TitanFactory))

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

(defroutes app-routes
  (GET "/api/test" [] (pr-str '(one two three ["herp" "derp" "duka"])))
  (GET "/ws" [] websocket-test-handler)
  (route/resources "/")
  (route/not-found "Page not found"))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (def g (TitanFactory/open "/tmp/titan"))
  (def juno (.addVertex g nil))
  (.setProperty juno "name" "juno")
  (def jupiter (.addVertex g nil))
  (.setProperty jupiter "name" "jupiter")
  (def married (.addEdge g nil juno jupiter "married"))
  (println (.getProperty jupiter "name"))
  (println "Hello, World!")
  (run-server (handler/site #'app-routes) {:port 3000}))
