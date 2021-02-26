;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns helins.binf.test.struct

  {:author "Adam Helins"}

  (:require [clojure.test       :as t]
            [helins.binf.struct :as binf.struct]))


;;;;;;;;;;


(def w32
     binf.struct/word-32)


(def w64
     binf.struct/word-64)



(defn prim

  ""

  [f name align offset]

  (assoc (f name)
         :binf.struct/align  align
         :binf.struct/offset offset))


;;;;;;;;;;


(t/deftest unnested


  (t/is (= {:binf.struct/align        w32
            :binf.struct/layout       [:a
                                       :b
                                       :c
                                       :d]
            :binf.struct/name->member {:a (prim binf.struct/u8
                                                :a
                                                1
                                                0)
                                       :b (prim binf.struct/i16
                                                :b
                                                2
                                                2)
                                       :c (prim binf.struct/u32
                                                :c
                                                4
                                                4)
                                       :d (prim binf.struct/i8
                                                :d
                                                1
                                                8)}
            :binf.struct/n-byte       12}
           (binf.struct/c w32
                          [(binf.struct/u8  :a)
                           (binf.struct/i16 :b)
                           (binf.struct/u32 :c)
                           (binf.struct/i8  :d)])))


  (t/is (= {:binf.struct/align        w64
            :binf.struct/layout       [:a
                                       :b
                                       :c]
            :binf.struct/name->member {:a (prim binf.struct/u8
                                                :a
                                                1
                                                0)
                                       :b (prim binf.struct/f64
                                                :b
                                                8
                                                8)
                                       :c (prim binf.struct/i16
                                                :c
                                                2
                                                16)}
            :binf.struct/n-byte       24}
           (binf.struct/c w64
                          [(binf.struct/u8  :a)
                           (binf.struct/f64 :b)
                           (binf.struct/i16 :c)])))


  (t/is (= {:binf.struct/align        w32
            :binf.struct/layout       [:a
                                       :b
                                       :c
                                       :d]
            :binf.struct/name->member {:a (prim binf.struct/i8
                                                :a
                                                1
                                                0)
                                       :b (prim binf.struct/u16
                                                :b
                                                2
                                                2)
                                       :c (prim binf.struct/i64
                                                :c
                                                4
                                                4)
                                       :d (prim binf.struct/u8 :d
                                                1
                                                12)}
            :binf.struct/n-byte       16}
           (binf.struct/c w32
                          [(binf.struct/i8  :a)
                           (binf.struct/u16 :b)
                           (binf.struct/i64 :c)
                           (binf.struct/u8  :d)])))
  )
