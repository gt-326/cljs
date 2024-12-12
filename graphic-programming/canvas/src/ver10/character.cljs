(ns ver10.character
  (:require [goog.object :as gobj]))

;; 座標を管理するためのクラス
(defn Position [x y]
  ;; constructor
  (this-as this
    (set! (.-x this) nil)
    (set! (.-y this) nil)

    ;; call method
    (.set this x y)

    ;; ここに this を置く必要はないみたい
    ;;this
    ))

;; [ Position.methods ]
(set! (.. Position -prototype -set)
      (fn [x y]
        (this-as this
          (if (not (nil? x))
            (set! (.-x this) x))

          (if (not (nil? y))
            (set! (.-y this) y)))))


;; キャラクター管理のための基幹クラス
(defn Character [ctx x y life imagePath]
  ;; constructor
  (let [img (js/Image.)]
    (this-as this
      (set! (.-position this) (Position. x y))
      (set! (.-ctx this) ctx)
      (set! (.-life this) life)

      ;; 画像
      (set! (.-src img) imagePath)
      (set! (.-image this) img)

      (set! (.-ready this) false)

      (.addEventListener
       (.-image this)
       "load"
       (fn []
         (do
           (set! (.-width this) (.-width img))
           (set! (.-height this) (.-height img))

           (set! (.-ready this) true)
           )))
      ;; 継承させるためには、ここに this を置かないといけないみたい
      this
      )))

;; [ Character.methods ]
(set! (.. Character -prototype -draw)
      #(this-as this
        (let [offset_x (/ (.-width this) 2)
              offset_y (/ (.-height this) 2)]

          (.drawImage
           (.-ctx this)
           (.-image this)

           ;; 画像の中心を指定する
           (- (.-x (.-position this)) offset_x)
           (- (.-y (.-position this)) offset_y)

           (.-width this)
           (.-height this))
          )))


;; viper クラス
(defn Viper [ctx x y imagePath]
  ;; extend
  (gobj/extend
      (.-prototype Viper)
      (.-prototype Character))

  ;; constructor
  (this-as this
    (.call Character this ctx x y 0 imagePath)

    ;; shots
    (set! (.-shotArray this) nil)
    (set! (.-singleShotArray this) nil)

    ;; ここに this を置く必要はないみたい
    ;;this
    ))

;; shot クラス
(defn Shot [ctx x y imagePath x2 y2]
  ;; extend
  (gobj/extend
      (.-prototype Shot)
      (.-prototype Character))

  ;; constructor
  (this-as this
    (.call Character this ctx x y 1 imagePath)

    (set! (.-vector this) (Position. x2 y2))
    ;; vector を有効にするためには、ここの this を有効にする必要がある
    this
    ))

;; [ Viper.methods ]
(set! (.. Viper -prototype -setShotArray)
      (fn [shots singleShots]
        (this-as this
          (set! (.-shotArray this) shots)
          (set! (.-singleShotArray this) singleShots))))

(set! (.. Viper -prototype -setComing)
      (fn [x_start y_start x_end y_end]
        ;; フィールドを追加
        (this-as this
          ;; 登場中のフラグを立てる
          (set! (.-isComing this) true)

          ;; 登場開始時のタイムスタンプを取得する
          (set! (.-comingStart this) (Date.now))

          ;; 登場演出を完了とする座標
          (set! (.-comingEndPosition this)
                (Position. x_end y_end))

          ;; 登場演出を開始する座標
          (.set (.-position this) x_start y_start))))

(set! (.. Viper -prototype -update)
      (fn [h w isKeyDown animationFrameCnt shotInterval]
        (this-as this
          (if (.-isComing this)

            ;;=================
            ;; 登場シーンの処理
            ;;=================
            (let [;; 現在までの経過時間を取得する
                  ;;（ミリ秒を秒に変換するため 1000 で除算）
                  ;; nowTime (/ (- (Date.now) @startTime) 1000)

                  justTime (Date.now)
                  comingTime (/ (- justTime (.-comingStart this)) 1000)]

              ;; 登場中は時間が経つほど上に向かって進む
              (set! (.-y (.-position this)) (- h (* comingTime 50)))

              ;; 一定の位置まで移動したら登場シーンを終了する
              (if (<= (.-y (.-position this)) (- h 100))
                (do
                  ;; 登場シーンフラグを下ろす
                  (set! (.-isComing this) false)
                  ;; 行き過ぎの可能性もあるので位置を再設定
                  (set! (.-y (.-position this)) (- h 100))))

              ;; justTime を 100 で割ったときの
              ;; 余りが 50 より小さくなる場合だけ半透明にする
              (if (< (rem justTime 100) 50)
                (set! (.-globalAlpha (.-ctx this)) 0.5)))

            ;;=================
            ;; 登場シーン以外
            ;;=================
            (let [viper_speed 3

                  viperX (atom (.-x (.-position this)))
                  viperY (atom (.-y (.-position this)))

                  offset_x (/ (.-width this) 2)
                  offset_y (/ (.-height this) 2)]

              ;; キーの押下状態を調べて挙動を変える
              (if (@isKeyDown :ArrowLeft)
                (reset! viperX (- @viperX viper_speed)))
              (if (@isKeyDown :ArrowRight)
                (reset! viperX (+ @viperX viper_speed)))
              (if (@isKeyDown :ArrowUp)
                (reset! viperY (- @viperY viper_speed)))
              (if (@isKeyDown :ArrowDown)
                (reset! viperY (+ @viperY viper_speed)))

              ;; 自機の現在地の座標を更新
              (.set (.-position this)
                    (min (max @viperX offset_x) (- w offset_x))
                    (min (max @viperY offset_y) (- h offset_y)))

              ;; ショット処理
              (if (@isKeyDown :z)
                ;; インターバル数未満のときは処理をおこなわない
                (if (>= @animationFrameCnt shotInterval)
                  ;; ショットのリスト分、ループする
                  (loop [shots (.-shotArray this)
                         singleShots (.-singleShotArray this)]
                    ;; ショットのリスト終端でないこと
                    (if (not
                         (and (empty? shots)
                              (empty? singleShots)))
                      ;; 発射されていない、または、
                      ;; 枠外に出たショットオブジェクトを探す
                      (if (every? #(pos? (.-life %))
                           (cons (first shots) (first singleShots)))
                        ;; 走査をつづける
                        (recur (rest shots) (rest singleShots))
                        ;; ショットの発射
                        (let [center (first shots)
                              [left right] (first singleShots)]

                          ;; ショットの現在地の座標を設定
                          (.set center
                                (.-x (.-position this))
                                (.-y (.-position this)))
                          ;; ショット左
                          (.set left
                                (.-x (.-position this))
                                (.-y (.-position this)))
                          ;; ショット右
                          (.set right
                                (.-x (.-position this))
                                (.-y (.-position this)))

                          ;; フレーム数をリセットする
                          (reset! animationFrameCnt 0)))
                      ))))

              ;; フレーム数をインクリメントする
              (swap! animationFrameCnt inc)
              ))

          ;; 画像を描画する（現在の viper の位置に準じた位置に描画する）
          (.draw this)

          ;; 念の為グローバルなアルファの状態を元に戻す
          (set! (.-globalAlpha (.-ctx this)) 1.0)
          )))

;; [ Shot.methods ]
(set! (.. Shot -prototype -set)
      (fn [x y]
        (this-as this
          (.set (.-position this) x y)
          (set! (.-life this) 1))))

(set! (.. Shot -prototype -setVector)
      (fn [x y]
        (this-as this
          (set! (.-x (.-vector this)) x)
          (set! (.-y (.-vector this)) y)
          )))

(set! (.. Shot -prototype -update)
      (fn []
        (this-as this
          ;; 発射済みのショットであること
          (if (pos? (.-life this))
            (let [shot_speed 7
                  current_x (.-x (.-position this))
                  current_y (.-y (.-position this))]

              ;;(.log js/console (str "current_y: " current_y))

              ;; 枠からはみ出たショットを無効化する
              (if (< (+ current_y (.-height this)) 0)
                (set! (.-life this) 0))

              ;; ショットの現在地の座標を更新する
              (set! (.-x (.-position this))
                    (+ current_x
                       (* (.-x (.-vector this)) shot_speed)))

              (set! (.-y (.-position this))
                    (+ current_y
                       (* (.-y (.-vector this)) shot_speed)))

              ;; ショット画像を描画する
              (.draw this)
              ))
          )))
