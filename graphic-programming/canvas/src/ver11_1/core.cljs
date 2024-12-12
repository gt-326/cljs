(ns ver11_1.core)

(def CANVAS-WIDTH 640)
(def CANVAS-HEIGHT 480)

(defn initialize [canvas]
  (do
    (set! (.-width canvas) CANVAS-WIDTH)
    (set! (.-height canvas) CANVAS-HEIGHT)
))

(js/window.addEventListener
 "load"
 (fn []
   (let [canvas (.querySelector js/document.body "#main_canvas")
         ctx (.getContext canvas "2d")
         rad (/ (* 45 Math.PI) 180)]

     ;; 初期化処理
     (initialize canvas)

     ;;(.rotate ctx rad)

     (set! (.-fillStyle ctx) "white")
     (.fillRect ctx 250 50 250 50)

     (set! (.-fillStyle ctx) "black")
     (.fillRect ctx 250 150 200 50)
     )))
