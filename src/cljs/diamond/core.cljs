(ns diamond.core
  (:require clojure.string))

(defn websocket-test []
  (def connection (js/WebSocket. "ws://localhost:3000/ws"))

  (set! (.-onmessage connection) (fn [message]
                                   (.log js/console "<p>" (.-data message) "</p>")))

  (set! (.-onerror connection) (fn [error]
                                 (.log js/console (str "ERROR: " (.stringify js/JSON error)))))

  (.log js/console "Listening for websocket data..."))
(def numx 10)
(def numy 10)
(def numnodes (* numx numy))

(def first-names ["dave", "dan", "sweta", ""])
(def last-names [])

(def name-strings "1 LIAM	1 CHARLOTTE
2 NOAH	2 AMELIA
3 OLIVER	3 OLIVIA
4 AIDAN/AIDEN/ADEN	4 AVA
5 ASHER	5 ARIA/ARYA
6 OWEN	6 VIOLET
7 BENJAMIN	7 SOPHIA/SOFIA
8 DECLAN	8 SCARLETT/SCARLET
9 HENRY	9 AUDREY
10 JACKSON/JAXON	10 EMMA
11 GRAYSON/GREYSON	11 NORA/NORAH
12 ETHAN	12 GRACE
13 CALEB/KALEB	13 LILY/LILLY
14 LANDON/LANDEN	14 AURORA
15 ELIJAH	15 ABIGAIL
16 LUCAS/LUKAS	16 CHLOE
17 GABRIEL	17 VIVIENNE/VIVIEN/VIVIAN
18 FINN/FYNN	18 HARPER
19 ALEXANDER	19 ALICE
20 WILLIAM	20 ELLA
21 ELLIOT/ELIOT/ELLIOTT	21 ELIZABETH/ELISABETH
22 GAVIN	22 CLAIRE/CLARE
23 JACK	23 LILA/LILAH
24 LEVI	24 ISABELLA
25 SILAS	25 ARIANNA/ARIANA
26 SEBASTIAN/SEBASTIEN	26 LUCY
27 EMMETT	27 ISLA
28 HUDSON	28 ELEANOR
29 MICAH	29 STELLA
30 JACOB	30 PENELOPE
31 THEODORE	31 GENEVIEVE
32 ISAAC	32 SADIE
33 JAMES	33 AVERY
34 EVERETT	34 HANNAH/HANNA
35 WYATT	35 CORA
36 JASPER	36 EVELYN
37 LOGAN	37 HAZEL
38 LUKE	38 JULIET/JULIETTE
39 MILES/MYLES	39 CAROLINE
40 SAMUEL	40 ADALYN")

(def first-names (filter #(< 2 (count %)) (clojure.string/split name-strings #"\s+")))
(def first-names (flatten (map #(clojure.string/split % #"/") first-names)))
(def first-names (map clojure.string/capitalize first-names))

(def nodes (clj->js (into []
                      (for [x (range numx)
                            y (range numy)]
                        {"x" (+ 10 (* 10 x))
                         "y" (+ 10 (* 10 y))
                         "name" (rand-nth first-names)}))))

(def links (clj->js (into []
                      (flatten
                        (for [i (filter even? (range numnodes))]
                          (let [nameof #(.-name (nth nodes %))]
                            (map
                              (fn [t]
                                {"source" i
                                 "target" t
                                 "desc" (str (nameof i) " lubs " (nameof t))})
                              (take 1 (shuffle (remove #(= i %) (range numnodes)))))))))))

(defrecord D3Graph [svg force-layout])

(defn start [d3graph]
  (.start (:force-layout d3graph)))

(defn update-nodes [svg new-nodes]
  "Updates the D3 visualization to match the new node data"
  (let [node (.. svg (selectAll ".node") (data new-nodes))]
    ;add
    (.. node
      (enter)
      (append "circle")
      (attr "class" "node")
      (attr "r" 8))

    ;add+update
    (.. node
      (append "svg:title")
      (text (fn [d] (.-name d))))))

(defn update-links [svg new-links]
  "Updates the D3 visualization to match the new link data"
  (let [link (.. svg (selectAll ".link") (data new-links))]
    ;add
    (.. link
      (enter)
      (append "line")
      (attr "class" "link"))

    ;add+update
    (.. link
      (append "svg:title")
      (text (fn [d] (.-desc d))))))

(defn tick [{svg :svg}]
  (let [node (.selectAll svg ".node")
        link (.selectAll svg ".link")]
    (.. link
      (attr "x1" (fn [d]
                   (.. d -source -x)))
      (attr "y1" (fn [d] (.. d -source -y)))
      (attr "x2" (fn [d] (.. d -target -x)))
      (attr "y2" (fn [d] (.. d -target -y))))
    (.. node
      (attr "cx" (fn [d] (.-x d)))
      (attr "cy" (fn [d] (.-y d))))))


(defn update [{svg :svg force-layout :force-layout} new-nodes new-links]
  "Updates the graph data and resets the force layout"
  (update-nodes svg new-nodes)
  (update-links svg new-links)
  (.. force-layout
    (nodes new-nodes)
    (links new-links)))


(defn create-graph []
  (def svg (.. js/d3
             (select "#container")
             (append "svg")
             (attr "width" 960)
             (attr "height" 500)))

  (def size (clj->js [700 400]))

  (def force-layout (.. js/d3 -layout (force)
           (size size)
           (charge -100)
           (gravity 0.2)
           (linkDistance 50)))

  (def graph (D3Graph. svg force-layout))
  (.on force-layout "tick" (fn [] (tick graph)))
  graph)

(def graph (create-graph))

(update graph nodes links)
(start graph)