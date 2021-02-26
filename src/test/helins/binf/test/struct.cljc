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


;;;;;;;;;;


(t/deftest unnested

  (t/is (= {:binf.struct/align        w32
            :binf.struct/layout       [:a
                                       :b
                                       :c
                                       :d]
            :binf.struct/name->member {:a (assoc (binf.struct/u8 :a)
                                                 :binf.struct/offset
                                                 0)
                                       :b (assoc (binf.struct/i16 :b)
                                                 :binf.struct/offset
                                                 2)
                                       :c (assoc (binf.struct/u32 :c)
                                                 :binf.struct/offset
                                                 4)
                                       :d (assoc (binf.struct/i8 :d)
                                                 :binf.struct/offset
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
            :binf.struct/name->member {:a (assoc (binf.struct/u8 :a)
                                                 :binf.struct/offset
                                                 0)
                                       :b (assoc (binf.struct/f64 :b)
                                                 :binf.struct/offset
                                                 8)
                                       :c (assoc (binf.struct/i16 :c)
                                                 :binf.struct/offset
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
            :binf.struct/name->member {:a (assoc (binf.struct/i8 :a)
                                                 :binf.struct/offset
                                                 0)
                                       :b (assoc (binf.struct/u16 :b)
                                                 :binf.struct/offset
                                                 2)
                                       :c (assoc (binf.struct/i64 :c)
                                                 :binf.struct/offset
                                                 4)
                                       :d (assoc (binf.struct/u8 :d)
                                                 :binf.struct/offset
                                                 12)}
            :binf.struct/n-byte       16}
           (binf.struct/c w32
                          [(binf.struct/i8  :a)
                           (binf.struct/u16 :b)
                           (binf.struct/i64 :c)
                           (binf.struct/u8  :d)])))
  )
