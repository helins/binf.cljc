(ns dvlopt.binf-test

  {:author "Adam Helinski"}

  (:require [clojure.test :as t]
            [dvlopt.binf  :as binf]))




;;;;;;;;;; Primitive conversions


(t/deftest uints

  (t/are [n fi fu]
         (let [value (dec (binf/integer (Math/pow 2
                                                  n)))]
           (t/is (= value
                    (-> value
                        fu
                        fi
                        fu
                        fi
                        fu))))
    8  binf/i8  binf/u8
    16 binf/i16 binf/u16
    32 binf/i32 binf/u32))



(t/deftest ^:no-node i64

  ; Fails on Node because it has no concept of BigInt as the browser (where ints are actually doubles < 64 bits).

  (let [value (binf/integer (Math/pow 2
                                      7))]
    (t/is (= value
             (binf/i64 (binf/u8 (binf/>> value
                                         56))
                       (binf/u8 (binf/>> value
                                         48))
                       (binf/u8 (binf/>> value
                                         40))
                       (binf/u8 (binf/>> value
                                         32))
                       (binf/u8 (binf/>> value
                                         24))
                       (binf/u8 (binf/>> value
                                         16))
                       (binf/u8 (binf/>> value
                                         8))
                       (binf/u8 value))))))



(t/deftest ^:no-js f32

  ; JS does not have real floats, imprecision arise when they get converted automatically to f64.
  ; Other than that, the implementation is technically correct.

  (t/is (= (float 42.42)
           (binf/f32 (binf/bits-f32 42.42)))
        "f32"))


(t/deftest ^:no-node f64

  ; Fails on Node, cf `i64`.

  (t/is (= 42.42
           (binf/f64 (binf/bits-f64 42.42)))
        "f32"))


;;;;;;;;;; Views


(def view-8
     (binf/view (binf/buffer 8)))



(t/deftest view-uints

  (t/are [wr rd value]
         (== value
             (-> view-8
                 (wr 0
                     value)
                 (rd 0)))


    binf/wa-u8  binf/ra-u8  (binf/integer (dec (Math/pow 2 8)))
    binf/wa-i8  binf/ra-i8  -1
    binf/wa-u16 binf/ra-u16 (binf/integer (dec (Math/pow 2 16)))
    binf/wa-i16 binf/ra-i16 -1
    binf/wa-i32 binf/ra-i32 -1
    binf/wa-u32 binf/ra-u32 (binf/integer (dec (Math/pow 2 32)))))



(t/deftest ^:no-node view-i64

  ; Node, Cf. [[i64]]

  (let [x #?(:clj  Long/MAX_VALUE
             :cljs (js/BigInt js/Number.MAX_SAFE_INTEGER))]
    (t/is (= x
             (-> view-8
                 (binf/wa-i64 0
                              x)
                 (binf/ra-i64 0))))))



(t/deftest ^:no-js view-f32

  (let [x (float 42.42)]
    (t/is (= x
             (-> view-8
                 (binf/wa-f32 0
                              x)
                 (binf/ra-f32 0))))))



(t/deftest view-f64

  (let [x 42.42]
    (t/is (= x
             (-> view-8
                 (binf/wa-f64 0
                              x)
                 (binf/ra-f64 0))))))
