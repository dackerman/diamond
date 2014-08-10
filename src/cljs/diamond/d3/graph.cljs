(ns diamond.d3.graph)

(defrecord D3Graph [svg force-layout])

(defn ^:private update-nodes [svg new-nodes]
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

(defn ^:private update-links [svg new-links]
  "Updates the D3 visualization to match the new link data"
  (let [link (.. svg (selectAll ".link") (data new-links))]
    ;update
    (.. link
      (classed "enter" false))

    ;add
    (.. link
      (enter)
      (append "line")
      (classed "link" true)
      (classed "enter" true))

    ;add+update
    (.. link
      (append "svg:title")
      (text (fn [d] (.-desc d))))))

(defn ^:private tick [{svg :svg}]
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


(defn start [d3graph]
  (.start (:force-layout d3graph)))


(defn update [{svg :svg force-layout :force-layout :as graph} new-nodes new-links]
  "Updates the graph data and resets the force layout"
  (.stop force-layout)
  (update-nodes svg new-nodes)
  (update-links svg new-links)
  (.. force-layout
    (nodes new-nodes)
    (links new-links))
  (tick graph))


(defn create-graph [height width]
  (def svg (.. js/d3
             (select "#container")
             (append "svg")
             (attr "width" width)
             (attr "height" height)))

  (def force-layout (.. js/d3 -layout (force)
                      (size (clj->js [width height]))
                      (charge -100)
                      (gravity 0.1)
                      (linkDistance 50)))

  (def graph (D3Graph. svg force-layout))
  (.on force-layout "tick" (fn [] (tick graph)))
  graph)