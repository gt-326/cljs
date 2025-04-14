(ns ver03.filter)

(defn grayScale [ctx imgData]
  (let [w (.-width imgData)
        h (.-height imgData)
        out (.createImageData ctx w h)
        orgnl (js/Uint8ClampedArray.from (.-data imgData))]

    (doseq [i (range h)
            j (range w)
            :let [idx (* 4 (+ (* i w) j))
                  idx2 (inc idx)
                  idx3 (inc idx2)
                  idx4 (inc idx3)

                  r (aget orgnl idx)
                  g (aget orgnl idx2)
                  b (aget orgnl idx3)

                  luminance (/ (+ r g b) 3)]]

      (aset (.-data out) idx luminance)
      (aset (.-data out) idx2 luminance)
      (aset (.-data out) idx3 luminance)
      (aset (.-data out) idx4 (aget orgnl idx4)))

    ;; img-changed
    out
    ))


(defn invert [ctx imgData]
  (let [w (.-width imgData)
        h (.-height imgData)
        out (.createImageData ctx w h)
        orgnl (js/Uint8ClampedArray.from (.-data imgData))]

    (doseq [i (range h)
            j (range w)
            :let [idx (* 4 (+ (* i w) j))
                  idx2 (+ idx 1)
                  idx3 (+ idx 2)
                  idx4 (+ idx 3)]]

      (aset (.-data out) idx (- 255 (aget orgnl idx)))
      (aset (.-data out) idx2 (- 255 (aget orgnl idx2)))
      (aset (.-data out) idx3 (- 255 (aget orgnl idx3)))
      (aset (.-data out) idx4 (aget orgnl idx4)))

    ;; img-changed
    out
    ))
