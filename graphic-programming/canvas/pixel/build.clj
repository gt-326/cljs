

;;====================================
;;
;; cljs.jar
;; [ https://github.com/clojure/clojurescript/releases/download/r1.11.132/cljs.jar ]
;;
;; pixel/
;; ├─ css/
;; │   └─ style.css
;; ├─ img/
;; │   ├─ sample.jpg
;; │   └─ sample2.jpg
;; ├─ out/
;; ├─ src/
;; │   └─ ver00/
;; │        └─ core.cljs
;; ├─ index.html
;; └─ build.clj
;;
;;====================================

;; $ java -cp "./cljs.jar:src" cljs.main --compile ver00.core --repl

;; (require 'ver00.core :reload)

;;====================================

(require '[cljs.build.api])

(cljs.build.api/build
 ;;cljs.build.api/watch
 "src"
 {:output-to "out/main.js"

  ;; chap00 の数字を適宜書き換えてビルドする
  :main 'ver01.core})
