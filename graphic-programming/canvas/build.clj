

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
;; │   └─ ver00/
;; │        ├─ canvas2d.cljs
;; │        └─ core.cljs
;; ├─ index.html
;; └─ build.clj
;;
;;====================================

;; $ java -cp "../cljs.jar:src" cljs.main --compile ver00.core --repl

;; (require 'ver00.core :reload)
;; (require 'ver00.canvas2d :reload)

;;====================================

(require '[cljs.build.api])

(cljs.build.api/build
 ;;cljs.build.api/watch
 "src"
 {:output-to "out/main.js"

  ;; chap00 の数字を適宜書き換えてビルドする
  :main 'ver04.core})
