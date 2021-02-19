(ns helins.binf.int

  {:author "Adam Helinski"}

  #?(:cljs (:require [helins.binf          :as binf]
                     [helins.binf.protocol :as binf.protocol]))
  (:refer-clojure :rename {bit-shift-left <<}))


;;;;;;;;;; Casting between  integers <= 32 bit


(defn i8

  ""

  [max-b32]

  #?(:clj  (unchecked-byte max-b32)
     :cljs (-> binf/-view-cast
               (binf.protocol/wa-b8 0
                                    max-b32)
               (binf.protocol/ra-i8 0))))



(defn u8

  ""

  [max-b32]

  (bit-and 0xff
           max-b32))



(defn u16

  ""

  ([max-b32]

   (bit-and 0xffff
            max-b32))


  ([b8-1 b8-2]

   (u16 (bit-or (<< b8-1
                    8)
                b8-2))))



(defn i16

  ""

  ([max-b32]

   #?(:clj  (unchecked-short max-b32)
      :cljs (-> binf/-view-cast
                (binf.protocol/wa-b16 0
                                      max-b32)
                (binf.protocol/ra-i16 0))))


  ([b8-1 b8-2]

   (i16 (u16 b8-1
             b8-2))))



(defn u32

  ""

  ([max-b32]

   #?(:clj  (bit-and 0xffffffff
                     max-b32)
      ;; Because bitwise operations in JS are 32 bits, bit-and'ing does not work in this case.
      :cljs (-> binf/-view-cast
                (binf.protocol/wa-b32 0
                                      max-b32)
                (binf.protocol/ra-u32 0))))


  ([b16-1 b16-2]

   (u32 (bit-or (<< b16-1
                    16)
                b16-2)))


  ([b8-1 b8-2 b8-3 b8-4]

   (u32 (u16 b8-1
             b8-2)
        (u16 b8-3
             b8-4))))



(defn i32

  ""

  ([max-b32]

   #?(:clj  (unchecked-int max-b32)
      :cljs (-> binf/-view-cast
                (binf.protocol/wa-b32 0
                                      max-b32)
                (binf.protocol/ra-i32 0))))

  ([b16-1 b16-2]

   (i32 (u32 b16-1
             b16-2)))

  ([b8-1 b8-2 b8-3 b8-4]

   (i32 (u32 b8-1
             b8-2
             b8-3
             b8-4))))



(defn- -b64

  ;;

  [b32-1 b32-2]

  #?(:clj  (bit-or (<< b32-1
                       32)
                   b32-2)
     :cljs (bit-or (<< (js/BigInt b32-1)
                       (js/BigInt 32))
                   b32-2)))


(defn i64

  ""

  ([b32-1 b32-2]

   (let [ret (-b64 b32-1
                   b32-2)]
     #?(:clj  ret
        :cljs (js/BigInt.asIntN 64
                                ret))))


  ([b16-1 b16-2 b16-3 b16-4]

   (i64 (u32 b16-1
             b16-2)
        (u32 b16-3
             b16-4)))


  ([b8-1 b8-2 b8-3 b8-4 b8-5 b8-6 b8-7 b8-8]

   (i64 (u16 b8-1
             b8-2)
        (u16 b8-3
             b8-4)
        (u16 b8-5
             b8-6)
        (u16 b8-7
             b8-8))))



(defn u64

  ""

  ([b32-1 b32-2]

   (let [ret (-b64 b32-1
                   b32-2)]
     #?(:clj  ret
        :cljs (js/BigInt.asUintN 64
                                 ret))))


  ([b16-1 b16-2 b16-3 b16-4]

   (i64 (u32 b16-1
             b16-2)
        (u32 b16-3
             b16-4)))


  ([b8-1 b8-2 b8-3 b8-4 b8-5 b8-6 b8-7 b8-8]

   (i64 (u16 b8-1
             b8-2)
        (u16 b8-3
             b8-4)
        (u16 b8-5
             b8-6)
        (u16 b8-7
             b8-8))))
