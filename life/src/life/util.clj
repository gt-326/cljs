(ns life.util)

(defmacro nested-vector [r c fnc]
　`(vec (for [~'i (range 0 ~r)]
　　(vec (for [~'j (range 0 ~c)] (~fnc ~'i ~'j ~r ~c))))))
