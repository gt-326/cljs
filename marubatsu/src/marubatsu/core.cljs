(ns marubatsu.core
  (:require [marubatsu.util :as util]
            [marubatsu.computer :as com]
            [clojure.core.async :as ca]))

;;=== Const  ====================

(def STATES ["empty" "alive" "dying"])

(def STATE-EMPTY 0)
(def STATE-ALIVE 1)
(def STATE-DYING 2)

(def TURN-HUMAN 1)
(def TURN-COMPUTER 2)

;;=== Utility fnc  ====================

(defn getHtmlElementById [id]
  (.getElementById js/document id))

(defn createHtmlElementAsChild [obj type]
  (.appendChild obj (.createElement js/document type)))

(defn setAttribute [obj param val]
  (.setAttribute obj param val))

(defn setObjectAttributeClass [obj idx]
  (setAttribute obj "class" (get STATES idx)))

(defn setCellAttributeClass [idx state]
  (setObjectAttributeClass
   (getHtmlElementById idx) state))

(defn initBoard [size]
  (vec (repeat (* size size) 0)))

(defn isAttributeClassDead? [obj]
  (= (.getAttribute obj "class")
     (get STATES STATE-EMPTY)))

;;=== Board fnc  ====================

(defn gen-board [n] (vec (repeat (* n n) 0)))

(defn gen-win-pattern [n]
  (concat
   ;; yoko
   (partition n (range (* n n)))

   ;; tate
   (for [i (range n)]
     (map #(+ i %) (range 0 (* n n) n)))

   ;; naname
   (list
    (range 0 (* n n) (inc n))
    (range (dec n) (dec (* n n)) (dec n)))
   ))

(defn conv_to_OX [turn]
  (if (= TURN-HUMAN turn) "O" "X"))

(defn upd-status [board log turn current]
  (let [current-status (first current)]
    ;; 現在のボードの状態を更新
    (reset! board current)

    ;; undo 用の情報
    (reset! log
            (conj @log {:i (:i current-status)
                        ;; 相手の手番を設定する
                        :t (com/get_turn_next turn)}))

    ;; 現在のボードの状態
    ;; {:b board-new
    ;;  :t turn
    ;;  :i idx
    ;;  :s score
    ;;  :l lives-new}
    current-status))


(defn set-btn-disabled [flg]
  (doseq [id ["undo" "quit"]
          :let [btn (getHtmlElementById id)]]
    ;; 色変え
    (.setAttribute
     btn "style"
     (if flg
       "background: #587559"
       "background: linear-gradient(to bottom right, #C5DEC6, #587559"))
    ;; 使用可、不可
    (set! (.-disabled btn) flg))
  )

(defn show-result [info t-start turn cnt]
  (do
    ;; ゲーム終了表示
    (js/alert
     (str
      "\n[ the lead : " (conv_to_OX (inc t-start)) " ]"
      " [ end : " (conv_to_OX turn) " wins ]"
      " [ cnt : " cnt " ]"))

    ;; クリックイベントを無効化
    (doseq [idx (range (count (:b info)))
            :let [td (getHtmlElementById (str "td" idx))]]
      (set! (.-onclick td) nil))

    ;; ボタン制御：使用不可
    (set-btn-disabled true)))

(defn win2? [win-pttrns board opr size]
  (some
   #(= size (count %))
   (map
    #(for [idx %
           :let [stone (nth board idx)]
           :when (opr stone)]
       idx)
    win-pttrns)))

(defn random-choosing-from-bests [data]
  (let [
        ;; スコアでソートした先頭の手を取得する
        best-choice
        (first
         (sort-by #((first %) :s) > data))

        ;; 最高スコアを取得する
        high-score (:s (first best-choice))

        ;; 最高スコアを持つ、すべての手を取得する
        bests
        (vec (filter #(= high-score (:s (first %))) data))

        ;; スコアが同じ場合、ランダムに手を選ぶ
        idx (rand-int (count bests))
        ]

    ;; 最高スコアの手のフィールド idx を返す
    (:i (first (bests idx)))
    ))


;;=== Print Grid cells ===========

(defn print-board [info]
  (doseq [idx (range (count (:b info)))
          :let [td (getHtmlElementById (str "td" idx))]]

    (if (zero? ((:l info) idx))
      (do
        (set! (.-innerHTML td) "")
        (setObjectAttributeClass td (get STATES STATE-EMPTY)))

      (do
        (set! (.-innerHTML td) (conv_to_OX ((:b info) idx)))

        ;; life:2 以下のセルの色を変える
        (setObjectAttributeClass td
                                 (if (< 2 ((:l info) idx))
                                   STATE-ALIVE
                                   STATE-DYING))

        ;; life:1 の場合、文字を白くする
        (.setAttribute
         td "style"
         (if (= 1 ((:l info) idx)) "color:white" "color:black"))

        ))))

;;=== Grid cell click event ===========

(defn cellClickHandler [idx board obj log win-pttrns turn size]
  ;; クリックしたパネルが空欄のとき
  (if (empty? (.-innerHTML obj))
    (ca/go-loop [cnt 0
                 t TURN-HUMAN
                 i idx]

      (if (< cnt 2)
        (let [current
              (first
               (filter #(= i (:i (first %))) (rest @board)))

              ;; 更新処理
              b-print
              (upd-status board log t current)]

          ;; ボード表示
          (print-board b-print)

          (if (or (= 1 (inc turn) (count @log))
                  (= 2 (inc turn) (count @log)))
            ;; ボタン制御：使用可
            (set-btn-disabled false))

          ;; ゲーム終了判定
          (if (win2? win-pttrns (:b b-print) #(= t %) size)
            (do
              (ca/<! (ca/timeout 1000))
              (show-result b-print turn t (count @log)))

            (do
              ;; 処理継続（手番交代）
              (ca/<! (ca/timeout 1000))

              (recur
               (inc cnt)
               (com/get_turn_next t)
               (random-choosing-from-bests (rest current)))
              ))
          )))
    ))

;;=== Initialize Panels ================

(defn createTableOnGridContainer
  [board size log win-pttrns t-start]
  (let [gc (getHtmlElementById "gridContainer")]
    (if gc
      (do
        ;; 初期化
        (set! (.-innerHTML gc) "")

        (util/console-log
         "html_tbl"
         gc
         size
         (fn [i j obj]
           (let [idx (+ (* i size) j)
                 idx_state (get (:b (first @board)) idx)
                 str_state (get STATES (js/parseInt idx_state))]

             (setAttribute obj "class" str_state)
             (setAttribute obj "id" (str "td" idx))

             (set! (.-onclick obj)
                   ;; クリックイベント
                   #(cellClickHandler idx board obj log win-pttrns t-start size)))))
        ))))

;;=== Reload Button event ================

(defn rewind [board log]
  (first
   (reduce
    (fn [b {idx :i turn :t}]
      (cons
       (first
        (filter
         #(and
           (= idx (:i (first %)))
           (= turn (:t (first %))))

         (rest (first b))))
       b))
    ;; acc
    (list board)
    ;; keys
    log)))

(defn fnc-undo [board log all-board]
  (let [n (- (count @log) 2)]
    ;;　idx が負数でない場合
    (if (<= 0 n)
      ;; 更新処理をおこなう
      (let [log-undo (vec (take n @log))]
        ;; undo 用の情報
        (reset! log log-undo)
        ;; ボードの情報（特定の手まで、開始時点から「完全読み」を辿る）
        (reset! board (rewind all-board log-undo))
        ;; メッセージ表示
        (js/alert "[ undo ]")

        (if (<= (count @log) 1)
          ;; ボタン制御：使用不可
          (set-btn-disabled true))
        ))

    (first @board)))

(defn undoButtonHandler [board log all-board]
  (let [board-redo (fnc-undo board log all-board)]
    ;; 再描画
    (print-board board-redo)))

;;=== Computer First Hand  ================

(defn first-hand-computer [board log turn-int all-board]
  ;; computer 先手
  (let [current
        (first
         (sort-by #((first %) :s) > (rest all-board)))]

    (upd-status board log turn-int current)))


(defn common [size board log turn selected-board msg]
  (let [win-pttrns (gen-win-pattern size)]
    ;; ボタン制御：使用不可
    (set-btn-disabled true)
    ;; パネル設定
    (createTableOnGridContainer board size log win-pttrns turn)

    (if (= TURN-HUMAN turn)
      (let [b (first-hand-computer
               board
               log
               (inc turn)
               selected-board)]
        ;; 描画
        (print-board b)))

    (js/alert (str msg (conv_to_OX (inc turn))))
    ))

;;=== Quit Button event ================

(defn reload [board-perfect board log]
  (let [turn (rand-int 2)
        selected-board (get board-perfect turn)]

    ;;======================
    ;; リセット
    (reset! board selected-board)
    (reset! log [])
    ;;======================

    (common
     ;; 数値に明示的に変換しないとバグる
     (js/parseInt (.-value (getHtmlElementById "size")))
     board log turn selected-board "restart: ")
    ))

(defn quitButtonHandler [board log board-perfect]
  (reload board-perfect board log))

;;=== Restart Button event ================

(defn restartButtonHandler [board log board-perfect]
  (reload board-perfect board log))

;;=== Setup Buttons  ================

(defn setupControlButtons [board log turn board-perfect]
  (let [btn1 (getHtmlElementById "undo")
        btn2 (getHtmlElementById "quit")
        btn3 (getHtmlElementById "restart")]

    (set! (.-onclick btn1)
          #(undoButtonHandler
            board log (get board-perfect turn)))
    (set! (.-onclick btn2)
          #(quitButtonHandler board log board-perfect))
    (set! (.-onclick btn3)
          #(restartButtonHandler board log board-perfect))
    ))

(defn gen-board-perfect [size]
  (let [win-pttrns (gen-win-pattern size)
        init-board (gen-board size)
        board-lives (gen-board size)]
    ;; 完全読み
    [(com/think6
      win-pttrns init-board board-lives size TURN-HUMAN)
     (com/think6
      win-pttrns init-board board-lives size TURN-COMPUTER)]
    ))

;;=== Init Pull Down Menu  ================

(defn initSizeMenuPulldown [board log fnc]
  (let [pulldown (getHtmlElementById "size")]
    ;; 項目
    (doseq [num [3 5 7]]
      (let [opt (.createElement js/document "option")]
        (set! (.-text opt) num)
        (set! (.-value opt) num)
        ;; 追加
        (.appendChild pulldown opt)))

    ;; onchange：イベント
    (set!
     (.-onchange pulldown)
     #(fnc
       [(js/parseInt (.-value pulldown)) "resize: " false]
       ;; 数値に明示的に変換しないとバグる
       (gen-board-perfect (js/parseInt (.-value pulldown)))
       board log))
    ))

(defn set-controllers [[size msg flg] board-perfect board log]
  (let [turn (rand-int 2)
        selected-board (get board-perfect turn)]

    ;;======================
    ;; リセット
    (reset! board selected-board)
    (reset! log [])
    ;;======================

    ;; ボタン制御
    (setupControlButtons board log turn board-perfect)

    ;; ブルダウン
    (if flg
      (initSizeMenuPulldown board log set-controllers))

    (common size board log turn selected-board msg)
    ))

;;=== Initialize  ================

(let [board (atom nil)
      log (atom nil)
      size 3
      board-perfect (gen-board-perfect size)]

  (defn initialize []
    (set-controllers
     [size "start: " true]
     board-perfect board log)))

;;==========================
;; Start everything
;;==========================
(set! (.-onload js/window) #(initialize))
