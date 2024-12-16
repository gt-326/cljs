(ns ver14.util)

(defmacro foo [img dir]
  `~(for [d dir]
      `(~'c/Shot. ~'ctx 0 0 ~img ~d)))

;; cljs.user=>
;; (macroexpand-1 '(util/foo "./img/viper.png" (1 2 3)))

;;((c/Shot. ctx 0 0 "./img/viper.png" 1)
;; (c/Shot. ctx 0 0 "./img/viper.png" 2)
;; (c/Shot. ctx 0 0 "./img/viper.png" 3))

(defmacro foo2 [img dir]
  `(fn []
     (list
      ~@(for [d dir]
          `(~'c/Shot. ~'ctx 0 0 ~img ~d)))))

;; cljs.user=>
;; (macroexpand-1 '(util/foo2 "./img/viper.png" (1 2 3)))

;; (clojure.core/fn []
;;   (clojure.core/list
;;    (c/Shot. ctx 0 0 "./img/viper.png" 1)
;;    (c/Shot. ctx 0 0 "./img/viper.png" 2)
;;    (c/Shot. ctx 0 0 "./img/viper.png" 3)))


(defmacro foo3 [img dir]
  `(repeatedly
    ~'maxShot
    (fn []
      (list
       ~@(for [d dir]
           `(~'c/Shot. ~'ctx 0 0 ~img ~d))))))

;; cljs.user=>
;; (macroexpand-1 '(util/foo3 "./img/viper.png" (1 2 3)))

;; (clojure.core/repeatedly
;;  maxShot
;;  (clojure.core/fn []
;;    (clojure.core/list
;;      (c/Shot. ctx 0 0 "./img/viper.png" 1)
;;      (c/Shot. ctx 0 0 "./img/viper.png" 2)
;;      (c/Shot. ctx 0 0 "./img/viper.png" 3))))

(defmacro foo4 [num img dir]
  `(repeatedly
    ~num
    (fn []
      (list
       ~@(for [d dir]
           `(~'c/Shot. ~'ctx 0 0 ~img ~d))))))

(defmacro foo5 [num fnc img dir]
  `(repeatedly
    ~num
    (fn []
      (list
       ~@(for [d dir]
           `(~fnc ~img ~d))))))

(defmacro foo6 [num fnc img dir]
  `(repeatedly
    ~num
    (fn []
      (list
       ~@(for [d dir]
           `(~fnc ~img ~d))))))

(defmacro myRepeat [cnt fnc params]
  `(repeatedly
    ~cnt
    (fn []
      (list
       ~@(for [p params]
           `(~fnc ~p))))))

(defmacro myRepeat2 [cnt fnc params]
  `(repeatedly
    ~cnt
    (fn []
      (list
       ~@(for [p params]
           `(~fnc '~p))))))

(defmacro foo3_2 [cnt img speed dir]
  `(repeatedly
    ~cnt
    (fn []
      (list
       ~@(for [d dir]
           `(~'c/Enemy. ~'ctx 0 0 ~img ~speed '~d))))))
