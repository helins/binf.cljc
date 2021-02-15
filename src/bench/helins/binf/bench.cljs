(ns helins.binf.bench

  "Benchmarking a few things."

  {:author "Adam Helinski"}

  (:require [helins.binf    :as binf]
            [taoensso.tufte :as tufte]))


;;;;;;;;;;


(tufte/add-basic-println-handler! {})


;;;;;;;;;;


(def n-iter
     1e2)



(def size
     (* 1024
        500
        ;1000
        ;500
        ))



;(def buffer
;     (binf/buffer size))
;
;
;
;(def data-view
;     (js/DataView. buffer))
;
;
;
;(def binf-view
;     (binf/view buffer))


;;;;;;;;;;


(defn b

  []

  ;(rand-int 128)
  42
  )



(defn main

  []

  (tufte/profile

    {}

    (do

      (tufte/p
        :data-view
        (dotimes [_i n-iter]
          (let [data-view (js/DataView. (binf/buffer size))]
            (dotimes [j size]
              (.setUint8 data-view
                         j
                         (b)
                         true)))))

      (tufte/p
        :binf-view
        (dotimes [_i n-iter]
          (let [binf-view (binf/view (binf/buffer size))]
            (dotimes [j size]
              (binf/wa-b8 binf-view
                          j
                          (b)))))))))
