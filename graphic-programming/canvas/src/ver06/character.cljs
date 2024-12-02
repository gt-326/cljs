(ns ver06.character
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
(defn Character [ctx x y life image]
  ;; constructor
  (this-as this
    (set! (.-position this) (Position. x y))
    (set! (.-ctx this) ctx)
    (set! (.-life this) life)
    (set! (.-image this) image)

    ;; 画像の大きさを取得して保持させる
    (set! (.-width this) (.-width image))
    (set! (.-height this) (.-height image))

    ;; 継承させるためには、ここに this を置かないといけないみたい
    this))

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
(defn Viper [ctx x y image]
  ;; extend
  (gobj/extend
      (.-prototype Viper)
      (.-prototype Character))

  ;; constructor
  (this-as this
    (.call Character this ctx x y
;; ここで引数に設定しなくてもよかった
;;           (.-width image)
;;           (.-height image)
           0 image)
    ;; ここに this を置く必要はないみたい
    ;;this
    ))

;; [ Viper.methods ]
(set! (.. Viper -prototype -setComing)
      (fn [x_start y_start x_end y_end]
        (do
          ;; フィールドを追加
          (this-as this
            ;; 登場中のフラグを立てる
            (set! (.-isComing this) true)

            ;; 登場開始時のタイムスタンプを取得する
            (set! (.-comingStart this) (Date.now))

            ;; 登場演出を完了とする座標
            (set! (.-comingEndPosition this)
                  (Position. x_end y_end))

            ;; 登場演出を開始した際のタイムスタンプ
            (.set (.-position this) x_start y_start))
          )))

(set! (.. Viper -prototype -update)
      (fn [h w]
        (this-as this

          ;; グローバルなアルファを必ず 1.0 で描画処理を開始する
          (set! (.-globalAlpha (.-ctx this)) 1.0)

          ;; 登場シーンの処理
          (if (.-isComing this)
            (let [ ;; 現在までの経過時間を取得する
                  ;;（ミリ秒を秒に変換するため 1000 で除算）
                  ;; nowTime (/ (- (Date.now) @startTime) 1000)

                  justTime (Date.now)
                  comingTime (/ (- justTime (.-comingStart this)) 1000)]

              ;; 登場中は時間が経つほど上に向かって進む
              (set! (.-y (.-position this))
                    (- h (* comingTime 50)))

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
                (set! (.-globalAlpha (.-ctx this)) 0.5))
              ))

          ;; 画像を描画する（現在の viper の位置に準じた位置に描画する）
          (.draw this)
          )))
