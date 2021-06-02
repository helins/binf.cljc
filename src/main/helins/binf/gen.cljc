;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns helins.binf.gen

  "`test.check` generators for primitive integers, buffers, and views.
  
    Attention, `test.check` is not included and must be imported by the user."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.generators :as TC.gen]
            [helins.binf                   :as binf]
            [helins.binf.buffer            :as binf.buffer]
            [helins.binf.float             :as binf.float]))


;;;;;;;;;; Floas


(def f32
     (TC.gen/fmap binf.float/f32
                  (TC.gen/double* {:infinite? false
                                   :max       3.402823466e38
                                   :min       1.175494351e-38
                                   :NaN?      true})))



(def f64
     TC.gen/double)


;;;;;;;;;; Integers <= 32-bits


(def i8
     (TC.gen/choose -128
                    127))


(def i16
     (TC.gen/choose -32768
                    32767))


(def i32
     (TC.gen/choose -2147483648
                    2147483647))


(def u8
     (TC.gen/choose 0
                    255))


(def u16
     (TC.gen/choose 0
                    65535))


(def u32
     (TC.gen/choose 0
                    4294967295))


;;;;;;;;;; Integers 64-bits


(def i64
     #?(:clj  TC.gen/large-integer
        :cljs (TC.gen/fmap (fn [[a b]]
                             (js/BigInt.asIntN 64
                                               (str a
                                                    b)))
                           (TC.gen/tuple u32
                                         u32))))

(def u64
     #?(:clj  TC.gen/large-integer
        :cljs (TC.gen/fmap (fn [[a b]]
                             (js/BigInt.asUintN 64
                                                (str a
                                                     b)))
                           (TC.gen/tuple u32
                                         u32))))



;;;;;;;;;; Buffers


(let [fmap (fn [gen]
             (TC.gen/fmap (fn [u8+]
                            (let [view (binf/view (binf.buffer/alloc (count u8+)))]
                              (doseq [u8 u8+]
                                (binf/wr-b8 view
                                            u8))
                              (binf/seek view
                                         0)))
                          gen))]
  (defn view


    ([]
     
     (fmap (TC.gen/vector u8)))


    ([n-byte-min n-byte-max]

     (fmap (TC.gen/vector u8
                          n-byte-min
                          n-byte-max)))))



(let [fmap (fn [gen]
             (TC.gen/fmap binf/backing-buffer
                          gen))]
  (defn buffer


    ([]

     (fmap (view)))


    ([n-byte-min n-byte-max]

     (fmap (view n-byte-min
                 n-byte-max)))))


;;;;;;;;;;


(comment


  (seq (TC.gen/generate (buffer 16
                                32)))


  )
