(ns ver13.character
  (:require
   [ver13.util :as util]
   [goog.object :as gobj]))

;;===========================
;; Utilities
;;===========================

;; 左右ショットと、一方向のショットの両方を扱っている。
(defn shoot [x y fnc shot_type]
  (loop [shots shot_type]
    ;; ショットのリスト終端でないこと
    (if (not (empty? shots))
      ;; 発射されていない、または、
      ;; 枠外に出たショットオブジェクトを探す
      ;; 左右のショットを別ものとして扱うために、some を用いている。
      (if (some fnc (first shots))
        ;; 走査をつづける
        (recur (rest shots))
        ;; ショットの発射
        (do
          (doseq [shot (first shots)]
            ;; 左右でショットの状態が異なる場合を想定
            (if (<= (.-life shot) 0)
              (.setShot shot [x y 1])))
          true)))))

(defn outOfCanvas? [h w obj]
  (or (< (+ (.-y (.-position obj)) (.-height obj)) 0)
      (> (+ (.-y (.-position obj)) (.-height obj)) h)

      (< (+ (.-x (.-position obj)) (.-width obj)) 0)
      (> (+ (.-x (.-position obj)) (.-width obj)) w)))

(defn calcShotStatus [h w obj]
  (let [shot_speed (.-speed obj)
        x (.-x (.-position obj))
        y (.-y (.-position obj))]

    [ ;; 座標を更新する
     (+ x (* (.-x (.-vector obj)) shot_speed))
     (+ y (* (.-y (.-vector obj)) shot_speed))
     ;; 枠からはみ出たら、無効にする
     (if (outOfCanvas? h w obj) 0 1)]
    ))

;;===========================
;; Class
;;===========================

;; 座標を管理するためのクラス
(defn Position [[x y]]
  ;; constructor
  (this-as this
    (set! (.-x this) nil)
    (set! (.-y this) nil)

    ;; call method
    (.set this x y)

    ;; ここに this を置く必要はないみたい
    ;;this
    ))

;; キャラクター管理のための基幹クラス
(defn Character [ctx x y life imagePath speed]
  ;; constructor
  (let [img (js/Image.)]
    (this-as this
      (set! (.-position this) (Position. [x y]))
      (set! (.-ctx this) ctx)
      (set! (.-life this) life)

      (set! (.-speed this) speed)

      ;; 画像 ====================================
      (set! (.-ready this) false)

      (set! (.-src img) imagePath)
      (set! (.-image this) img)

      (.addEventListener
       (.-image this)
       "load"
       (fn []
         (do
           (set! (.-width this) (.-width img))
           (set! (.-height this) (.-height img))

           (set! (.-ready this) true)
           )))
      ;;=========================================

      ;; 継承させるためには、ここに this を置かないといけないみたい
      this
      )))

;; viper クラス
(defn Viper [ctx x y imagePath speed [a v]]
  ;; extend
  (gobj/extend
      (.-prototype Viper)
      (.-prototype Character))

  ;; constructor
  (this-as this
    ;; 親クラス
    (.call Character this ctx x y 0 imagePath speed)

    ;; 子クラスフィールド設定
    (set! (.-angle this) a)
    (set! (.-vector this) v)
    ;; shots
    (set! (.-shotArray this) nil)
    (set! (.-singleShotArray this) nil)

    ;; ここに this を置く必要はないみたい
    ;;this
    ))

;; enemy クラス
(defn Enemy [ctx x y imagePath speed [a v]]
  ;; extend
  (gobj/extend
      (.-prototype Enemy)
      (.-prototype Character))

  ;; constructor
  (this-as this
    ;; 親クラス
    (.call Character this ctx x y 0 imagePath speed)

    ;; 子クラスフィールド設定
    (set! (.-angle this) a)
    (set! (.-vector this) v)
    ;; shots
    (set! (.-shotArray this) nil)

    ;; ここに this を置く必要はないみたい
    ;;this
    ))

;; shot クラス
(defn Shot [ctx x y imagePath speed [a v]]
  ;; extend
  (gobj/extend
      (.-prototype Shot)
    (.-prototype Character))

  ;; constructor
  (this-as this
    ;; 親クラス
    (.call Character this ctx x y 0 imagePath speed)

    ;; 子クラスフィールド設定
    (set! (.-angle this) a)
    (set! (.-vector this) v)

    ;; vector を有効にするためには、ここの this を有効にする必要がある。
    ;; コメントアウトすると、loadCheck(): f2 f3 が有効にならない。
    this
    ))

;;===========================
;; Methods
;;===========================

;; [ Position.methods ]
(set! (.. Position -prototype -set)
      (fn [x y]
        (this-as this
          (if (not (nil? x))
            (set! (.-x this) x))

          (if (not (nil? y))
            (set! (.-y this) y)))))

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

(set! (.. Character -prototype -rotateDraw)
      (fn []
        (this-as this
          (let [offset_x (/ (.-width this) 2)
                offset_y (/ (.-height this) 2)]

            ;; 座標系を回転する前の状態を保存する
            (.save (.-ctx this))

            ;; 自身の位置が座標系の中心と重なるように平行移動する
            (.translate (.-ctx this)
                        (.-x (.-position this))
                        (.-y (.-position this)))

            ;; 座標系を回転させる（270度の位置が基準）。
            ;; フィールド「angle」から Math.PI * 1.5 （270度）を
            ;; 引いた差分を「傾き」とする。
            (.rotate (.-ctx this)
                     (- (.-angle this) (* Math.PI 1.5)))

            (.drawImage
             (.-ctx this)
             (.-image this)

             ;; 先に translate で平行移動しているのでオフセットのみ行う
             (- offset_x)
             (- offset_y)

             (.-width this)
             (.-height this))

            ;; 座標系を回転する前の状態に戻す
            (.restore (.-ctx this))
            ))
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
                (Position. [x_end y_end]))

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
            (let [viper_speed (.-speed this)

                  x (.-x (.-position this))
                  y (.-y (.-position this))

                  viperX (atom x)
                  viperY (atom y)

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

              ;;=================
              ;; ショット処理
              ;;=================
              (if (@isKeyDown :z)
                ;; インターバル数未満のときは処理をおこなわない
                (if (>= @animationFrameCnt shotInterval)
                  ;; ショット２種類の処理
                  (doseq [shot_type (list
                                     (.-shotArray this)
                                     (.-singleShotArray this))]
                    ;; ショットのリスト分、ループする
                    (if (shoot x y #(pos? (.-life %)) shot_type)
                      ;; フレーム数をリセットする
                      (reset! animationFrameCnt -1)))))

              ;; フレーム数をインクリメントする
              (swap! animationFrameCnt inc)
              ))

          ;; 画像を描画する（現在の viper の位置に準じた位置に描画する）
          (.draw this)

          ;; 念の為グローバルなアルファの状態を元に戻す
          (set! (.-globalAlpha (.-ctx this)) 1.0)
          )))

;; [ Shot.methods ]
(set! (.. Shot -prototype -setShot)
      (fn [[x y life]]
        (this-as this
          (.set (.-position this) x y)
          (set! (.-life this) life)
          )))

(set! (.. Shot -prototype -update)
      (fn [h w rotateFlg]
        (this-as this
          ;; 発射済みのショットであること
          (if (pos? (.-life this))
            (do
              ;; パラメーターを更新
              (.setShot this (calcShotStatus h w this))
              ;; ショット画像を描画する（ローテート有／無）
              (if rotateFlg
                (.rotateDraw this)
                (.draw this))
              ))
          )))

;; [ Enemy.methods ]
(set! (.. Enemy -prototype -setShotArray)
      (fn [shots]
        (this-as this
          (set! (.-shotArray this) shots))))

(set! (.. Enemy -prototype -setEnemy)
      (fn ;;[x y life & type]
        [[x y life & type]]
        (this-as this
          (.set (.-position this) x y)
          (set! (.-life this) life)
          (set! (.-type this)
                (if (empty? type) "default" (first type)))
          )))

(set! (.. Enemy -prototype -update)
      (fn [h w sceneFrameCnt]
        (this-as this
          (if (pos? (.-life this))
            (do
              (case (.-type this)
                "default"
                (do
                  ;; 攻撃
                  (if (= sceneFrameCnt 50)
                    (.fire this this))

                  ;; パラメーターを更新
                  (.setEnemy this (calcShotStatus h w this))))

              ;; 敵画像を描画する
              (.draw this)))
          )))

(set! (.. Enemy -prototype -fire)
      (fn [obj]
        (shoot
         (.-x (.-position obj))
         (.-y (.-position obj))
         #(pos? (.-life %)) (.-shotArray obj))))
