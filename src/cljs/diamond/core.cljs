(ns diamond.core
  (:require [clojure.string]
            [diamond.d3.graph :as g]))

(defn websocket-test []
  (def connection (js/WebSocket. "ws://localhost:3000/ws"))

  (set! (.-onmessage connection) (fn [message]
                                   (.log js/console "<p>" (.-data message) "</p>")))

  (set! (.-onerror connection) (fn [error]
                                 (.log js/console (str "ERROR: " (.stringify js/JSON error)))))

  (.log js/console "Listening for websocket data..."))
;(def numx 10)
;(def numy 10)
;(def numnodes (* numx numy))
;
;(def first-names ["dave", "dan", "sweta", ""])
;(def last-names [])
;
;(def name-strings "1 LIAM	1 CHARLOTTE
;2 NOAH	2 AMELIA
;3 OLIVER	3 OLIVIA
;4 AIDAN/AIDEN/ADEN	4 AVA
;5 ASHER	5 ARIA/ARYA
;6 OWEN	6 VIOLET
;7 BENJAMIN	7 SOPHIA/SOFIA
;8 DECLAN	8 SCARLETT/SCARLET
;9 HENRY	9 AUDREY
;10 JACKSON/JAXON	10 EMMA
;11 GRAYSON/GREYSON	11 NORA/NORAH
;12 ETHAN	12 GRACE
;13 CALEB/KALEB	13 LILY/LILLY
;14 LANDON/LANDEN	14 AURORA
;15 ELIJAH	15 ABIGAIL
;16 LUCAS/LUKAS	16 CHLOE
;17 GABRIEL	17 VIVIENNE/VIVIEN/VIVIAN
;18 FINN/FYNN	18 HARPER
;19 ALEXANDER	19 ALICE
;20 WILLIAM	20 ELLA
;21 ELLIOT/ELIOT/ELLIOTT	21 ELIZABETH/ELISABETH
;22 GAVIN	22 CLAIRE/CLARE
;23 JACK	23 LILA/LILAH
;24 LEVI	24 ISABELLA
;25 SILAS	25 ARIANNA/ARIANA
;26 SEBASTIAN/SEBASTIEN	26 LUCY
;27 EMMETT	27 ISLA
;28 HUDSON	28 ELEANOR
;29 MICAH	29 STELLA
;30 JACOB	30 PENELOPE
;31 THEODORE	31 GENEVIEVE
;32 ISAAC	32 SADIE
;33 JAMES	33 AVERY
;34 EVERETT	34 HANNAH/HANNA
;35 WYATT	35 CORA
;36 JASPER	36 EVELYN
;37 LOGAN	37 HAZEL
;38 LUKE	38 JULIET/JULIETTE
;39 MILES/MYLES	39 CAROLINE
;40 SAMUEL	40 ADALYN")
;
;(def first-names (filter #(< 2 (count %)) (clojure.string/split name-strings #"\s+")))
;(def first-names (flatten (map #(clojure.string/split % #"/") first-names)))
;(def first-names (map clojure.string/capitalize first-names))
;
;(defn build-random-nodes []
;  (clj->js (into []
;             (for [x (range numx)
;                   y (range numy)]
;               {"name" (rand-nth first-names)}))))
;
;
;(defn generate-random-links [i nodes]
;  (let [nameof #(.-name (nth nodes %))]
;    (map
;      (fn [t]
;        {"source" i
;         "target" t
;         "desc" (str (nameof i) " lubs " (nameof t))})
;      (take 1 (shuffle (remove #(= i %) (range numnodes)))))))
;
;(defn build-random-links [nodes]
;  (clj->js (into []
;             (flatten
;               (for [i (filter even? (range numnodes))]
;                 (generate-random-links i nodes))))))


;(def nodes (build-random-nodes))
;(def links (build-random-links nodes))

(def graph (g/create-graph 900 1200))

(.json js/d3 "/api/graph"
  (fn [error json]
    (if-not (nil? error)
      (.log js/console "error: " error)
      (let [js-nodes (.-vertices json)
            js-links (.-edges json)
            n-index (fn [node-id] (first (filter #(= node-id (aget % "_id")) js-nodes)))]
        (.log js/console "success: ")
        (g/update graph
          (clj->js js-nodes)
          (clj->js (map (fn [link]
                          {"source" (n-index (aget link "_outV"))
                           "target" (n-index (aget link "_inV"))
                           "desc" (.-_label link)})
                     js-links)))
        (g/start graph)))))


;(.addEventListener js/window "keydown"
;  (fn [event]
;    (if (= 13 (.-keyCode event))
;      (do
;        (.log js/console "updating graph")
;        (.push links (clj->js (first (generate-random-links (rand-int numnodes) nodes))))
;        (g/update graph nodes links)
;        (g/start graph)))))
