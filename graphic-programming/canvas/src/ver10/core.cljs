(ns ver10.core
  (:require [ver10.canvas2d :as c2d]
            [ver10.character :as c]))

(def CANVAS-WIDTH 640)
(def CANVAS-HEIGHT 480)

(def IMG-VIPER "./img/viper.png")
(def IMG-SHOT "./img/viper_shot.png")
(def IMG-SINGLE "./img/viper_single_shot.png")

(defn initialize [canvas ctx viper maxShot]
  (let [shots
        (repeatedly
         maxShot
         #(c/Shot. ctx 0 0 IMG-SHOT 0.0 -1.0))

        singleShots
        (repeatedly
         maxShot
         #(list
           (c/Shot. ctx 0 0 IMG-SINGLE -1 -0.9) ;; 左
           (c/Shot. ctx 0 0 IMG-SINGLE  1 -0.9) ;; 右
           ))]

    (set! (.-width canvas) CANVAS-WIDTH)
    (set! (.-height canvas) CANVAS-HEIGHT)

    (.setComing viper
                (/ CANVAS-WIDTH 2)
                (+ CANVAS-HEIGHT 50)
                (/ CANVAS-WIDTH 2)
                (- CANVAS-HEIGHT 100))

    (.setShotArray viper shots singleShots)
    ))

(defn eventSetting [isKeyDown]
  (letfn [(fnc [event_name val]
            (js/window.addEventListener
             event_name
             (fn [event]
               (do
                 ;;(.log js/console (.-key event))
                 (swap! isKeyDown assoc (keyword (.-key event)) val)
                 ))))]

    ;; keydown イベント
    (fnc "keydown" true)

    ;; keyup イベント
    (fnc "keyup" false)
    ))

(defn render [util viper keyStat animationFrameCnt shotInterval]
  (do
    ;; 描画前に画面全体を不透明な明るいグレーで塗りつぶす
    (.drawRect util "#eeeeee" 0 0 CANVAS-WIDTH CANVAS-HEIGHT)

    ;; 自機の描画、ショットの発射など
    (.update viper CANVAS-HEIGHT CANVAS-WIDTH
             keyStat animationFrameCnt shotInterval)

    ;; ショットの描画
    (doseq [s (.-shotArray viper)]
      (.update s))

    ;; 左右ショットの描画
    (doseq [[left right] (.-singleShotArray viper)]
      (.update left)
      (.update right))

    ;; 恒常ループのために描画処理を再帰呼出しする
    (js/window.requestAnimationFrame
     #(render util viper keyStat animationFrameCnt shotInterval))
    ))

(defn loadCheck [util viper isKeyDown]
  (let [shotInterval 10
        animationFrameCnt (atom shotInterval)

        flg (every?
              identity
              (map #(.-ready %) (.-shotArray viper)))

        flg2 (every?
              identity
              (map
               (fn [[l r]] (and (.-ready l) (.-ready r)))
               (.-singleShotArray viper)))]

;;    (.logzz js/console (str flg ":" flg2 ":" (.-ready viper)))

    (if (and flg flg2 (.-ready viper))
      (do
        ;; キーイベントを設定する
        (eventSetting isKeyDown)
        ;; 描画処理を行う
        (render util viper
                isKeyDown animationFrameCnt shotInterval))

      (do
        ;; 自機、ショットオブジェクトのロード待ち
        (js/setTimeout
         #(loadCheck util viper isKeyDown) 100)))
    ))

(let [canvas (.querySelector js/document.body "#main_canvas")
      util (c2d/Canvas2DUtility. canvas)
      ctx (.getContext util)

      isKeyDown (atom {:ArrowLeft  false
                       :ArrowRight false
                       :ArrowUp    false
                       :ArrowDown  false

                       :z          false})]

  (js/window.addEventListener
   "load"
   (fn []
     (let [viper (c/Viper. ctx 0 0 IMG-VIPER)
           maxShot 10]

       ;; 初期化処理を行う
       (initialize canvas ctx viper maxShot)
       ;; ゲーム処理
       (loadCheck util viper isKeyDown)
       ))))