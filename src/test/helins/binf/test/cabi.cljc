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



(def env32
     {:binf.cabi/align w32})



(def env64
     {:binf.cabi/align w64})



(defn member

  ""

  [f-member offset env]

  (assoc (f-member env)
         :binf.cabi/offset
         offset))


;;;;;;;;;; Unnested structs


(t/deftest struct-unnested


  (t/is (= {:binf.cabi/align          w32
            :binf.cabi/n-byte         12
            :binf.cabi/type           'foo
            :binf.cabi.struct/layout  [:a
                                       :b
                                       :c
                                       :d]
            :binf.cabi.struct/member+ {:a (member binf.cabi/u8
                                                  0
                                                  env32)
                                       :b (member binf.cabi/i16
                                                  2
                                                  env32)
                                       :c (member binf.cabi/u32
                                                  4
                                                  env32)
                                       :d (member binf.cabi/i8
                                                  8
                                                  env32)}}
           ((binf.cabi/struct 'foo
                              [[:a binf.cabi/u8]
                               [:b binf.cabi/i16] 
                               [:c binf.cabi/u32] 
                               [:d binf.cabi/i8]])
            env32)))


  (t/is (= {:binf.cabi/align          w64
            :binf.cabi/n-byte         24
            :binf.cabi/type           'foo
            :binf.cabi.struct/layout  [:a
                                       :b
                                       :c]
            :binf.cabi.struct/member+ {:a (member binf.cabi/u8
                                                  0
                                                  env64)
                                       :b (member binf.cabi/f64
                                                  8
                                                  env64)
                                       :c (member binf.cabi/i16
                                                  16
                                                  env64)}}
           ((binf.cabi/struct 'foo
                               [[:a binf.cabi/u8]
                                [:b binf.cabi/f64]
                                [:c binf.cabi/i16]])
            env64)))


  (t/is (= {:binf.cabi/align          w32
            :binf.cabi/n-byte         16
            :binf.cabi/type           'foo
            :binf.cabi.struct/layout  [:a
                                       :b
                                       :c
                                       :d]
            :binf.cabi.struct/member+ {:a (member binf.cabi/i8
                                                  0
                                                  env32)
                                       :b (member binf.cabi/u16
                                                  2
                                                  env32)
                                       :c (member binf.cabi/i64
                                                  4
                                                  env32)
                                       :d (member binf.cabi/u8
                                                  12
                                                  env32)}}
           ((binf.cabi/struct 'foo
                              [[:a binf.cabi/i8]
                               [:b binf.cabi/u16]
                               [:c binf.cabi/i64]
                               [:d binf.cabi/u8]])
            env32))))



(comment

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



)

;;;;;;;;;; Nested structs


(t/deftest struct-nested

  (let [inner (binf.cabi/struct 'bar
                                [[:c binf.cabi/i8]
                                 [:d binf.cabi/f64]])]
    (t/is (= {:binf.cabi/align          w32
              :binf.cabi/n-byte         16
              :binf.cabi/type           'foo
              :binf.cabi.struct/layout  [:a
                                         :b]
              :binf.cabi.struct/member+ {:a (member binf.cabi/u16
                                                    0
                                                    env32)
                                         :b (member inner
                                                    4
                                                    env32)}}
             ((binf.cabi/struct 'foo
                                [[:a binf.cabi/u16]
                                 [:b inner]])
              env32)))))
