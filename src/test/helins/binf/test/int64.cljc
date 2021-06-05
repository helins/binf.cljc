;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns helins.binf.test.int64

  "Testing 64-bit integer utilities."

  {:author "Adam Helins"}

  (:require [clojure.test                  :as T]
            [clojure.test.check.generators :as TC.gen]
            [clojure.test.check.properties :as TC.prop]
            [helins.binf.gen               :as binf.gen]
            [helins.binf.int64             :as binf.int64]
            [helins.mprop                  :as mprop]))


;;;;;;;;;; Casting to ints <= 32-bits


(mprop/deftest cast-smaller-i

  {:ratio-num 150}

  (TC.prop/for-all [x binf.gen/i8]
    (let [x64 (binf.int64/i* x)]
      (= x
         (binf.int64/i8 x64)
         (binf.int64/i16 x64)
         (binf.int64/i32 x64)))))



(mprop/deftest cast-smaller-u

  {:ratio-num 150}

  (TC.prop/for-all [x binf.gen/u8]
    (let [x64 (binf.int64/u* x)]
      (= x
         (binf.int64/u8 x64)
         (binf.int64/u16 x64)
         (binf.int64/u32 x64)))))


;;;;;;;;;; Bitwise operations from standard lib which does not work with js/BigInt


(T/deftest bit-clear--

  (T/is (zero? (binf.int64/u32 (binf.int64/bit-clear (binf.int64/u* 2r10)
                                                     (binf.int64/u* 1)))))

  (T/is (zero? (binf.int64/u32 (binf.int64/bit-clear (binf.int64/u* 0)
                                                     (binf.int64/u* 1))))))



(T/deftest bit-flip--

  (T/is (zero? (binf.int64/u32 (binf.int64/bit-flip (binf.int64/u* 2r10)
                                                    (binf.int64/u* 1)))))

  (T/is (= (binf.int64/u* 2)
           (binf.int64/bit-flip (binf.int64/u* 2r00)
                                (binf.int64/u* 1)))))



(T/deftest bit-set--

  (T/is (= (binf.int64/u* 2)
           (binf.int64/bit-set (binf.int64/u* 2r00)
                               (binf.int64/u* 1)))))



(T/deftest bit-test--

  (T/is (true? (binf.int64/bit-test (binf.int64/u* 2r10)
                                    (binf.int64/u* 1))))

  (T/is (false? (binf.int64/bit-test (binf.int64/u* 0)
                                     (binf.int64/u* 1)))))



(mprop/deftest bitwise

  {:ratio-num 200}

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



(T/deftest u<

  (T/is (binf.int64/u< u64-min
                       u64-max))

  (T/is (false? (binf.int64/u< u64-max
                               u64-min)))

  (T/is (false? (binf.int64/u< u64-max
                               u64-max))))



(T/deftest u<=

  (T/is (binf.int64/u<= u64-min
                        u64-max))

  (T/is (false? (binf.int64/u<= u64-max
                                u64-min)))

  (T/is (binf.int64/u<= u64-max
                        u64-max)))



(T/deftest u>

  (T/is (binf.int64/u> u64-max
                       u64-min))

  (T/is (false? (binf.int64/u> u64-min
                               u64-max)))

  (T/is (false? (binf.int64/u> u64-max
                               u64-max))))



(T/deftest u>=

  (T/is (binf.int64/u>= u64-max
                        u64-min))

  (T/is (false? (binf.int64/u>= u64-min
                                u64-max)))

  (T/is (binf.int64/u>= u64-max
                        u64-max)))


;;;;;;;;;; Unsigned maths


(T/deftest udiv

  (T/is (= (binf.int64/u* 0x7fffffffffffffff)
           (binf.int64/udiv u64-max
                            (binf.int64/u* 2)))))



(T/deftest urem

  (T/is (= (binf.int64/u* 1)
           (binf.int64/urem (binf.int64/u* 10)
                            (binf.int64/u* 3)))))
