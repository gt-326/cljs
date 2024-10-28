(ns marubatsu.util)

(defmacro nested-vector [size fnc]
  `(vec (for [~'idx (range 0 ~size)] (~fnc ~'idx))))

(defmacro nested-vector2 [size gc fnc]
  `(let [tbl# (~'createHtmlElementAsChild ~gc "table")]
     (vec
      (for [~'i (range 0 ~size)
            :let [~'tr (~'createHtmlElementAsChild tbl# "tr")]]
        (vec
         (for [~'j (range 0 ~size)
               :let [~'td (~'createHtmlElementAsChild ~'tr "td")]]
           (~fnc ~'i ~'j ~'td)
           ))))))

(defmacro console-log
  ([title obj size fnc]
   `(console-log
     (str ~title ":" (nested-vector2 ~size ~obj ~fnc))))

  ([title size fnc]
   `(console-log (str ~title ":" (nested-vector ~size ~fnc))))

  ([msg] `(.log js/console ~msg)))
