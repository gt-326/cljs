

;;====================================
;;
;; cljs.jar
;; [ https://github.com/clojure/clojurescript/releases/download/r1.11.132/cljs.jar ]
;;
;; canvas/
;; ├─ css/
;; │   └─ style.css
;; ├─ img/
;; │   └─ viper.png
;; ├─ out/
;; ├─ src/
;; │   └─ chap00/
;; │        ├─ canvas2d.cljs
;; │        └─ core.cljs
;; ├─ index.html
;; └─ build.clj
;;
;;====================================

;; $ java -cp "../cljs.jar:src" cljs.main --compile chap00.core --repl

;; (require 'chap00.core :reload)
;; (require 'chap00.canvas2d :reload)

;;====================================

(require '[cljs.build.api])

(cljs.build.api/build
 ;;cljs.build.api/watch
 "src"
 {:output-to "out/main.js"

  ;; chap00 の数字を適宜書き換えてビルドする
  :main 'chap00.core})
