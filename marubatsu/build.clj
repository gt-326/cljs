

;;====================================
;;
;; marubatsu/
;; ├─ out/
;; ├─ src/
;; │   └─ marubatsu/
;; │        ├─ util.cljs
;; │        ├─ util.clj
;; │        ├─ computer.cljs
;; │        └─ core.cljs
;; ├─ index.css
;; ├─ index.html
;; ├─ build.clj
;; ├─ core.async-1.5.648.jar
;; └─ cljs.jar  [ https://github.com/clojure/clojurescript/releases/download/r1.11.132/cljs.jar ]
;;
;;====================================

;; $ java -cp "cljs.jar:core.async-1.5.648.jar:src" cljs.main --compile marubatsu.core --repl

;;====================================

(require '[cljs.build.api])

(cljs.build.api/build
 ;;cljs.build.api/watch
 "src"
 {:main 'marubatsu.core
  :output-to "out/main.js"})
