(ns ver13.util
  (:require-macros [ver13.util]))

(defn degToRed [degrees]
  (/ (* degrees Math.PI) 180))

(defn genVectorFromDegrees [degrees]
  (let [radian (degToRed degrees)]
    (list
     (Math.cos radian)
     (Math.sin radian))))
