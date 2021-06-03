;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns helins.binf.test.int64

  {:author "Adam Helins"}

  (:require [clojure.test                    :as t]
            [clojure.test.check.clojure-test :as TC.ct]
            [clojure.test.check.generators   :as TC.gen]
            [clojure.test.check.properties   :as TC.prop]
            [helins.binf.gen                 :as binf.gen]
            [helins.binf.int64               :as binf.int64]))


;;;;;;;;;; Casting to ints <= 32-bits


(TC.ct/defspec cast-smaller-i

  (TC.prop/for-all [x binf.gen/i8]
    (let [x64 (binf.int64/i* x)]
      (= x
         (binf.int64/i8 x64)
         (binf.int64/i16 x64)
         (binf.int64/i32 x64)))))



(TC.ct/defspec cast-smaller-u

  (TC.prop/for-all [x binf.gen/u8]
    (let [x64 (binf.int64/u* x)]
      (= x
         (binf.int64/u8 x64)
         (binf.int64/u16 x64)
         (binf.int64/u32 x64)))))


;;;;;;;;;; Bitwise operations from standard lib which does not work with js/BigInt


(t/deftest bit-clear--

  (t/is (zero? (binf.int64/u32 (binf.int64/bit-clear (binf.int64/u* 2r10)
                                                     (binf.int64/u* 1)))))

  (t/is (zero? (binf.int64/u32 (binf.int64/bit-clear (binf.int64/u* 0)
                                                     (binf.int64/u* 1))))))



(t/deftest bit-flip--

  (t/is (zero? (binf.int64/u32 (binf.int64/bit-flip (binf.int64/u* 2r10)
                                                    (binf.int64/u* 1)))))

  (t/is (= (binf.int64/u* 2)
           (binf.int64/bit-flip (binf.int64/u* 2r00)
                                (binf.int64/u* 1)))))



(t/deftest bit-set--

  (t/is (= (binf.int64/u* 2)
           (binf.int64/bit-set (binf.int64/u* 2r00)
                               (binf.int64/u* 1)))))



(t/deftest bit-test--

  (t/is (true? (binf.int64/bit-test (binf.int64/u* 2r10)
                                    (binf.int64/u* 1))))

  (t/is (false? (binf.int64/bit-test (binf.int64/u* 0)
                                     (binf.int64/u* 1)))))



(TC.ct/defspec bitwise

  (TC.prop/for-all [i (TC.gen/fmap #(binf.int64/u* %)
                                   (TC.gen/choose 0
                                                  64))
                    x (TC.gen/one-of [binf.gen/i64
                                      binf.gen/u64])]
    (let [off (binf.int64/bit-clear x
                                    i)
          on  (binf.int64/bit-set x
                                  i)]
    (and (not= off
               on)
         (not (binf.int64/bit-test off
                                   i))
         (binf.int64/bit-test on
                              i)
         (= off
            (binf.int64/bit-flip on
                                 i))
         (= on
            (binf.int64/bit-flip off
                                 i))))))

;;;;;;;;;; Unsigned logic tests


(def u64-max
     (binf.int64/u* 0xffffffffffffffff))



(def u64-min
     (binf.int64/u* 0))



(t/deftest u<

  (t/is (binf.int64/u< u64-min
                       u64-max))

  (t/is (false? (binf.int64/u< u64-max
                               u64-min)))

  (t/is (false? (binf.int64/u< u64-max
                               u64-max))))



(t/deftest u<=

  (t/is (binf.int64/u<= u64-min
                        u64-max))

  (t/is (false? (binf.int64/u<= u64-max
                                u64-min)))

  (t/is (binf.int64/u<= u64-max
                        u64-max)))



(t/deftest u>

  (t/is (binf.int64/u> u64-max
                       u64-min))

  (t/is (false? (binf.int64/u> u64-min
                               u64-max)))

  (t/is (false? (binf.int64/u> u64-max
                               u64-max))))



(t/deftest u>=

  (t/is (binf.int64/u>= u64-max
                        u64-min))

  (t/is (false? (binf.int64/u>= u64-min
                                u64-max)))

  (t/is (binf.int64/u>= u64-max
                        u64-max)))


;;;;;;;;;; Unsigned maths


(t/deftest udiv

  (t/is (= (binf.int64/u* 0x7fffffffffffffff)
           (binf.int64/udiv u64-max
                            (binf.int64/u* 2)))))



(t/deftest urem

  (t/is (= (binf.int64/u* 1)
           (binf.int64/urem (binf.int64/u* 10)
                            (binf.int64/u* 3)))))
