;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns helins.binf.test.cabi

  "Testing C-ABI utilities."

  {:author "Adam Helins"}

  (:require [clojure.test     :as t]
            [helins.binf :as binf]
            [helins.binf.cabi :as binf.cabi]
            [helins.binf.buffer :as binf.buffer])
  (:refer-clojure :exclude [array]))


;;;;;;;;;;


(def w32
     4)



(def w64
     8)



(def env32
     (binf.cabi/env w32))

(def env64
     (binf.cabi/env w64))



(defn member

  [f-member offset env]

  (assoc (f-member env)
         :binf.cabi/offset
         offset))


;;;;;;;;;; Enums


(t/deftest enum

  (t/is (= {:binf.cabi/align          w32
            :binf.cabi/n-byte         4
            :binf.cabi/type           :enum
            :binf.cabi.enum/constant+ {:a 0
                                       :b 1
                                       :c 1000
                                       :d 1001
                                       :e 42
                                       :f 43}
            :binf.cabi.enum/type      :foo}
           ((binf.cabi/enum :foo
                            [:a
                             :b
                             [:c 1000]
                             :d
                             [:e 42]
                             :f])
            env64))))


;;;;;;;;;; Unnested structs


(t/deftest struct-unnested


  (t/is (= {:binf.cabi/align          w32
            :binf.cabi/n-byte         12
            :binf.cabi/type           :struct
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
                                                  env32)}
            :binf.cabi.struct/type    :foo}
           ((binf.cabi/struct :foo
                              [[:a binf.cabi/u8]
                               [:b binf.cabi/i16] 
                               [:c binf.cabi/u32] 
                               [:d binf.cabi/i8]])
            env32)))


  (t/is (= {:binf.cabi/align          w64
            :binf.cabi/n-byte         24
            :binf.cabi/type           :struct
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
                                                  env64)}
            :binf.cabi.struct/type    :foo}
           ((binf.cabi/struct :foo
                               [[:a binf.cabi/u8]
                                [:b binf.cabi/f64]
                                [:c binf.cabi/i16]])
            env64)))


  (t/is (= {:binf.cabi/align          w32
            :binf.cabi/n-byte         16
            :binf.cabi/type           :struct
            :binf.cabi.struct/layout  [:a
                                       :b
                                       :c
                                       :d]
            :binf.cabi.struct/member+ {:a (member binf.cabi/bool
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
                                                  env32)}
            :binf.cabi.struct/type    :foo}
           ((binf.cabi/struct :foo
                              [[:a binf.cabi/bool]
                               [:b binf.cabi/u16]
                               [:c binf.cabi/i64]
                               [:d binf.cabi/u8]])
            env32))))


;;;;;;;;;; Pointers


(t/deftest ptr

  (t/is (= {:binf.cabi/align          4
            :binf.cabi/n-byte         w32
            :binf.cabi/type           :ptr
            :binf.cabi.pointer/target ((binf.cabi/struct :foo
                                                         [[:a binf.cabi/u64]])
                                       env32)}
           ((binf.cabi/ptr (binf.cabi/struct :foo
                                             [[:a binf.cabi/u64]]))
            (assoc env32
                   :binf.cabi.pointer/n-byte
                   w32)))))

;;;;;;;;;; Arrays


(t/deftest array-primitive

  (t/is (= {:binf.cabi/align           4
            :binf.cabi/n-byte          40
            :binf.cabi/type            :array
            :binf.cabi.array/element   (binf.cabi/f32 env64)
            :binf.cabi.array/n-element 10}
           ((binf.cabi/array binf.cabi/f32
                             10)
            env64))
        "1D")

  (t/is (= {:binf.cabi/align           4
            :binf.cabi/n-byte          160
            :binf.cabi/type            :array
            :binf.cabi.array/element   {:binf.cabi/align           4
                                        :binf.cabi/n-byte          80
                                        :binf.cabi/type            :array
                                        :binf.cabi.array/element   (binf.cabi/f64 env32)
                                        :binf.cabi.array/n-element 10}
            :binf.cabi.array/n-element 2}
           ((-> binf.cabi/f64
                (binf.cabi/array 10)
                (binf.cabi/array 2))
            env32))
        "2D"))



(t/deftest struct-with-array


  (t/is (= {:binf.cabi/align          2
            :binf.cabi/n-byte         22
            :binf.cabi/type           :struct
            :binf.cabi.struct/member+ {:a (member binf.cabi/u8
                                                  0
                                                  env64)
                                       :b (member (fn [env]
                                                    ((binf.cabi/array binf.cabi/u16
                                                                      10)
                                                     env))
                                                  2
                                                  env64)}
            :binf.cabi.struct/layout  [:a
                                       :b]
            :binf.cabi.struct/type    :foo}
           ((binf.cabi/struct :foo
                              [[:a binf.cabi/u8]
                               [:b (binf.cabi/array binf.cabi/u16
                                                    10)]])
            env64))
        "1D")


  (t/is (= {:binf.cabi/align          2
            :binf.cabi/n-byte         102
            :binf.cabi/type           :struct
            :binf.cabi.struct/layout  [:a
                                       :b]
            :binf.cabi.struct/member+ {:a (member binf.cabi/bool
                                                  0
                                                  env64)
                                       :b (member (fn [env]
                                                    ((-> binf.cabi/u16
                                                         (binf.cabi/array 10)
                                                         (binf.cabi/array 5))
                                                     env))
                                                  2
                                                  env64)}
            :binf.cabi.struct/type    :foo}
           ((binf.cabi/struct :foo
                              [[:a binf.cabi/bool]
                               [:b (-> binf.cabi/u16
                                       (binf.cabi/array 10)
                                       (binf.cabi/array 5))]])
            env64))
        "2D"))



(t/deftest array-struct

  (t/is (= {:binf.cabi/align           4
            :binf.cabi/n-byte          40
            :binf.cabi/type            :array
            :binf.cabi.array/element   ((binf.cabi/struct :foo
                                                          [[:a binf.cabi/u32]])
                                        env64)
            :binf.cabi.array/n-element 10}
           ((binf.cabi/array (binf.cabi/struct :foo
                                               [[:a binf.cabi/u32]])
                             10)
            env64))))


;;;;;;;;;; Nested structs


(def struct-inner
     (binf.cabi/struct :bar
                       [[:c binf.cabi/i8]
                        [:d binf.cabi/f64]]))



(t/deftest struct-nested

  (t/is (= {:binf.cabi/align          w32
            :binf.cabi/n-byte         16
            :binf.cabi/type           :struct
            :binf.cabi.struct/layout  [:a
                                       :b]
            :binf.cabi.struct/member+ {:a (member binf.cabi/u16
                                                  0
                                                  env32)
                                       :b (member struct-inner
                                                  4
                                                  env32)}
            :binf.cabi.struct/type    :foo}
           ((binf.cabi/struct :foo
                              [[:a binf.cabi/u16]
                               [:b struct-inner]])
            env32))))


;;;;;;;;;; Unions


(t/deftest union

  (t/is (= {:binf.cabi/align         8
            :binf.cabi/n-byte        16
            :binf.cabi/type          :union
            :binf.cabi.union/member+ {:a (binf.cabi/i8 env64)
                                      :b ((binf.cabi/struct :bar
                                                            [[:c binf.cabi/u16]
                                                             [:d binf.cabi/f64]])
                                          env64)}
            :binf.cabi.union/type    :foo}
           ((binf.cabi/union :foo
                             {:a binf.cabi/i8
                              :b (binf.cabi/struct :bar
                                                   [[:c binf.cabi/u16]
                                                    [:d binf.cabi/f64]])})
            env64))))


;;;;;;;;;; IO


(defn alloc-view
  ([] (alloc-view 1024))
  ([n-byte]
   (binf/endian-set (binf/view (binf.buffer/alloc n-byte)) :little-endian)))

(t/deftest wr-cabi

  (t/testing "Single primitive"
    (let [v (alloc-view)
          layout (binf.cabi/i32 env32)
          e 42]
      (binf.cabi/wr-cabi v layout e)
      (t/is (= 4 (binf/position v)))
      (binf/seek v 0)
      (t/is (= e (binf/rr-i32 v)))))

  (t/testing "Simple array of specially aligned arrays"
    (let [v (alloc-view)
          ivec3 (binf.cabi/vector :ivec3 binf.cabi/i32 3 16)
          array-length 5
          layout ((binf.cabi/array ivec3 array-length) env32)
          data (->> (range (* array-length 3))
                    (partition 3)
                    (mapv vec))
          e (->> data
                 (mapcat #(conj % 0))
                 vec)]
      (binf.cabi/wr-cabi v layout data)
      (t/is (= (* array-length 4 4) (binf/position v)))
      (binf/seek v 0)
      (doseq [i e]
        (t/is (= i (binf/rr-i32 v))))))

  (t/testing "Matrices"
    ;; Note column orientated
    (t/testing "4x4"
      (let [v (alloc-view)
            column (binf.cabi/vector :ivec4 binf.cabi/i32 4 16)
            matrix (binf.cabi/vector :mat4x4 column 4 64)
            layout (matrix env32)
            matrix (->> (range 16) (partition 4) (mapv vec))
            e (->> matrix (mapcat identity) vec)]
        (binf.cabi/wr-cabi v layout matrix)
        (binf/seek v 0)
        (doseq [i e]
          (t/is (= i (binf/rr-i32 v))))))

    (t/testing "3x2"
      (let [v (alloc-view)
            column (binf.cabi/vector :ivec4 binf.cabi/i32 2 8)
            matrix (binf.cabi/vector :mat3x2 column 3 24)
            layout (matrix env32)
            matrix (->> (range (* 3 2)) (partition 2) (mapv vec))
            e (->> matrix (mapcat identity) vec)]
        (binf.cabi/wr-cabi v layout matrix)
        (binf/seek v 0)
        (doseq [i e]
          (t/is (= i (binf/rr-i32 v))))))

    (t/testing "2x3"
      (let [v (alloc-view)
            column (binf.cabi/vector :ivec4 binf.cabi/i32 3 16)
            matrix (binf.cabi/vector :mat2x3 column 2 32)
            layout (matrix env32)
            matrix (->> (range (* 3 2)) (partition 3) (mapv vec))
            e (->> matrix (mapcat #(conj % 0)) vec)]
        (binf.cabi/wr-cabi v layout matrix)
        (binf/seek v 0)
        (doseq [i e]
          (t/is (= i (binf/rr-i32 v))))))))

(t/deftest io-roundtrip

  (t/testing "Single primitive"
    (let [v (alloc-view)
          layout (binf.cabi/i32 env32)
          e 42]
      (binf.cabi/wr-cabi v layout e)
      (binf/seek v 0)
      (let [r (binf.cabi/rr-cabi v layout)]
        (t/is (= e r)))))

  (t/testing "Single enum"
    (let [layout ((binf.cabi/enum :foo
                                  [:a
                                   :b
                                   [:c 1000]
                                   :d
                                   [:e 42]
                                   :f]) env32)]

      (let [v (alloc-view)
            e :a]
        (binf.cabi/wr-cabi v layout e)
        (binf/seek v 0)
        (let [r (binf.cabi/rr-cabi v layout)]
          (t/is (= e r))))

      (let [v (alloc-view)
            e :c]
        (binf.cabi/wr-cabi v layout e)
        (binf/seek v 0)
        (let [r (binf.cabi/rr-cabi v layout)]
          (t/is (= e r))))))

  (t/testing "Single union"
    (let [layout ((binf.cabi/union :foo
                                   {:a binf.cabi/i8
                                    :b (binf.cabi/struct :bar
                                                         [[:c binf.cabi/u16]
                                                          [:d binf.cabi/f64]])})
                  env64)]

      (let [v (alloc-view)
            e {:a 42}]
        (binf.cabi/wr-cabi v layout e)
        (t/is (= (:binf.cabi/n-byte layout) (binf/position v)))
        (binf/seek v 0)
        (let [r (binf.cabi/rr-cabi v layout {:pick-union (fn [union-type data-so-far]
                                                           :a)})]
          (t/is (= e r))))

      (let [v (alloc-view)
            e {:b {:c 43
                   :d 84.}}]
        (binf.cabi/wr-cabi v layout e)
        (t/is (= (:binf.cabi/n-byte layout) (binf/position v)))
        (binf/seek v 0)
        (let [r (binf.cabi/rr-cabi v layout {:pick-union (fn [union-type data-so-far]
                                                           :b)})]
          (t/is (= e r))))))

  (t/testing "Tagged union"
    (let [layout ((binf.cabi/struct :foobar
                                    [[:tag (binf.cabi/enum :union [:a :b])]
                                     [:union (binf.cabi/union :foo
                                                              {:a binf.cabi/i8
                                                               :b (binf.cabi/struct :bar
                                                                                    [[:c binf.cabi/u16]
                                                                                     [:d binf.cabi/f64]])})]])
                  env64)]

      (t/testing "Read as bytes"

        (let [v (alloc-view)
              e {:tag :a
                 :union {:a 42}}]
          (binf.cabi/wr-cabi v layout e)
          (t/is (= (:binf.cabi/n-byte layout) (binf/position v)))
          (binf/seek v 0)
          (let [r (binf.cabi/rr-cabi v layout {:pick-union (fn [union-type data-so-far] (:tag data-so-far))})]
            (t/is (= e r))))

        (let [v (alloc-view)
              e {:tag :b
                 :union {:b {:c 43
                             :d 84.}}}]
          (binf.cabi/wr-cabi v layout e)
          (t/is (= (:binf.cabi/n-byte layout) (binf/position v)))
          (binf/seek v 0)
          (let [r (binf.cabi/rr-cabi v layout {:pick-union (fn [union-type data-so-far] (:tag data-so-far))})]
            (t/is (= e r)))))))

  (t/testing "Struct including union"
    (let [layout ((binf.cabi/struct :foobar
                                    [[:union (binf.cabi/union :foo
                                                              {:a binf.cabi/i8
                                                               :b (binf.cabi/struct :bar
                                                                                    [[:c binf.cabi/u16]
                                                                                     [:d binf.cabi/f64]])})]
                                     [:int binf.cabi/i32]])
                  env64)]

      (let [v (alloc-view)
            e {:union {:a 42}
               :int 43}]
        (binf.cabi/wr-cabi v layout e)
        (t/is (= (:binf.cabi/n-byte layout) (binf/position v)))
        (binf/seek v 0)
        (let [r (binf.cabi/rr-cabi v layout {:pick-union (fn [union-type data-so-far] :a)})]
          (t/is (= (:binf.cabi/n-byte layout) (binf/position v)))
          (t/is (= e r))))))

  (t/testing "Simple struct"
    (let [v (alloc-view)
          layout ((binf.cabi/struct :my-struct
                                    [[:a binf.cabi/i32]
                                     [:b binf.cabi/i32]]) env32)
          e {:a 10 :b 20}]
      (binf.cabi/wr-cabi v layout e)
      (binf/seek v 0)
      (let [r (binf.cabi/rr-cabi v layout)]
        (t/is (= e r))))

    (t/testing "Missing entry when writing"
      (let [v (alloc-view)
            layout ((binf.cabi/struct :my-struct
                                      [[:a binf.cabi/i32]
                                       [:b binf.cabi/i32]]) env32)
            data {:b 20}
            e (assoc data :a 0)]
        (binf.cabi/wr-cabi v layout data)
        (binf/seek v 0)
        (let [r (binf.cabi/rr-cabi v layout)]
          (t/is (= e r))))))

  (t/testing "Simple array"
    (let [v (alloc-view)
          layout ((binf.cabi/array binf.cabi/i32 5) env32)
          e [1 2 3 4 5]]
      (binf.cabi/wr-cabi v layout e)
      (binf/seek v 0)
      (let [r (binf.cabi/rr-cabi v layout)]
        (t/is (= e r)))))

  (t/testing "Simple array of structs"
    (let [v (alloc-view)
          inner (binf.cabi/struct :my-struct
                                  [[:a binf.cabi/i32]
                                   [:b binf.cabi/i32]])
          layout ((binf.cabi/array inner 5) env32)
          e (->> (range 10) (partition 2) (mapv (fn [[a b]] {:a a :b b})))]
      (binf.cabi/wr-cabi v layout e)
      (binf/seek v 0)
      (let [r (binf.cabi/rr-cabi v layout)]
        (t/is (= e r)))))

  (t/testing "Simple array of specially aligned arrays"
    (let [v (alloc-view)
          ivec3 (binf.cabi/vector :ivec3 binf.cabi/i32 3 16)
          layout ((binf.cabi/array ivec3 5) env32)
          e (->> (range 1 8)
                 (partition 3 1)
                 (mapv vec))]
      (binf.cabi/wr-cabi v layout e)
      (binf/seek v 0)
      (let [r (binf.cabi/rr-cabi v layout)]
        (t/is (= e r)))))

  (t/testing "Struct of specially aligned arrays"
    (let [v (alloc-view)
          ivec3 (binf.cabi/vector :ivec3 binf.cabi/i32 3 16)
          layout ((binf.cabi/struct :my-struct
                                    [[:position ivec3]
                                     [:normal ivec3]]) env32)
          e {:position [3 4 5]
             :normal [10 11 12]}]
      (binf.cabi/wr-cabi v layout e)
      (binf/seek v 0)
      (let [r (binf.cabi/rr-cabi v layout)]
        (t/is (= e r))))

    (let [v (alloc-view)
          ivec3 (binf.cabi/vector :ivec3 binf.cabi/i32 3 16)
          layout ((binf.cabi/struct :my-struct
                                    [[:position ivec3]
                                     [:normal ivec3]]) env32)
          e {:position [3 4 5]
             :normal [10 11 12]}]
      (binf.cabi/wr-cabi v layout e)
      (binf/seek v 0)
      (let [r (binf.cabi/rr-cabi v layout)]
        (t/is (= e r)))))

  (t/testing "Complex structures"
    (let [v (alloc-view)
          ivec3 (binf.cabi/vector :ivec3 binf.cabi/i32 3 16)
          vertex (binf.cabi/struct :vertex
                                   [[:position ivec3]
                                    [:normal ivec3]])
          layout ((binf.cabi/struct :vertex
                                    [[:count binf.cabi/i32]
                                     [:min ivec3]
                                     [:max ivec3]
                                     [:vertices (binf.cabi/array vertex 2)]]) env32)
          e {:count 2
             :min [1 2 3]
             :max [11 12 13]
             :vertices [{:position [1 12 3]
                         :normal [1 0 0]}
                        {:position [11 2 13]
                         :normal [0 1 0]}]}]
      (binf.cabi/wr-cabi v layout e)
      (t/is (= (:binf.cabi/n-byte layout) (binf/position v)))
      (binf/seek v 0)
      (let [r (binf.cabi/rr-cabi v layout)]
        (t/is (= e r))))

    ;; Structure follows example at https://gpuweb.github.io/gpuweb/wgsl/#structure-layout-rules
    (let [v (alloc-view)
          ivec2 (binf.cabi/vector :ivec3 binf.cabi/i32 2 8)
          ivec3 (binf.cabi/vector :ivec3 binf.cabi/i32 3 16)
          ivec4 (binf.cabi/vector :ivec3 binf.cabi/i32 4 16)
          struct-a (binf.cabi/force-align (binf.cabi/struct :a
                                                            [[:u binf.cabi/i32]
                                                             [:v binf.cabi/i32]
                                                             [:w ivec2]
                                                             [:x binf.cabi/i32]]) 16)
          struct-b ((binf.cabi/struct :b
                                      [[:a ivec2]
                                       [:b ivec3]
                                       [:c binf.cabi/i32]
                                       [:d binf.cabi/i32]
                                       [:e struct-a]
                                       [:f ivec3]
                                       [:g (binf.cabi/array struct-a 3)]
                                       [:h binf.cabi/i32]]) env32)
          data {}
          e {:a [0 0],
             :b [0 0 0],
             :c 0,
             :d 0,
             :e {:u 0, :v 0, :w [0 0], :x 0},
             :f [0 0 0],
             :g [{:u 0, :v 0, :w [0 0], :x 0}
                 {:u 0, :v 0, :w [0 0], :x 0}
                 {:u 0, :v 0, :w [0 0], :x 0}],
             :h 0}]
      (binf.cabi/wr-cabi v struct-b data)
      (t/is (= (:binf.cabi/n-byte struct-b) (binf/position v)))
      (binf/seek v 0)
      (let [r (binf.cabi/rr-cabi v struct-b)]
        (t/is (= e r))))

    ;; Structure follows example at https://gpuweb.github.io/gpuweb/wgsl/#structure-layout-rules
    (let [v (alloc-view)
          ivec2 (binf.cabi/vector :ivec3 binf.cabi/i32 2 8)
          ivec3 (binf.cabi/vector :ivec3 binf.cabi/i32 3 16)
          ivec4 (binf.cabi/vector :ivec3 binf.cabi/i32 4 16)
          struct-a (binf.cabi/force-align (binf.cabi/struct :a
                                                            [[:u binf.cabi/i32]
                                                             [:v binf.cabi/i32]
                                                             [:w ivec2]
                                                             [:x binf.cabi/i32]]) 16)
          struct-b ((binf.cabi/struct :b
                                      [[:a ivec2]
                                       [:b ivec3]
                                       [:c binf.cabi/i32]
                                       [:d binf.cabi/i32]
                                       [:e struct-a]
                                       [:f ivec3]
                                       [:g (binf.cabi/array struct-a 3)]
                                       [:h binf.cabi/i32]]) env32)
          e {:a [0 0],
             :b [0 0 0],
             :c 0,
             :d 0,
             :e {:u 0, :v 9, :w [0 0], :x 0},
             :f [0 0 0],
             :g [{:u 0, :v 0, :w [0 0], :x 0}
                 {:u 0, :v 0, :w [0 0], :x 0}
                 {:u 42, :v 0, :w [0 0], :x 0}],
             :h 7}]
      (binf.cabi/wr-cabi v struct-b e)
      (t/is (= (:binf.cabi/n-byte struct-b) (binf/position v)))
      (binf/seek v 0)
      (let [r (binf.cabi/rr-cabi v struct-b)]
        (t/is (= e r)))))

  (t/testing "Matrices"

    ;; Note column orientated

    (t/testing "4x4"
      (let [v (alloc-view)
            column (binf.cabi/vector :ivec4 binf.cabi/i32 4 16)
            matrix (binf.cabi/vector :mat4x4 column 4 64)
            layout (matrix env32)
            e (->> (range 16) (partition 4) (mapv vec))]
        (binf.cabi/wr-cabi v layout e)
        (binf/seek v 0)
        (let [r (binf.cabi/rr-cabi v layout)]
          (t/is (= e r)))))

    (t/testing "3x2"
      (let [v (alloc-view)
            column (binf.cabi/vector :ivec4 binf.cabi/i32 2 8)
            matrix (binf.cabi/vector :mat3x2 column 3 24)
            layout (matrix env32)
            e (->> (range (* 3 2)) (partition 2) (mapv vec))]
        (binf.cabi/wr-cabi v layout e)
        (binf/seek v 0)
        (let [r (binf.cabi/rr-cabi v layout)]
          (t/is (= e r)))))

    (t/testing "2x3"
      (let [v (alloc-view)
            column (binf.cabi/vector :ivec4 binf.cabi/i32 3 16)
            matrix (binf.cabi/vector :mat2x3 column 2 32)
            layout (matrix env32)
            e (->> (range (* 3 2)) (partition 3) (mapv vec))]
        (binf.cabi/wr-cabi v layout e)
        (binf/seek v 0)
        (let [r (binf.cabi/rr-cabi v layout)]
          (t/is (= e r))))))

  (t/testing "Absolute functions"
    (let [v (alloc-view)
          ivec2 (binf.cabi/vector :ivec3 binf.cabi/i32 2 8)
          ivec3 (binf.cabi/vector :ivec3 binf.cabi/i32 3 16)
          ivec4 (binf.cabi/vector :ivec3 binf.cabi/i32 4 16)
          struct-a (binf.cabi/force-align (binf.cabi/struct :a
                                                            [[:u binf.cabi/i32]
                                                             [:v binf.cabi/i32]
                                                             [:w ivec2]
                                                             [:x binf.cabi/i32]]) 16)
          struct-b ((binf.cabi/struct :b
                                      [[:a ivec2]
                                       [:b ivec3]
                                       [:c binf.cabi/i32]
                                       [:d binf.cabi/i32]
                                       [:e struct-a]
                                       [:f ivec3]
                                       [:g (binf.cabi/array struct-a 3)]
                                       [:h binf.cabi/i32]]) env32)
          e {:a [0 0],
             :b [0 0 0],
             :c 0,
             :d 0,
             :e {:u 0, :v 9, :w [0 0], :x 0},
             :f [0 0 0],
             :g [{:u 0, :v 0, :w [0 0], :x 0}
                 {:u 0, :v 0, :w [0 0], :x 0}
                 {:u 42, :v 0, :w [0 0], :x 0}],
             :h 7}]
      (binf.cabi/wa-cabi v 128 struct-b e)
      (let [r (binf.cabi/ra-cabi v 128 struct-b)]
        (t/is (= e r))))))
