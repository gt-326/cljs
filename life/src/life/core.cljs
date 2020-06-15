;;---
;; Inspired by the Game of Life  that have gone before:
;;
;; Robert Spatz's Game of Life - JavaScript: https://codepen.io/RBSpatz/pen/rLyNLb
;;
;;---

(ns life.core
  (:require [clojure.string :as str] [life.util :as util]))

;;=== Const  ====================

(def ROWS 30)
(def COLS 30)
(def RE-PRODUCTION-TIME 200)

(def STATES ["dead" "alive" "born" "add"]) 
(def STATE-DEAD 0)
(def STATE-BORN 1)
(def STATE-ALIVE 2)
(def STATE-ADD 3)

(def SEP-CHAR "-")

;;=== Utility fnc  ====================

(defn getHtmlElementById [id]
　(.getElementById js/document id))

(defn getHtmlElementsByClassName [name]
　(.getElementsByClassName js/document name))

(defn createHtmlElementAsChild [obj type]
　(.appendChild obj (.createElement js/document type)))

(defn isAttributeClassDead? [obj]
　(= (.getAttribute obj "class") (get STATES STATE-DEAD)))

(defn setAttribute [obj param val]
　(.setAttribute obj param val))

(defn setObjectAttributeClass [obj idx]
　(setAttribute obj "class" (get STATES idx)))

(defn setCellAttributeClass [i j idx]
　(setObjectAttributeClass (getHtmlElementById (str i SEP-CHAR j)) idx))

;;=== Button event etc ================

;; RULES
;; Any live cell with fewer than two live neighbours dies, as if caused by under-population.
;; Any live cell with two or three live neighbours lives on to the next generation.
;; Any live cell with more than three live neighbours dies, as if by overcrowding.
;; Any dead cell with exactly three live neighbours becomes a live cell, as if by reproduction.

(defn cntNeighbors [g row col]
　(let [neighbors [[-1 -1] [0 -1] [1 -1] [1 0] [1 1] [0 1] [-1 1] [-1 0]]]
　　(count (remove zero?
　　　(for [[i j] neighbors :let [h (+ row i) w (+ col j)]]
　　　　(if (and (< 0 h ROWS) (< 0 w COLS))
　　　　　(get-in @g [h w])
　　　　　STATE-DEAD))))))

(defn updateView [g]
　;; Does anyone know how to display new status without using .log ?
  (util/console-log "updateView"
　　(fn [i j]
　　　(let [stat (get-in @g [i j])]
　　　　(do
　　　　　(setCellAttributeClass i j stat)
　　　　　stat)))))

(defn computeNextGen [g]
　(do
　　;; copy state-next to grid
　　(reset! g (util/nested-vector
　　　(fn [i j]
　　　　(case (cntNeighbors g i j)
　　　　　2 (if (= (get-in @g [i j]) STATE-DEAD) STATE-DEAD STATE-ALIVE)
　　　　　3 STATE-BORN
　　　　　STATE-DEAD))))

　　;; copy all 1 values to "live" in the table
　　(updateView g)))

(defn play [playing timer g]
　(if @playing
　　(do
　　　(computeNextGen g)
　　　(reset! timer (js/setTimeout #(play playing timer g) RE-PRODUCTION-TIME)))))

;; start game
(defn startButtonHandler [playing timer g obj]
　(let [flg @playing]
　　(do
　　　(util/console-log (if flg "Pause the game" "Continue the game"))
　　　(reset! playing (not flg))
　　　(set! (.-innerHTML obj) (if flg "Restart" "Stop"))
　　　(if flg (.clearTimeout js/window @timer) (play playing timer g)))))

;; clear the grid
(defn clearButtonHandler [playing timer g init]
　(do
　　(util/console-log "Clear the game: stop playing, clear the grid")
　　(reset! playing false)
　　(set! (.-innerHTML (getHtmlElementById "start")) "Start")
　　(.clearTimeout js/window @timer)

　　;; init grid
　　(reset! g init)

　　;; Does anyone know how to display new status without using .log ?
　　(util/console-log "grid_dead"
　　　(fn [i j] (do (setCellAttributeClass i j STATE-DEAD) STATE-DEAD)))))

;; calc random value
(defn randomButtonHandler [playing timer g init]
　(if (not @playing)
　　(do
　　　;; clear Grid
　　　(clearButtonHandler playing timer g init)

　　　;; init grid randomValue
　　　(reset! g (util/nested-vector #(.round js/Math (js/Math.random))))

　　　;; Does anyone know how to display new status without using .log ?
　　　(util/console-log "state"
　　　　(fn [i j]
　　　　　(let [stat (get-in @g [i j])]
　　　　　　(do
　　　　　　　(if (= stat STATE-BORN) (setCellAttributeClass i j STATE-ALIVE))
　　　　　　　stat)))))))

;;=== Grid cell click event ===========

(defn cellClickHandler [idx g obj]
  (let [flg (isAttributeClassDead? obj)]
　　(do
　　　(reset! g (assoc-in @g idx (if flg STATE-ALIVE STATE-DEAD)))
　　　(setObjectAttributeClass obj (if flg STATE-ADD STATE-DEAD)))))

;;=== PageLoad etc ====================

(defn createGc [g]
　(let [gc (getHtmlElementById "gridContainer")]
　　(if gc
　　　;; Does anyone know how to display new status without using .log ?
　　　(util/console-log "html_tbl" gc 
　　　　(fn [i j obj]
　　　　　(do
　　　　　　(setAttribute obj "id" (str i SEP-CHAR j))
　　　　　　(setObjectAttributeClass obj STATE-DEAD)
　　　　　　(set! (.-onclick obj) #(cellClickHandler [i j] g obj))))))))

(defn setupControlButtons [g init]
　(let [ flg (atom false)
         timer (atom nil)
　　　　　btn1 (getHtmlElementById "start")
　　　　　btn2 (getHtmlElementById "clear")
　　　　　btn3 (getHtmlElementById "random") ]
　　(do
　　　(set! (.-onclick btn1) #(startButtonHandler flg timer g btn1))
　　　(set! (.-onclick btn2) #(clearButtonHandler flg timer g init))
　　　(set! (.-onclick btn3) #(randomButtonHandler flg timer g init)))))

;; Initialize
(defn initialize []
　(let [init (util/nested-vector (fn [] STATE-DEAD)) g (atom init)]
　　(do
　　　(createGc g)
　　　(setupControlButtons g init))))

;; Start everything
(set! (.-onload js/window) #(initialize))
