(ns ver05.core
  (:require [ver05.canvas2d :as c2d]
            [ver05.character :as c]))

(def CANVAS-WIDTH 640)
(def CANVAS-HEIGHT 480)

(defn initialize [canvas viper]
  (do
    (set! (.-width canvas) CANVAS-WIDTH)
    (set! (.-height canvas) CANVAS-HEIGHT)

    (.setComing viper
                (/ CANVAS-WIDTH 2)
                CANVAS-HEIGHT
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

(defn render [canvas util image viper]
  (let [ctx (.getContext util)]

    ;; グローバルなアルファを必ず 1.0 で描画処理を開始する
    (set! (.-globalAlpha ctx) 1.0)

    ;; 描画前に画面全体を不透明な明るいグレーで塗りつぶす
    (.drawRect util "#eeeeee"
               0 0 (.-width canvas) (.-height canvas))

    ;; 登場シーンの処理
    (if (.-isComing viper)
      (let [;; 現在までの経過時間を取得する
            ;;（ミリ秒を秒に変換するため 1000 で除算）
            ;; nowTime (/ (- (Date.now) @startTime) 1000)

            justTime (Date.now)
            comingTime (/ (- justTime (.-comingStart viper)) 1000)]

        ;; 登場中は時間が経つほど上に向かって進む
        (set! (.-y (.-position viper))
              (- CANVAS-HEIGHT (* comingTime 50)))

        ;; 一定の位置まで移動したら登場シーンを終了する
        (if (<= (.-y (.-position viper)) (- CANVAS-HEIGHT 100))
          (do
            ;; 登場シーンフラグを下ろす
            (set! (.-isComing viper) false)
            ;; 行き過ぎの可能性もあるので位置を再設定
            (set! (.-y (.-position viper)) (- CANVAS-HEIGHT 100))))

        ;; justTime を 100 で割ったときの
        ;; 余りが 50 より小さくなる場合だけ半透明にする
        (if (< (rem justTime 100) 50)
          (set! (.-globalAlpha ctx) 0.5))
        ))

    ;; 画像を描画する（現在の viper の位置に準じた位置に描画する）
    (.draw viper)

    ;; 恒常ループのために描画処理を再帰呼出しする
    (js/window.requestAnimationFrame
     #(render canvas util image viper))
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
          (let [viper (c/Viper. ctx 0 0 img)]
            ;; 初期化処理を行う
            (initialize canvas viper)

            ;; キーイベントを設定する
            (eventSetting viper)

            ;; 描画処理を行う
            (render canvas util img viper)
            )))
       ))))
