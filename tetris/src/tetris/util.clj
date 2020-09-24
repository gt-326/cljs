(ns tetris.util)

(defmacro nested-vector [fnc]
　`(vec (for [~'i (range 0 ~'ROWS)]
　　(vec (for [~'j (range 0 ~'COLS)] (~fnc ~'i ~'j))))))

(defmacro nested-vector2 [gc fnc]
　`(let [tbl# (~'createHtmlElementAsChild ~gc "table")]
　　(vec (for [~'i (range 0 ~'ROWS) :let [~'tr (~'createHtmlElementAsChild tbl# "tr")]]
　　　(vec (for [~'j (range 0 ~'COLS) :let [~'td (~'createHtmlElementAsChild ~'tr "td")]]
　　　　(~fnc ~'i ~'j ~'td)))))))

(defmacro console-log
  ([title obj fnc] `(console-log (str ~title ":" (nested-vector2 ~obj ~fnc))))
  ([title fnc] `(console-log (str ~title ":" (nested-vector ~fnc))))
  ([msg] `(.log js/console ~msg)))

(defmacro foo2_ [state target val]
　`(.log js/console
　　(str "state:" (for [~'idx ~target :let [~'state_new (update-in (deref ~state) ~'idx (fn [] ~val))]]
　　　(reset! ~state ~'state_new)))))
