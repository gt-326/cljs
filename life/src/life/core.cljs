;---
; Inspired by the Game of Life  that have gone before:
;
; Robert Spatz's Game of Life - JavaScript: https://codepen.io/RBSpatz/pen/rLyNLb
;
;---

(ns life.core
  (:require [clojure.string :as str]))

(def rows 30)
(def cols 30)
(def reproductionTime 200)

(def states ["dead" "alive" "born" "add"]) 
(def state-dead 0)
(def state-born 1)
(def state-alive 2)
(def state-add 3)

(def sep-char "-")

;;=== Utility fnc  ====================

(defn getHtmlElementById [id]
　(.getElementById js/document id))

(defn getHtmlElementsByClassName [name]
　(.getElementsByClassName js/document name))

(defn createHtmlElementAsChild [obj type]
　(.appendChild obj (.createElement js/document type)))

(defn isAttributeClassDead? [obj]
  (= (.getAttribute obj "class") (get states state-dead)))

(defn setAttribute [obj param val]
　(.setAttribute obj param val))

(defn setObjectAttributeClass [obj idx]
　(setAttribute obj "class" (get states idx)))

(defn setCellAttributeClass [i j idx]
　(setObjectAttributeClass (getHtmlElementById (str i sep-char j)) idx))

;;=== Button event etc ================

;; RULES
;; Any live cell with fewer than two live neighbours dies, as if caused by under-population.
;; Any live cell with two or three live neighbours lives on to the next generation.
;; Any live cell with more than three live neighbours dies, as if by overcrowding.
;; Any dead cell with exactly three live neighbours becomes a live cell, as if by reproduction.

(defn cntNeighbors [g row col m_row m_col]
　(let [neighbors [[-1 -1] [0 -1] [1 -1] [1 0] [1 1] [0 1] [-1 1] [-1 0]]]
　　(count
　　　(remove zero? (for [[i j] neighbors :let [h (+ row i) w (+ col j)]]
　　　　(if (or (< h 0) (< w 0) (>= h m_row) (>= w m_col))
　　　　　state-dead
　　　　　(get-in @g [h w])))))))

(defn updateView [g m n]
　(letfn [(fnc []
　　(for [i (range 0 m)]
　　　(for [j (range 0 n)]
　　　　(do
　　　　　(setCellAttributeClass i j (get-in @g [i j]))
　　　　　(str i sep-char j)))))]

　　;; Does anyone know how to display result without using .log ?
　　(.log js/console (str "updateView:" (fnc)))))

(defn computeNextGen [g m n]
　(let [state-next
　　(vec (for [i (range 0 m)]
　　　(vec (for [j (range 0 n) :let [numNeighbors (cntNeighbors g i j m n)]]
　　　　(cond
　　　　　(= numNeighbors 2) (if (= (get-in @g [i j]) state-dead) state-dead state-alive)
　　　　　(= numNeighbors 3) state-born
　　　　　:else state-dead)))))]
　　(do
　　　;; copy state-next to grid
　　　(reset! g state-next)
　　　;; copy all 1 values to "live" in the table
　　　(updateView g m n))))

(defn play [playing timer g m n]
　(if @playing
　　(do
　　　(computeNextGen g m n)
　　　(reset! timer (js/setTimeout (fn [] (play playing timer g m n)) reproductionTime)))))

;; start game
(defn startButtonHandler [playing timer g obj m n]
　(if @playing
　　(do
　　　(.log js/console "Pause the game")
　　　(reset! playing false)
　　　(set! (.-innerHTML obj) "Restart")
　　　(.clearTimeout js/window @timer))
　　(do
　　　(.log js/console "Continue the game")
　　　(reset! playing true)
　　　(set! (.-innerHTML obj) "Stop")
　　　(play playing timer g m n))))

;; clear the grid
(defn clearButtonHandler [playing timer g init m n]
　(do
　　(.log js/console "Clear the game: stop playing, clear the grid")
　　(reset! playing false)
　　(set! (.-innerHTML (getHtmlElementById "start")) "Start")
　　(.clearTimeout js/window @timer)

　　(letfn [(fnc []
　　　(for [i (range 0 m)]
　　　　(for [j (range 0 n)]
　　　　　(setCellAttributeClass i j state-dead))))]

　　　;; Does anyone know how to display result without using .log ?
　　　(.log js/console (str "grid_dead:" (fnc))))

　　　;; init grid
　　　(reset! g init)))

;; calc random value
(defn randomButtonHandler [playing timer g init m n]
　(if (not @playing)
　　(do
　　　;; clear Grid
　　　(clearButtonHandler playing timer g init m n)

　　　(let [state-new
　　　　(vec (for [i (range 0 m)]
　　　　　(vec (for [j (range 0 n)] (.round js/Math (js/Math.random))))))]
　　　　　　(do
　　　　　　　;; init grid randomValue
　　　　　　　(reset! g state-new)

　　　　　　　(letfn [(fnc [state-array]
　　　　　　　　(for [i (range 0 m)]
　　　　　　　　　(for [j (range 0 n) :when (= (get-in state-array [i j]) state-born)]
　　　　　　　　　　(setCellAttributeClass i j state-alive))))]

　　　　　　　　;; Does anyone know how to display result without using .log ?
　　　　　　　　;; NG (fnc state-new)
　　　　　　　　;; NG (.log js/console (count (fnc state-new)))
　　　　　　　　(.log js/console (str "state:" (fnc state-new)))))))))

;;=== Grid cell click event ===========

(defn cellClickHandler [i j g obj]
　(if (isAttributeClassDead? obj)
　　(do
　　　(reset! g (assoc-in @g [i j] state-alive))
　　　(setObjectAttributeClass obj state-add))
　　(do
　　　(reset! g (assoc-in @g [i j] state-dead))
　　　(setObjectAttributeClass obj state-dead))))

;;=== PageLoad etc ====================

(defn createGc [g m n]
　(let [gc (getHtmlElementById "gridContainer")]
　　(if gc
　　　(let [tbl (createHtmlElementAsChild gc "table")]
　　　　(letfn [(fnc []
　　　　　(for [i (range 0 m) :let [tr (createHtmlElementAsChild tbl "tr")]]
　　　　　　(for [j (range 0 n) :let [td (createHtmlElementAsChild tr "td")]]
　　　　　　　(do
　　　　　　　　(setAttribute td "id" (str i sep-char j))
　　　　　　　　(setObjectAttributeClass td state-dead)
　　　　　　　　(set! (.-onclick td) (fn [] (cellClickHandler i j g td)))))))]

　　　;; Does anyone know how to display result except .log ?
　　　(.log js/console (str "tbl:" (fnc))))))))

(defn setupControlButtons [g init m n]
　(let [ timer (atom nil)
　　　　　flg (atom false)
　　　　　btn1 (getHtmlElementById "start")
　　　　　btn2 (getHtmlElementById "clear")
　　　　　btn3 (getHtmlElementById "random") ]
　　(do
　　　(set! (.-onclick btn1) (fn [] (startButtonHandler flg timer g btn1 m n)))
　　　(set! (.-onclick btn2) (fn [] (clearButtonHandler flg timer g init m n)))
　　　(set! (.-onclick btn3) (fn [] (randomButtonHandler flg timer g init m n))))))

;; Initialize
(defn initialize [r c]
　(let [ init
          (vec (for [i (range 0 r)]
　　　　　　　(vec (for [j (range 0 c)] state-dead))))
　　　　　g (atom init) ]
　　(do
　　　(createGc g r c)
　　　(setupControlButtons g init r c))))

;; Start everything
(set! (.-onload js/window) (fn [] (initialize rows cols)))
