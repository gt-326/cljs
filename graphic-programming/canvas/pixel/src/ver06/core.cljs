(ns ver06.core
  (:require [ver06.filter :as f]))

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


(defn render [filter ctx img size]
  (do
    ;; まず、取得元の画像（切り出す対象）をそのまま描画する。
    (.drawImage ctx img 0 0)

    (let [imageData
          ;; Canvas から ImageData を抽出する
          (.getImageData ctx 0 0 size size)
          ;; フィルター処理を実行する
          outputData (filter ctx imageData)]

      ;; Canvas にたいして ImageData を書き戻す
      (.putImageData ctx outputData 0 0)
      )))


(let [IMG-SAMPLE "./img/sample2.jpg"
      CANVAS_SIZE 512
      ;; 加工用フィルター
      filter f/median

      ;; querySelector を利用して canvas を参照
      canvas (.querySelector js/document.body "#main_canvas")
      ;; canvas からコンテキストを取得する
      ctx (.getContext canvas "2d")]

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
          (render filter ctx img  CANVAS_SIZE))
        ))
     )))
