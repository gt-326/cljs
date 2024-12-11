(ns ver13.scene)

;;===========================
;; Class
;;===========================

(defn SceneManager []
  (this-as this
    (set! (.-scene this) {})
    (set! (.-activeScene this) nil)
    (set! (.-startTime this) nil)
    (set! (.-sceneFrameCnt this) 0)
    ;;this
    ))

;;===========================
;; Methods
;;===========================

;; [ SceneManager.methods ]
(set! (.. SceneManager -prototype -add)
      (fn [name updFnc]
        (this-as this
          (set! (.-scene this)
                (assoc (.-scene this) (keyword name) updFnc))
          )))

(set! (.. SceneManager -prototype -use)
      (fn [name]
        (this-as this
          (if (contains? (.-scene this) (keyword name))
            (do
              (set! (.-activeScene this)
                    ((.-scene this) (keyword name)))
              (set! (.-startTime this) (Date.now))
              (set! (.-sceneFrameCnt this) 0))))
        ))

(set! (.. SceneManager -prototype -sceneSetting)
      (fn [width height enemies]
        (this-as this

          (.add this
                "invade"
                (fn [time]
                  (if ;;(zero? (.-sceneFrameCnt this))
                      (> (.-sceneFrameCnt this) 100)
                      (loop [e @enemies]
                        (if (not (empty? e))
                          (if (pos? (.-life (first e)))
                            (do
                              ;;(.log js/console "aaa")
                              (recur (rest e)))

                            (let [enemy (first e)
                                  x (/ width 2)
                                  y (- (.-height enemy))]
                              ;;(.log js/console "bbb")

                              ;; 敵のステータスを更新する
                              ;;(.setEnemy enemy x y 1)
                              (.setEnemy enemy [x y 1])

                              ;; フレーム数をリセット：-1
                              (set! (.-sceneFrameCnt this) 0))
                            )))
                    )))

          (.use this "invade")
          )))

(set! (.. SceneManager -prototype -update)
      (fn [name]
        (this-as this
          (let [fncUdt (.-activeScene this)
                ;; シーンがアクティブになってからの経過時間（秒）
                ;; いまのところ意味がない
                activeTime
                (/ (- (Date.now) (.-startTime this)) 1000)]

            ;; 経過時間を引数に与えて updateFunction を呼び出す
            (fncUdt activeTime)

            ;; シーンを更新したのでカウンタをインクリメントする
            (set! (.-sceneFrameCnt this)
                  (inc (.-sceneFrameCnt this)))))
        ))
