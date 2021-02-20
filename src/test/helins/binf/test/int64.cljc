(ns helins.binf.test.int64

  {:author "Adam Helins"}

  (:require [clojure.test      :as t]
            [helins.binf.int64 :as binf.int64])
  (:refer-clojure :exclude [bit-clear
                            bit-flip
                            bit-set
                            bit-test]))


;;;;;;;;;;


(t/deftest casting-smaller

  (let [n (binf.int64/i* -42)]
    (t/is (= -42
             (binf.int64/i8  n)
             (binf.int64/i16 n)
             (binf.int64/i32 n))
          "Signed"))

  (let [n (binf.int64/u* 42)]
    (t/is (= 42
             (binf.int64/u8  n)
             (binf.int64/u16 n)
             (binf.int64/u32 n))
          "Unsigned")))


;;;;;;;;;;


(t/deftest bit-clear

  (t/is (zero? (binf.int64/u32 (binf.int64/bit-clear (binf.int64/u* 2r10)
                                                     (binf.int64/u* 1)))))

  (t/is (zero? (binf.int64/u32 (binf.int64/bit-clear (binf.int64/u* 0)
                                                     (binf.int64/u* 1))))))



(t/deftest bit-flip

  (t/is (zero? (binf.int64/u32 (binf.int64/bit-flip (binf.int64/u* 2r10)
                                                    (binf.int64/u* 1)))))

  (t/is (= (binf.int64/u* 2)
           (binf.int64/bit-flip (binf.int64/u* 2r00)
                                (binf.int64/u* 1)))))



(t/deftest bit-set

  (t/is (= (binf.int64/u* 2)
           (binf.int64/bit-set (binf.int64/u* 2r00)
                               (binf.int64/u* 1)))))



(t/deftest bit-test

  (t/is (true? (binf.int64/bit-test (binf.int64/u* 2r10)
                                    (binf.int64/u* 1))))

  (t/is (false? (binf.int64/bit-test (binf.int64/u* 0)
                                     (binf.int64/u* 1)))))
