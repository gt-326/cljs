(ns ver04.core
  (:require [ver04.canvas2d :as c2d]))

(def CANVAS-WIDTH 640)
(def CANVAS-HEIGHT 480)

(defn initialize [canvas]
  (do
    (set! (.-width canvas) CANVAS-WIDTH)
    (set! (.-height canvas) CANVAS-HEIGHT)))

(defn eventSetting [isComing viperX viperY]
  (js/window.addEventListener
   "keydown"
   (fn [event]
     (if (not @isComing)
       (case (.-key event)
         "ArrowLeft"  (reset! viperX (- @viperX 10))
         "ArrowRight" (reset! viperX (+ @viperX 10))
         "ArrowUp"    (reset! viperY (- @viperY 10))
         "ArrowDown"  (reset! viperY (+ @viperY 10)))
       ))
   ))

(defn render [canvas util image
              [startTime isComing comingStart viperX viperY]]

  (let [ctx (.getContext util)]

    ;; グローバルなアルファを必ず 1.0 で描画処理を開始する
    (set! (.-globalAlpha ctx) 1.0)

    ;; 描画前に画面全体を不透明な明るいグレーで塗りつぶす
    (.drawRect util "#eeeeee"
               0 0 (.-width canvas) (.-height canvas))

    ;; 登場シーンの処理
    (if @isComing
      (let [;; 現在までの経過時間を取得する
            ;;（ミリ秒を秒に変換するため 1000 で除算）
            ;; nowTime (/ (- (Date.now) @startTime) 1000)

            justTime (Date.now)
            comingTime (/ (- justTime @comingStart) 1000)]

        ;; 登場中は時間が経つほど上に向かって進む
        (reset! viperY (- CANVAS-HEIGHT (* comingTime 50)))

        ;; 一定の位置まで移動したら登場シーンを終了する
        (if (<= @viperY (- CANVAS-HEIGHT 100))
          (do
            ;; 登場シーンフラグを下ろす
            (reset! isComing false)
            ;; 行き過ぎの可能性もあるので位置を再設定
            (reset! viperY (- CANVAS-HEIGHT 100))))

        ;; justTime を 100 で割ったときの
        ;; 余りが 50 より小さくなる場合だけ半透明にする
        (if (< (rem justTime 100) 50)
          (set! (.-globalAlpha ctx) 0.5))
        ))

    ;; 画像を描画する（現在の viper の位置に準じた位置に描画する）
    (.drawImage ctx image @viperX @viperY)

    ;; 恒常ループのために描画処理を再帰呼出しする
    (js/window.requestAnimationFrame
     #(render
       canvas util image
       [startTime isComing comingStart viperX viperY]))
    ))

(let [canvas (.querySelector js/document.body "#main_canvas")
      util (c2d/Canvas2DUtility. canvas)]

  (js/window.addEventListener
   "load"
   (fn []
     (let [startTime (atom (Date.now))
           comingStart (atom (Date.now))

           isComing (atom true)
           viperX (atom (/ CANVAS-WIDTH 2))
           viperY(atom ;;(/ CANVAS-HEIGHT 2)
                  CANVAS-HEIGHT)]

       (.imgLoader
        util
        "./img/viper.png"

        (fn [img]
          (do
            ;; 初期化処理を行う
            (initialize canvas)
            ;; キーイベントを設定する
            (eventSetting isComing viperX viperY)
            ;; 描画処理を行う
            (render
             canvas util img
             [startTime isComing comingStart viperX viperY])
            )))
       ))))
