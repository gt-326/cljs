(require 'cljs.build.api)
(cljs.build.api/build "src" {:main 'life.core :output-to "out/main.js"})
;;(cljs.build.api/watch "src" {:main 'life.core :output-to "out/main.js"})

