;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns helins.binf.test.cabi

  {:author "Adam Helins"}

  (:require [clojure.test     :as t]
            [helins.binf.cabi :as binf.cabi])
  (:refer-clojure :exclude [array]))


;;;;;;;;;;


(def w32
     binf.cabi/word-32)


(def w64
     binf.cabi/word-64)



(defn member

  ""

  [f name align offset]

  (assoc (f name)
         :binf.cabi/align  align
         :binf.cabi/offset offset))


;;;;;;;;;; Unnested structs


(t/deftest struct-unnested


  (t/is (= {:binf.cabi/align        w32
            :binf.cabi/layout       [:a
                                     :b
                                     :c
                                     :d]
            :binf.cabi/name->member {:a (member binf.cabi/u8
                                                :a
                                                1
                                                0)
                                     :b (member binf.cabi/i16
                                                :b
                                                2
                                                2)
                                     :c (member binf.cabi/u32
                                                :c
                                                4
                                                4)
                                     :d (member binf.cabi/i8
                                                  :d
                                                  1
                                                  8)}
            :binf.cabi/n-byte       12
            :binf.cabi/type         'foo}
           (binf.cabi/struct 'foo
                              w32
                              [(binf.cabi/u8  :a)
                               (binf.cabi/i16 :b)
                               (binf.cabi/u32 :c)
                               (binf.cabi/i8  :d)])))


  (t/is (= {:binf.cabi/align        w64
            :binf.cabi/layout       [:a
                                     :b
                                     :c]
            :binf.cabi/name->member {:a (member binf.cabi/u8
                                                :a
                                                1
                                                0)
                                     :b (member binf.cabi/f64
                                                :b
                                                8
                                                8)
                                     :c (member binf.cabi/i16
                                                :c
                                                2
                                                16)}
            :binf.cabi/n-byte       24
            :binf.cabi/type         'foo}
           (binf.cabi/struct 'foo
                              w64
                              [(binf.cabi/u8  :a)
                               (binf.cabi/f64 :b)
                               (binf.cabi/i16 :c)])))


  (t/is (= {:binf.cabi/align        w32
            :binf.cabi/layout       [:a
                                     :b
                                     :c
                                     :d]
            :binf.cabi/name->member {:a (member binf.cabi/i8
                                                :a
                                                1
                                                0)
                                     :b (member binf.cabi/u16
                                                :b
                                                2
                                                2)
                                     :c (member binf.cabi/i64
                                                :c
                                                4
                                                4)
                                     :d (member binf.cabi/u8
                                                :d
                                                1
                                                12)}
            :binf.cabi/n-byte       16
            :binf.cabi/type         'foo}
           (binf.cabi/struct 'foo
                             w32
                             [(binf.cabi/i8  :a)
                              (binf.cabi/u16 :b)
                              (binf.cabi/i64 :c)
                              (binf.cabi/u8  :d)]))))


;;;;;;;;;; Arrays


(t/deftest array

  (t/is (= {:binf.cabi/align  4
            :binf.cabi/name   :a
            :binf.cabi/n-byte 40
            :binf.cabi/type   ['f32 10]}
           (binf.cabi/array (binf.cabi/f32 :a)
                              10))
        "1D")

  (t/is (= {:binf.cabi/align  4
            :binf.cabi/name   :a
            :binf.cabi/n-byte 80
            :binf.cabi/type   [['f32 10] 2]}
           (-> (binf.cabi/f32 :a)
               (binf.cabi/array 10)
               (binf.cabi/array 2)))
        "2D"))



(t/deftest struct-with-array


  (t/is (= {:binf.cabi/align        2
            :binf.cabi/layout       [:a
                                     :b]
            :binf.cabi/name->member {:a (member binf.cabi/u8
                                                :a
                                                1
                                                0)
                                     :b (member #(binf.cabi/array (binf.cabi/u16 %)
                                                                  10)
                                                :b
                                                2
                                                2)}
            :binf.cabi/n-byte       22
            :binf.cabi/type         'foo}
           (binf.cabi/struct 'foo
                             w64
                             [(binf.cabi/u8 :a)
                              (binf.cabi/array (binf.cabi/u16 :b)
                                                 10)]))
        "1D")


   (t/is (= {:binf.cabi/align        2
             :binf.cabi/layout       [:a
                                      :b]
             :binf.cabi/name->member {:a (member binf.cabi/u8
                                                 :a
                                                 1
                                                 0)
                                      :b (member #(binf.cabi/array (binf.cabi/array (binf.cabi/u16 %)
                                                                                      10)
                                                                   5)
                                                 :b
                                                 2
                                                 2)}
             :binf.cabi/n-byte       102
             :binf.cabi/type         'foo}
            (binf.cabi/struct 'foo
                              w64
                              [(binf.cabi/u8 :a)
                               (binf.cabi/array (binf.cabi/array (binf.cabi/u16 :b)
                                                                     10)
                                                  5)]))
         "2D"))


;;;;;;;;;; Nested structs


(t/deftest struct-nested

  (t/is (= {:binf.cabi/align        w32
            :binf.cabi/layout       [:a
                                       :b]
            :binf.cabi/name->member {:a (member binf.cabi/u16
                                                :a
                                                2
                                                0)
                                     :b (member #(binf.cabi/struct 'bar
                                                                   w32
                                                                   %
                                                                   [(binf.cabi/i8  :c)
                                                                    (binf.cabi/f64 :d)])
                                                :b
                                                4
                                                4)}
            :binf.cabi/n-byte       16
            :binf.cabi/type         'foo}
           (binf.cabi/struct 'foo
                             w32
                             [(binf.cabi/u16 :a)
                              (binf.cabi/struct 'bar
                                           w32
                                           :b
                                           [(binf.cabi/i8  :c)
                                            (binf.cabi/f64 :d)])]))))
