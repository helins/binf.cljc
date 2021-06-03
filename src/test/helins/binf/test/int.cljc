;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns helins.binf.test.int

  {:author "Adam Helins"}

  (:require [clojure.test                    :as t]
            [clojure.test.check.clojure-test :as TC.ct]
            [clojure.test.check.properties   :as TC.prop]
            [helins.binf.gen                 :as binf.gen]
            [helins.binf.int                 :as binf.int]
            [helins.binf.int64               :as binf.int64]))


;;;;;;;;;; Casting signed <-> unsigned


(defn prop-cast-iu

  "Checking a double conversion, back and forth."

  [gen to from]

  (TC.prop/for-all [x gen]
    (= x
       (-> x
           to
           from))))



(TC.ct/defspec i8

  (prop-cast-iu binf.gen/i8
                binf.int/u8
                binf.int/i8))



(TC.ct/defspec i16

  (prop-cast-iu binf.gen/i16
                binf.int/u16
                binf.int/i16))



(TC.ct/defspec i32

  (prop-cast-iu binf.gen/i32
                binf.int/u32
                binf.int/i32))



(TC.ct/defspec u8

  (prop-cast-iu binf.gen/u8
                binf.int/i8
                binf.int/u8))



(TC.ct/defspec u16

  (prop-cast-iu binf.gen/u16
                binf.int/i16
                binf.int/u16))



(TC.ct/defspec u32

  (prop-cast-iu binf.gen/u32
                binf.int/i32
                binf.int/u32))


;;;;;;;;;; Reconstructing values from bytes


(t/deftest byte-combining

  (t/is (= 0x1122
           (binf.int/i16 0x11
                         0x22)
           (binf.int/u16 0x11
                         0x22))
        "16-bits")

  (t/is (= 0x11223344
           (binf.int/i32 0x11
                         0x22
                         0x33
                         0x44)
           (binf.int/i32 0x1122
                         0x3344)
           (binf.int/u32 0x11
                         0x22
                         0x33
                         0x44)
           (binf.int/u32 0x1122
                         0x3344))
        "32-bit")

  (t/is (= (binf.int64/i* 0x1122334455667788)
           (binf.int/i64 0x11
                         0x22
                         0x33
                         0x44
                         0x55
                         0x66
                         0x77
                         0x88)
           (binf.int/i64 0x1122
                         0x3344
                         0x5566
                         0x7788)
           (binf.int/i64 0x11223344
                         0x55667788))
        "Signed 64-bit")

  (t/is (= (binf.int64/u* 0x1122334455667788)
           (binf.int/u64 0x11
                         0x22
                         0x33
                         0x44
                         0x55
                         0x66
                         0x77
                         0x88)
           (binf.int/u64 0x1122
                         0x3344
                         0x5566
                         0x7788)
           (binf.int/u64 0x11223344
                         0x55667788))
        "Unsigned 64-bit"))


;;;;;;;;;; Stringifying


(t/deftest string

  (t/is (= "255"
           (binf.int/str 0xff)))

  (t/is (= "ff"
           (binf.int/str 16
                         0xff))))
