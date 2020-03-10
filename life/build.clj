;;====================================
;;
;; life/
;; ├─ out/
;; ├─ src/
;; │　　└─ life/
;; │　　　　└─ core.cljs
;; ├─ index.html
;; ├─ build.clj
;; └─ cljs.jar  [ https://github.com/clojure/clojurescript/releases/download/r1.10.520/cljs.jar ]
;;
;;====================================

(require 'cljs.build.api)
(cljs.build.api/build "src" {:main 'life.core :output-to "out/main.js"})
;;(cljs.build.api/watch "src" {:main 'life.core :output-to "out/main.js"})
