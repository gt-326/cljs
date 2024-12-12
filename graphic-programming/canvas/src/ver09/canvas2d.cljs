(ns ver09.canvas2d)

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

;;      (set! (.. Canvas2DUtility -prototype -imgLoader)
;;            (fn [path callback]
;;              (let [target (js/Image.)]
;;                (.addEventListener
;;                 target
;;                 "load"
;;                 (fn []
;;                   (if callback
;;                     (callback target))))
;;
;;                ;; 画像のロードを開始するためにパスを指定する
;;                (set! (.-src target) path)
;;                )))
      )
    this))
