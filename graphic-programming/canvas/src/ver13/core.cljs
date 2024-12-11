(ns ver13.core
  (:require [ver13.util :as util]
            [ver13.canvas2d :as c2d]
            [ver13.character :as c]

            [ver13.scene :as s]))

;; Utilities

;;===========================

(def CANVAS-WIDTH 640)
(def CANVAS-HEIGHT 480)

(def IMG-VIPER "./img/viper.png")
(def IMG-SHOT "./img/viper_shot.png")
(def IMG-SINGLE "./img/viper_single_shot.png")

(def IMG-ENEMY "./img/enemy_small.png")
(def IMG-ENEMY-SHOT "./img/enemy_shot.png")

;; 270°
(def DEGREE-DIR-UP 270)

(defn initialize [canvas ctx
                  [viper maxShotCnt]
                  [enemies maxShotCntEnemy]]
  (let [fncGenShot
        (fn [img speed] #(c/Shot. ctx 0 0 img speed %))

        shots
        (util/myRepeat maxShotCnt (fncGenShot IMG-SHOT 7)
         (DEGREE-DIR-UP))

        singleShots
        (util/myRepeat maxShotCnt (fncGenShot IMG-SINGLE 7)
         ((- DEGREE-DIR-UP 20) (+ DEGREE-DIR-UP 20)))

        shotsEnemy
        (util/myRepeat maxShotCntEnemy
                       (fncGenShot IMG-ENEMY-SHOT 1)
                       ((- DEGREE-DIR-UP 90)))
        ]

    (set! (.-width canvas) CANVAS-WIDTH)
    (set! (.-height canvas) CANVAS-HEIGHT)

    (.setComing viper
                (/ CANVAS-WIDTH 2)
                (+ CANVAS-HEIGHT 50)
                (/ CANVAS-WIDTH 2)
                (- CANVAS-HEIGHT 100))

    (.setShotArray viper shots singleShots)

    ;; 敵
    (doseq [e @enemies]
      (.setShotArray e shotsEnemy))

    ))

(defn eventSetting [isKeyDown]
  (letfn [(fnc [event_name val]
            (js/window.addEventListener
             event_name
             (fn [event]
               (do
                 ;;(.log js/console (.-key event))
                 (swap! isKeyDown
                        assoc (keyword (.-key event)) val)
                 ))))]

    ;; keydown イベント
    (fnc "keydown" true)

    ;; keyup イベント
    (fnc "keyup" false)
    ))

(defn render [util viper scene enemies
              keyStat animationFrameCnt shotInterval]
  (do
    ;; 描画前に画面全体を不透明な明るいグレーで塗りつぶす
    (.drawRect util "#eeeeee" 0 0 CANVAS-WIDTH CANVAS-HEIGHT)

    ;; 自機の描画、ショットの発射など
    (.update viper CANVAS-HEIGHT CANVAS-WIDTH
             keyStat animationFrameCnt shotInterval)

    ;; ショットの描画
    (doseq [s (.-shotArray viper)]
      (.update (first s) CANVAS-HEIGHT CANVAS-WIDTH false))

    ;; 左右ショットの描画
    (doseq [[left right] (.-singleShotArray viper)]
      (.update left CANVAS-HEIGHT CANVAS-WIDTH true)
      (.update right CANVAS-HEIGHT CANVAS-WIDTH true))

    ;; シーンを更新する
    (.update scene)

    ;; 敵キャラクターの状態を更新する
    (if (not (.-isComing viper))
      (let [cnt (atom 0)]
        (doseq [e @enemies]
          (.update e CANVAS-WIDTH CANVAS-HEIGHT
                   (.-sceneFrameCnt scene))
          ;; ショットの状態は、各敵で共有しているので一度だけ。
          (if (zero? @cnt)
            ;; 敵（@enemies）のショットの描画
            (doseq [s (.-shotArray e)]
              (.update (first s)
                       CANVAS-HEIGHT CANVAS-WIDTH false)))
          ;; カウントアップ
          (swap! cnt inc))))

    ;; 恒常ループのために描画処理を再帰呼出しする
    (js/window.requestAnimationFrame
     #(render util viper scene enemies
              keyStat animationFrameCnt shotInterval))
    ))


(defn loadCheck [util viper enemies scene isKeyDown
                 [flg1 flg2 flg3 flg4]]
  (let [shotInterval 10
        animationFrameCnt (atom shotInterval)
        fnc (fn [f shots]
              (every?
               identity
               (map f shots)))]

    (.log js/console
          (str "aaa: " flg1 " " flg2 " " flg3 " " flg4))

    (if (and flg1 flg2 flg3 flg4)
      (do
        ;; キーイベントを設定する
        (eventSetting isKeyDown)

        (.sceneSetting scene CANVAS-WIDTH CANVAS-HEIGHT enemies)

        ;; 描画処理を行う
        (render util viper scene enemies
                isKeyDown animationFrameCnt shotInterval))

      (let [f1 (atom flg1)
            f2 (atom flg2)
            f3 (atom flg3)
            f4 (atom flg4)]

        (if (not @f1)
          (reset! f1 (.-ready viper)))

        (if (not @f2)
          (reset! f2 (fnc
                      #(.-ready (first %))
                      (.-shotArray viper))))

        (if (not @f3)
          (reset! f3 (fnc
                      (fn [[l r]] (and (.-ready l) (.-ready r)))
                      (.-singleShotArray viper))))

        (if (not @f4)
          (reset! f4 (fnc
                      #(.-ready %)
                      @enemies)))

        ;; 自機、敵、ショットオブジェクトのロード待ち
        (js/setTimeout
         #(loadCheck util viper enemies scene isKeyDown
                     [@f1 @f2 @f3 @f4]) 100)))
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
     (let [viper (c/Viper. ctx 0 0 IMG-VIPER 3 DEGREE-DIR-UP)
           maxShotCnt 10

           enemies
           (atom
            (repeatedly
             10
             #(c/Enemy. ctx 0 0
                        IMG-ENEMY 1 (- DEGREE-DIR-UP 180))))

           scene (s/SceneManager.)]

       ;; 初期化処理を行う
       (initialize canvas ctx
                   [viper 10] [enemies 50])

       ;; ゲーム処理
       (loadCheck util viper enemies scene isKeyDown
                  [false false false false])
       ))))
