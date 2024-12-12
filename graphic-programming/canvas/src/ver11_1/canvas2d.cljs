(ns ver11_1.canvas2d)

;;Canvas2D API をラップしたユーティリティクラス

(defn Canvas2DUtility [canvas]
  (this-as this
    (set! (.-context2d this) (.getContext canvas "2d"))

    (let [ctx (.getContext canvas "2d")]

      (set! (.. Canvas2DUtility -prototype -getContext)
            (fn []
              (this-as this
                (.-context2d this))))

      (set! (.. Canvas2DUtility -prototype -drawRect)
            (fn [color x y w h]
              (do
                (set! (.-fillStyle ctx) color)
                (.fillRect ctx x y w h)
                )))
      this)))
