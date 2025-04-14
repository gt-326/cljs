(ns ver01.core)

(defn imgLoader [path callback]
  (let [target
        ;; 画像のインスタンスを生成する
        (js/Image.)]

    ;; 画像がロード完了したときの処理を先に記述する
    (.addEventListener
     target
     "load"
     (fn []
       ;; もしコールバックがあれば呼び出す
       (if callback
         ;; コールバック関数の引数:img に画像を渡す
         (callback target))))

    ;; 画像のロードを開始するためにパスを指定する
    (set! (.-src target) path)
    ))


(defn initialize [canvas size]
  (do
    ;; canvas の大きさをウィンドウ全体を覆うように変更する
    (set! (.-width canvas) size)
    (set! (.-height canvas) size)
    ))


(defn render [ctx img size]
  (let [imageData
        ;; Canvas から ImageData を抽出する
        (.getImageData ctx 0 0 size size)]

    ;; まず画像をそのまま描画する
    (.drawImage ctx img 0 0)
    ;; コンソールにそのまま出力する
    (.log js/console imageData)
    ))


(let [IMG-SAMPLE "./img/sample.jpg"
      CANVAS_SIZE 512

      canvas
      ;; querySelector を利用して canvas を参照
      (.querySelector js/document.body "#main_canvas")
      ;; canvas からコンテキストを取得する
      ctx (.getContext canvas "2d")

      ;; 画像のインスタンスを生成する
      target2 (js/Image.)]

  (js/window.addEventListener
   "load"
   (fn []
     (imgLoader
      ;; 引数1: path
      IMG-SAMPLE
      ;; 引数2: callback
      (fn [img]
        (do
          ;; 初期化処理を行う
          (initialize canvas CANVAS_SIZE)
          ;; 描画処理を行う
          (render ctx img CANVAS_SIZE))
        ))

     ;; こっちでもイケる
     ;;     (imgLoader2
     ;;      ;; 引数0: target
     ;;      target2
     ;;      ;; 引数1: path
     ;;      IMG-SAMPLE
     ;;      ;; 引数2: callback
     ;;      (fn [img]
     ;;        (do
     ;;          ;; 初期化処理を行う
     ;;          (initialize canvas CANVAS_SIZE)
     ;;          ;; 描画処理を行う
     ;;          (render ctx img))
     ;;        ))

     )))


;; (defn imgLoader2 [target path callback]
;;   (do
;;
;;     ;; 画像がロード完了したときの処理を先に記述する
;;     (.addEventListener
;;      target
;;      "load"
;;      (fn []
;;        ;; もしコールバックがあれば呼び出す
;;        (if callback
;;          ;; コールバック関数の引数:img に画像を渡す
;;          (callback target))))
;;
;;     ;; 画像のロードを開始するためにパスを指定する
;;     (set! (.-src target) path)
;;     ))
