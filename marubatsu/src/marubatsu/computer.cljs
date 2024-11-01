(ns marubatsu.computer)

;;=== Const  ====================

(def TURN-HUMAN 1)
(def TURN-COMPUTER 2)

;;=== Utility fnc  ====================

(defn canPutIdxes [board]
  (for [i (range (count board))
        :when (= 0 (board i))] i))

(defn get_turn_next [turn]
  ([TURN-HUMAN TURN-COMPUTER] (- TURN-COMPUTER turn)))

(defn get-lines-to-win2 [lines board]
  (for [idx (canPutIdxes board)
        :let [vec-positions
              (for [l lines
                    :when (some #(= % idx) l)] l)]]
    {:idx idx :lines vec-positions}))

(defn get-guard-points2 [board idxes opponent-stone]
  (map
   #(if (= opponent-stone (board %)) 1 0)
   idxes))

(defn get-current-scores3 [board idxes my-stone]
  (map
   #(condp = (board %)
      my-stone 2
      0 1
      0)
   idxes))

(defn gen-base-score2 [n]
  (loop [idxes (concat
                (range 0 (* n n) (inc n))
                (range (dec n) (dec (* n n)) (dec n)))

         score (vec (repeat (* n n) 0))]

    (if (empty? idxes)
      score
      (recur
       (rest idxes)
       (update score (first idxes) inc)))))

;;=================

;; 新ルール「一定時間経過すると、石が消滅する」を導入してみる

(defn update-lives [lives]
  (vec
   (map #(if (zero? %) 0 (dec %)) lives)))

(defn update-board [board lives num]
  (loop [idx 0
         l lives
         b board]

    (if (empty? l)
      b
      (recur
       (inc idx)
       (rest l)
       (assoc b idx (if (= num (first l)) 0 (b idx)))
       ))))

(defn update-board2 [board lives fnc]
  (loop [idx 0
         l lives
         b board]

    (if (empty? l)
      b
      (recur
       (inc idx)
       (rest l)
       (assoc b idx (if (fnc (first l) (b idx)) 0 (b idx)))
       ))))

(defn get-board-child2 [board lives life-max turn]
  (loop [idxes (canPutIdxes board)
         result '()]
    (if (empty? idxes)
      result
      (recur
       (rest idxes)
       (conj result {:idx (first idxes)
                     :board (assoc board (first idxes) turn)
                     ;; 改修箇所
                     :lives (assoc lives (first idxes) life-max)
                     }))
      )))

(defn search-vanishing-idx [board lives turn]
  (loop [idx 0
         l lives]

    (if (and
         ;; life:2 でないと引っ掛からない
         (= 2 (first l))
         (= (board idx) turn))
      idx
      (if (empty? l)
        nil
        (recur (inc idx) (rest l))))
    ))

(defn update-l [list-idx pattern]
  (loop [idxes list-idx
         rslt pattern]

    (if (empty? idxes)
      rslt
      (if (nil? (first idxes))
        '()

        (recur
         (rest idxes)
         (filter
          (fn [l] (some #(= (first idxes) %) l))
          rslt))
         ))))

(defn has-reach? [size board turn lines]
  (let [scores
        (map
         #(apply +
                 (get-current-scores3 board % turn))
         lines)]
    (some
     ;; ポイント
     ;; [ 相手の石 ]＋[ 空白 ]＋( [ 自分の石 ]：n ）
     #(= (* 2 (- size 2)) %)
     scores)
    )
)

(defn get-position-scores5
  [lines base-score size board lives turn i]

  (let [
        ;; つぎに置く石（idx）と入れ替わりで消える life:1 の石
        ;; それをないものとして、それぞれの手のスコアを計算する
        board-next
        (update-board2
         board
         lives
         #(and (= %1 1) (= %2 turn)))

        ;; 相手のターン
        turn-next
        (get_turn_next turn)

        ;; 相手の消える石の idx を取得する
        vanishing-idx
        (search-vanishing-idx board lives turn-next)
        ]

    (for [position-info (get-lines-to-win2 lines board-next)
          :let [idx (position-info :idx)

                ;; 相手の王手をガードしたときのポイント
                guard-score
                (first
                 (sort >
                       (for [idxes (position-info :lines)]
                         (apply +
                                ;; 改修箇所
                                (get-guard-points2
                                 board
                                 idxes
                                 turn-next)))))

                ;; 取りうる手なかでの最高スコア
                situation-score
                (first
                 (sort >
                       (for [idxes (position-info :lines)]
                         (apply +
                                ;; 改修箇所
                                (get-current-scores3
                                 ;; life:1 の自石を除いた状態
                                 board-next
                                 idxes
                                 turn)))))

                ;; リーチになるか？
                lines-update
                (update-l [vanishing-idx idx] lines)

                reach-flg
                (has-reach? size board-next turn-next lines-update)
                ]

          :when (= idx i)]

      {:idx idx
       :score (+ (base-score idx)
                 ;; リーチポイント
                 ;; 相手の王手に対応したときより低いポイントを設定する
                 (if reach-flg (dec size) 0)

                 ;; 相手の王手に対応したら
                 ;; 自分の勝ちより低いポイントを設定する
                 (if (<= (dec size) guard-score) size 0)

                 ;; 自分が勝ちになる手なら
                 situation-score
                 (if (< (* 2 (dec size)) situation-score)
                   (inc size) 0))
       }
      )))

;; 遅延評価を導入したもの

(defn think6
  ([win-patterns board lives size turn]
   (think6 win-patterns board lives size turn -1 -1))

  ([win-patterns board lives size turn idx score]
   (let [life-max (- (* size size) (dec size))
         ;; 1. dec lives
         lives-new (update-lives lives)
         ;; 2. update board
         board-new
         (update-board2 board lives-new #(= %1 0))
         ]

     ;; 改修箇所
     (lazy-cat
      (list {:b board-new
             :t turn
             :i idx
             :s score
             :l lives-new})

      (map

       (fn [{:keys [board lives idx]}]
         (think6
          win-patterns
          board
          lives
          size
          (get_turn_next turn)
          idx

          (:score
           (first
            (get-position-scores5
             win-patterns
             (gen-base-score2 size)
             size
             board-new
             lives-new
             turn
             idx)))
          ))

       (get-board-child2 board-new lives-new life-max turn))
      ))))
