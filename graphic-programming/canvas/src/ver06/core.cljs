(ns ver06.core
  (:require [ver06.canvas2d :as c2d]
            [ver06.character :as c]))

(def CANVAS-WIDTH 640)
(def CANVAS-HEIGHT 480)

(defn initialize [canvas viper]
  (do
    (set! (.-width canvas) CANVAS-WIDTH)
    (set! (.-height canvas) CANVAS-HEIGHT)

    (.setComing viper
                (/ CANVAS-WIDTH 2)
                (+ CANVAS-HEIGHT 50)
                (/ CANVAS-WIDTH 2)
                (- CANVAS-HEIGHT 100))
    ))

(defn eventSetting [v]
  (js/window.addEventListener
   "keydown"
   (fn [event]
     ;; 登場シーンでのキー操作は不可
     (if (not (.-isComing v))
       (let [viperX (atom (.-x (.-position v)))
             viperY (atom (.-y (.-position v)))]

         (case (.-key event)
           "ArrowLeft"  (reset! viperX (- @viperX 10))
           "ArrowRight" (reset! viperX (+ @viperX 10))
           "ArrowUp"    (reset! viperY (- @viperY 10))
           "ArrowDown"  (reset! viperY (+ @viperY 10)))

         ;; 更新
         (.set (.-position v) @viperX @viperY)
         )))
   ))

(defn render
  ;;[canvas util image viper]
  [util image viper]
  (do ;;let [ctx (.getContext util)]

    ;; 描画前に画面全体を不透明な明るいグレーで塗りつぶす
    (.drawRect util "#eeeeee"
               0 0
               ;;(.-width canvas) (.-height canvas)
               CANVAS-WIDTH CANVAS-HEIGHT)

    ;; 登場シーンの処理
    (.update viper CANVAS-HEIGHT CANVAS-WIDTH)

    ;; 恒常ループのために描画処理を再帰呼出しする
    (js/window.requestAnimationFrame
     ;;#(render canvas util image viper)
     #(render util image viper)
     )
    ))

(let [canvas (.querySelector js/document.body "#main_canvas")
      util (c2d/Canvas2DUtility. canvas)
      ctx (.getContext util)]

  (js/window.addEventListener
   "load"
   (fn []
     (do
       (.imgLoader
        util
        "./img/viper.png"
        (fn [img]
          (let [viper
                ;; 画像のサイズをここで設定しなくてもよい
                ;;(c/Viper. ctx 0 0
                ;;(.-height img) (.-width img) img)
                (c/Viper. ctx 0 0 img)]

            ;; 初期化処理を行う
            (initialize canvas viper)

            ;; キーイベントを設定する
            (eventSetting viper)

            ;; 描画処理を行う
            ;;(render canvas util img viper)
            (render util img viper)
            )))
       ))))
