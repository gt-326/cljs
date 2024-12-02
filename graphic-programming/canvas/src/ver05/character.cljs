(ns ver05.character
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

    ;; 継承させるためには、ここに this を置かないといけないみたい
    this
    ))

;; [ Character.methods ]
(set! (.. Character -prototype -draw)
      (fn []
        (this-as this
          (.drawImage
           (.-ctx this)
           (.-image this)
           (.-x (.-position this))
           (.-y (.-position this))))))

;; viper クラス
(defn Viper [ctx x y image]
  ;; extend
  (gobj/extend
      (.-prototype Viper)
      (.-prototype Character))

  ;; constructor
  (this-as this
    (.call Character this ctx x y 0 image)
    ;; ここに this を置く必要はないみたい
    ;;this
    ))

;; [ Viper.methods ]
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

          ;; 登場演出を開始した際のタイムスタンプ
          (.set (.-position this) x_start y_start))))
