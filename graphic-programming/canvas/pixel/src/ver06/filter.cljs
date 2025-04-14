(ns ver06.filter)

(defn getLumi [data idx]
  (let [r (aget data idx)
        g (aget data (+ idx 1))
        b (aget data (+ idx 2))]
    (int (/ (+ r g b) 3))))


(defn median [ctx imgData]
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

                  ;;=======================

                  idx_top (* 4
                             (+ j
                                (* w
                                   (Math.max (dec i) 0))))

                  idx_bottom (* 4
                                (+ j
                                   (* w
                                      (Math.min (inc i) (dec h)))))

                  idx_left (* 4
                              (+ (* i w)
                                 (Math.max (dec j) 0)))

                  idx_right (* 4
                               (+ (* i w)
                                  (Math.min (inc j) (dec w))))

                  ;;=======================

                  idx_top_left (* 4
                                  (+ (* w
                                        (Math.max (dec i) 0))
                                     (Math.max (dec j) 0)))

                  idx_bottom_left (* 4
                                     (+ (* w
                                           (Math.min (inc i) (dec h)))
                                        (Math.max (dec j) 0)))

                  idx_top_left (* 4
                                  (+ (* w
                                        (Math.max (dec i) 0))
                                     (Math.min (inc j) (dec w))))

                  idx_bottom_right (* 4
                                      (+ (* w
                                            (Math.min (inc i) (dec h)))
                                         (Math.min (inc j) (dec w))))

                  ;;=======================

                  lArray [{:i idx :l (getLumi orgnl idx)}

                          {:i idx_top :l (getLumi orgnl idx_top)}
                          {:i idx_bottom :l (getLumi orgnl idx_bottom)}
                          {:i idx_left :l (getLumi orgnl idx_left)}
                          {:i idx_right :l (getLumi orgnl idx_right)}

                          {:i idx_top_left :l (getLumi orgnl idx_top_left)}
                          {:i idx_bottom_left :l (getLumi orgnl idx_bottom_left)}
                          {:i idx_top_left :l (getLumi orgnl idx_top_left)}
                          {:i idx_bottom_right :l (getLumi orgnl idx_bottom_right)}]

                  med (int (/ (count lArray) 2))

                  luminance_med (get
                                 (vec
                                  (sort-by
                                   ;; 要素:l の昇順にソートする
                                   ;;(fn [a b] (- (:l a) (:l b)))
                                   :l <
                                   lArray)) med)

                  idx_med (:i luminance_med)]]

      ;;(.log js/console (str "luminance: " luminance))

      (aset (.-data out) idx (aget orgnl idx_med))
      (aset (.-data out) idx2 (aget orgnl (+ idx_med 1)))
      (aset (.-data out) idx3 (aget orgnl (+ idx_med 2)))
      (aset (.-data out) idx4 (aget orgnl (+ idx_med 3))))

    ;; img-changed
    out
    ))


(defn laplacian [ctx imgData]
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

                  idx_top (* 4
                             (+ j
                                (* w
                                   (Math.max (dec i) 0))))

                  idx_bottom (* 4
                                (+ j
                                   (* w
                                      (Math.min (inc i) (dec h)))))

                  idx_left (* 4
                              (+ (* i w)
                                 (Math.max (dec j) 0)))

                  idx_right (* 4
                               (+ (* i w)
                                  (Math.min (inc j) (dec w))))

                  r (+ (aget orgnl idx_top)
                       (aget orgnl idx_bottom)
                       (aget orgnl idx_left)
                       (aget orgnl idx_right)

                       (* -4 (aget orgnl idx)))

                  g (+ (aget orgnl (+ idx_top 1))
                       (aget orgnl (+ idx_bottom 1))
                       (aget orgnl (+ idx_left 1))
                       (aget orgnl (+ idx_right 1))

                       (* -4 (aget orgnl idx2)))

                  b (+ (aget orgnl (+ idx_top 2))
                       (aget orgnl (+ idx_bottom 2))
                       (aget orgnl (+ idx_left 2))
                       (aget orgnl (+ idx_right 2))

                       (* -4 (aget orgnl idx3)))

                  value (/ (+ (Math.abs r) (Math.abs g) (Math.abs b)) 3)
                  ]]

      (aset (.-data out) idx value)
      (aset (.-data out) idx2 value)
      (aset (.-data out) idx3 value)
      (aset (.-data out) idx4 (aget orgnl idx4)))

    ;; img-changed
    out
    ))


(defn binarization [ctx imgData]
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

                  luminance (/ (+ r g b) 3)

                  value (if (>= luminance 128) 255 0)]]

      (aset (.-data out) idx value)
      (aset (.-data out) idx2 value)
      (aset (.-data out) idx3 value)
      (aset (.-data out) idx4 (aget orgnl idx4)))

    ;; img-changed
    out
    ))


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
