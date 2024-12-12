(ns ver07.core
  (:require [ver07.canvas2d :as c2d]
            [ver07.character :as c]))

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

(defn eventSetting [isKeyDown]
  (letfn [(fnc [event_name val]
            (js/window.addEventListener
             event_name
             (fn [event]
               (swap! isKeyDown assoc (keyword (.-key event)) val))))]
    ;; keydown イベント
    (fnc "keydown" true)

    ;; keyup イベント
    (fnc "keyup" false)
    ))

(defn render [util image viper keyStat]
  (do
    ;; 描画前に画面全体を不透明な明るいグレーで塗りつぶす
    (.drawRect util "#eeeeee" 0 0 CANVAS-WIDTH CANVAS-HEIGHT)

    ;; 登場シーンの処理
    (.update viper CANVAS-HEIGHT CANVAS-WIDTH keyStat)

    ;; 恒常ループのために描画処理を再帰呼出しする
    (js/window.requestAnimationFrame
     #(render util image viper keyStat))
    ))

(let [canvas (.querySelector js/document.body "#main_canvas")
      util (c2d/Canvas2DUtility. canvas)

      isKeyDown (atom {:ArrowLeft false
                       :ArrowRight false
                       :ArrowUp false
                       :ArrowDown false})]

  (js/window.addEventListener
   "load"
   (fn []
     (do
       (.imgLoader
        util
        "./img/viper.png"
        (fn [img]
          (let [viper (c/Viper. (.getContext util) 0 0 img)]
            ;; 初期化処理を行う
            (initialize canvas viper)

            ;; キーイベントを設定する
            (eventSetting isKeyDown)

            ;; 描画処理を行う
            (render util img viper isKeyDown)
            )))
       ))))
