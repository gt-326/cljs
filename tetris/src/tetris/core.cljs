;;---
;; Inspired by the Game of Life  that have gone before:
;;
;; Robert Spatz's Game of Life - JavaScript: https://codepen.io/RBSpatz/pen/rLyNLb
;;---

(ns tetris.core
  (:require [clojure.string :as str] [tetris.util :as util]))

;;=== Const  ====================

(def LINES 5)

(def ROWS 30)
(def COLS 10)
(def RE-PRODUCTION-TIME 500)

(def KEY-CODE-SPACE 32)
(def KEY-CODE-LEFT 37)
(def KEY-CODE-UP 38)
(def KEY-CODE-RIGHT 39)
(def KEY-CODE-DOWN 40)

(def BLOCK-BORN 1)
(def BLOCK-ALIVE 2)

(def SEP-CHAR "-")

(def STATE-DEAD 0)
(def STATE-I 1)
(def STATE-O 2)
(def STATE-S 3)
(def STATE-Z 4)
(def STATE-L 5)
(def STATE-J 6)
(def STATE-T 7)

(def CELL-TYPES [ "DEAD" "I" "O" "S" "Z" "L" "J" "T" ])

(def BLOCKS
　{ "I" [ [[1 -1] [1 0] [1 1] [1 2]]
　　　　　　[[-1 1] [0 1] [1 1] [2 1]] ]

　　"O" [ [[0 0] [0 1] [1 0] [1 1]] ]

　　"S" [ [[0 1] [0 2] [1 0] [1 1]]
　　　　　　[[0 -1] [1 -1] [1 0] [2 0]] ]

　　"Z" [ [[0 -1] [0 0] [1 0] [1 1]]
　　　　　　[[0 1] [1 0] [1 1] [2 0]] ]

　　"L" [ [[1 0] [2 0] [2 1] [2 2]]
　　　　　　[[1 1] [1 2] [0 2] [-1 2]]
　　　　　　[[1 0] [0 0] [0 -1] [0 -2]]
　　　　　　[[0 0] [1 0] [2 0] [0 1]] ]

　　"J" [ [[1 1] [2 -1] [2 0] [2 1]]
　　　　　　[[-1 -2] [0 -2] [1 -2] [1 -1]]
　　　　　　[[0 0] [1 0] [0 1] [0 2]]
　　　　　　[[0 1] [0 2] [1 2] [2 2]] ]

　　"T" [ [[1 1] [2 0] [2 1] [2 2]]
　　　　　　[ [0 0] [0 1] [-1 1] [1 1]]
　　　　　　[ [1 -1] [0 -1] [0 0] [0 -2]]
　　　　　　[ [0 0] [1 0] [2 0] [1 1]] ] } )

(def EMPTY_LINE [0 0 0 0 0 0 0 0 0 0])

;;=== key events ======================

(defn getOffset [keyCode]
　(cond
　　(= KEY-CODE-SPACE keyCode) [ 1  0]
　　(= KEY-CODE-UP keyCode)    [-1  0]
　　(= KEY-CODE-LEFT keyCode)  [ 0 -1]
　　(= KEY-CODE-RIGHT keyCode) [ 0  1]
　　(= KEY-CODE-DOWN keyCode)  [ 1  0]
　　:else [0 0]))

;;=== Utility fnc  ====================

(defn getHtmlElementById [id]
  　(.getElementById js/document id))

(defn getHtmlElementsByClassName [name]
　(.getElementsByClassName js/document name))

(defn createHtmlElementAsChild [obj type]
　(.appendChild obj (.createElement js/document type)))

(defn isAttributeClassDead? [obj]
　(= (.getAttribute obj "class") (get CELL-TYPES STATE-DEAD)))

(defn setAttribute [obj param val]
　(.setAttribute obj param val))

(defn setObjectAttributeClass [obj idx]
　(setAttribute obj "class" (get CELL-TYPES idx)))

(defn setCellAttributeClass [i j idx]
　(setObjectAttributeClass (getHtmlElementById (str i SEP-CHAR j)) idx))

;;=== update state ====================

;; Does anyone know how to display new status without using .log ?

(defn updateBlockState [grid blockCells state]
　(.log js/console (str "updateBlockState:"
　　(for [cell_idx blockCells :let [grid_new (update-in @grid cell_idx (fn [] state))]]
　　　(reset! grid grid_new)))))

(defn updateLineState [g_state idxes line]
　(let [grid (atom (deref g_state)) ]
　　(.log js/console (str "updateLineState:"
　　　(for [idx idxes :let [grid_new (assoc-in @grid [idx] line)]]
　　　(reset! grid grid_new))))
　　grid))

;;=== refresh grid & view =============

(defn initGridView [log_header state]
　(util/console-log log_header
　　(fn [i j] (do (setCellAttributeClass i j state) state))))

(defn refreshGridView [log_header state]
　(util/console-log log_header
　　(fn [i j]
　　　(let [cell_state (get-in state [i j])]
　　　　(do
　　　　　(setCellAttributeClass i j cell_state)
　　　　　cell_state)))))

(defn updateGridState [g_state g_color b_current b_next b_type status_next]
　(do
　　;; kill current position
　　(updateBlockState g_state b_current STATE-DEAD)
　　(updateBlockState g_color b_current STATE-DEAD)
　　;; fill next position
　　(updateBlockState g_state b_next status_next)
　　(updateBlockState g_color b_next b_type)
　　;; update view
　　(refreshGridView "updateGridState" @g_color)))

;;========================

(defn getTargetIdxes [g_state state]
　(vec (remove nil? (reduce into (util/nested-vector (fn [i j] (if (= state (get-in @g_state [i j])) [i j])))))))

(defn ableToMoveTargetBlock? [g_state block]
　(every? true? (for [[i j] block] (and (< -1 i ROWS) (< -1 j COLS) (< (get-in @g_state [i j]) BLOCK-ALIVE)))))

(defn ableToFillNewBlock? [g_state block]
　(every? true? (for [[i j] block] (= (get-in @g_state [i j]) STATE-DEAD))))

;;========================

(defn fillNewBlock [g_state g_color target-type playing]
　(let [ target-temp (set (for [[i j] (get (get BLOCKS (get CELL-TYPES target-type)) 0) ] [(+ i) (+ 4 j)]))
　　　　　gameover? (not (ableToFillNewBlock? g_state target-temp)) ]
　　;; states
　　(reset! g_state
　　　(util/nested-vector
　　　　(fn [i j] (if (contains? target-temp [i j]) BLOCK-BORN (get-in @g_state [i j])))))
　　;; colors
　　(reset! g_color
　　　(util/nested-vector
　　　　(fn [i j] (if (contains? target-temp [i j]) target-type (get-in @g_color [i j])))))
　　;; update view
　　(refreshGridView "fillNewBlock" @g_color)

　　(if gameover?
　　　(do
　　　　(reset! playing false)
  　　　(js/alert "lose!")))))

(defn calcGridStateNext [state x_idxes]
　(into
　　;; fill line(s) into grid upper
　　(vec (take (count x_idxes) (repeat EMPTY_LINE)))
　　;; del line(s) from grid lower
　　(vec
　　　(for [idx (range ROWS) :let [line (get @state idx)] :when (not (contains? (set x_idxes) idx)) ] line))))

(defn blink [timer colors]
　(if (not (empty? colors))
　　(do
　　　(refreshGridView "blink" (first colors))
　　　(reset! timer (js/setTimeout #(blink timer (rest colors)) RE-PRODUCTION-TIME)))))

(defn flushLine [b_current b_next [g_state g_color cnt_rolling timer playing cnt_flushed flushing]]
　(let [ b_adjust (vec (map #(vec (map + (getOffset KEY-CODE-UP) %)) b_next))
　　　　　b_type (get-in @g_color (vec (first b_current))) ]

　　(updateGridState g_state g_color b_current b_adjust b_type BLOCK-ALIVE)

　　(let [ x_idxes (vec (set (for [[i j] b_adjust] i)))
　　　　　　x_idxes_new (for [x x_idxes :when (every? #(= BLOCK-ALIVE %) (get @g_state x))] x)]

　　　;; able to flush line(s) ?
　　　(if (not (empty? x_idxes_new))
　　　　;; Does anyone know how to manage an atom 'g_color' state change timing and 'blink' loop ?
　　　　(let [ color_alt (updateLineState g_color x_idxes_new EMPTY_LINE)
　　　　　　　　color_new (calcGridStateNext g_color x_idxes_new) ]

　　　　　(blink timer [@g_color @color_alt @g_color @color_alt  color_new])
　　　　　;; refresh atoms
　　　　　(reset! flushing true)
　　　　　(reset! g_color color_new)
　　　　　(reset! g_state (calcGridStateNext g_state x_idxes_new))
　　　　　(reset! cnt_flushed (+ @cnt_flushed (count x_idxes_new)))))

　　　;; game clear?
　　　(if (< @cnt_flushed LINES)
　　　　(do
　　　　　;; create new block
　　　　　(reset! cnt_rolling 0)
　　　　　(fillNewBlock g_state g_color (inc (rand-int STATE-T)) playing))))))

(defn freeFall [[g_state g_color cnt_rolling timer playing cnt_flushed flushing]]
　(loop [ dir (getOffset KEY-CODE-DOWN)
　　　　　　b_current (getTargetIdxes g_state BLOCK-BORN)
　　　　　　b_next (vec (map #(vec (map + dir %)) b_current))]

　　(if (ableToMoveTargetBlock? g_state b_next)
　　　;; falling　　　
　　　(recur dir b_current (vec (map #(vec (map + dir %)) b_next)))
　　　;; landing
　　　(flushLine b_current b_next [g_state g_color cnt_rolling timer playing cnt_flushed flushing]))))

(defn move [dir [g_state g_color cnt_rolling timer playing cnt_flushed flushing]]
　(let [ b_current (getTargetIdxes g_state BLOCK-BORN)
　　　　　b_next (vec (map #(vec (map + dir %)) b_current)) ]

　　(if (ableToMoveTargetBlock? g_state b_next)
　　　;; forwarding one step
　　　(updateGridState g_state g_color b_current b_next (get-in @g_color (vec (first b_current))) BLOCK-BORN)

　　　(if (= dir (getOffset KEY-CODE-DOWN))
　　　　(flushLine b_current b_next [g_state g_color cnt_rolling timer playing cnt_flushed flushing])))))

(defn rolling [[g_state g_color cnt_rolling]]
　(let [ b_current (getTargetIdxes g_state BLOCK-BORN)
　　　　　b_type (get-in @g_color (vec (first b_current)))
　　　　　b_forms (get BLOCKS (get CELL-TYPES b_type))
　　　　　b_forms_idx (rem (inc @cnt_rolling) (count b_forms))
　　　　　b_next (vec (map (fn [idx] (vec (map + (first b_current) idx))) (get b_forms b_forms_idx))) ]

　　(if (ableToMoveTargetBlock? g_state b_next)
　　　(do
　　　　;; create new block
　　　　(reset! cnt_rolling b_forms_idx)
　　　　(updateGridState g_state g_color b_current b_next b_type BLOCK-BORN)))))

(defn playgame [g_state g_color cnt_rolling timer playing cnt_flushed flushing]
　(if @playing
　　(if @flushing
　　　(do
　　　　;; waiting for flush line(s) animation ends
　　　　(reset! flushing false)　
　　　　(reset! timer
　　　　　(js/setTimeout #(playgame g_state g_color cnt_rolling timer playing cnt_flushed flushing)
　　　　　(* RE-PRODUCTION-TIME 4))))

　　　(if (>= @cnt_flushed LINES)
　　　　(do
　　　　　(reset! playing false)
　　　　　(js/alert "Win"))

　　　　(do
　　　　　(move (getOffset KEY-CODE-DOWN) [g_state g_color cnt_rolling timer playing cnt_flushed flushing])
　　　　　(reset! timer
　　　　　　(js/setTimeout #(playgame g_state g_color cnt_rolling timer playing cnt_flushed flushing)
　　　　　　RE-PRODUCTION-TIME)))))))

;;=== Event Handler ===================

;; start game
(defn startButtonHandler [obj [g_state g_color cnt_rolling timer playing cnt_flushed flushing]]
　(let [flg @playing]
　　(util/console-log (if flg "Pause the game" "Continue the game"))
　　(set! (.-innerHTML obj) (if flg "Restart" "Stop"))
　　(reset! playing (not flg))

　　(if flg
　　　(do
　　　　(initGridView "hideGrid" STATE-DEAD)
　　　　(.clearTimeout js/window @timer))

　　　(do
　　　　(if (empty? (getTargetIdxes g_state BLOCK-BORN))
　　　　　(do
　　　　　　;; create new block
　　　　　　(reset! cnt_rolling 0)
　　　　　　(fillNewBlock g_state g_color (inc (rand-int STATE-T)) playing)))

　　　　(playgame g_state g_color cnt_rolling timer playing cnt_flushed flushing)))))

;; clear the grid
(defn clearButtonHandler [[g_state g_color cnt_rolling timer playing cnt_flushed flushing]]
　(let [init (util/nested-vector (fn [] STATE-DEAD))]
　　(util/console-log "Clear the game: stop playing, clear the grid")
　　(reset! playing false)
　　(set! (.-innerHTML (getHtmlElementById "start")) "Start")
　　(.clearTimeout js/window @timer)

　　;; init atoms
　　(reset! g_state init)
　　(reset! g_color init)
　　(reset! cnt_rolling 0)
　　(reset! cnt_flushed 0)
　　(reset! flushing false)
　　(initGridView "initGrid" STATE-DEAD)))

;; keyevent handler
(defn keyEventHandler [btn playing init_params]
　(if @playing
　　(cond
　　　(= KEY-CODE-SPACE (int btn)) (freeFall init_params)
　　　(= KEY-CODE-UP (int btn)) (rolling init_params)
　　　:else (move (getOffset btn) init_params))))

;;=== PageLoad etc ====================

(defn createGc []
　(let [gc (getHtmlElementById "gridContainer")]
　　(if gc
　　　;; Does anyone know how to display new status without using .log ?
　　　(util/console-log "html_tbl" gc
　　　　(fn [i j obj] (setAttribute obj "id" (str i SEP-CHAR j)))))))

(defn setupControls [playing init_params]
　(let [ btn1 (getHtmlElementById "start")
　　　　　btn2 (getHtmlElementById "clear") ]
　　(set! (.-onclick btn1) #(startButtonHandler btn1 init_params))
　　(set! (.-onclick btn2) #(clearButtonHandler init_params))
　　(set! (.-onkeydown js/document) (fn [e] (keyEventHandler (.-keyCode e) playing init_params)))))

;; Initialize
(defn initialize []
　(let [ init (util/nested-vector (fn [] STATE-DEAD))
　　　　　grid_state (atom init)
　　　　　grid_color (atom init)
　　　　　cnt_rolling (atom 0)
　　　　　timer (atom nil)
　　　　　playing (atom false)
　　　　　cnt_flushed (atom 0)
　　　　　flushing (atom false) ]
　　(createGc)
　　(setupControls playing [grid_state grid_color cnt_rolling timer playing cnt_flushed flushing])))

;; Start everything
(set! (.-onload js/window) #(initialize))
