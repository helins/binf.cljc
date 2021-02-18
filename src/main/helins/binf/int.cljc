(ns helins.binf.int

  {:author "Adam Helinski"}

  #?(:cljs (:require [helins.binf :as binf]))
  (:refer-clojure :rename {bit-shift-left           <<
                           bit-shift-right          >>
                           unsigned-bit-shift-right >>>}))


;;;;;;;;;; Casting between  integers <= 32 bit


(defn i8

  ""

  [max-b32]

  #?(:clj  (unchecked-byte max-b32)
     :cljs (-> binf/-view-cast
               (binf/wa-b8 0
                           max-b32)
               (binf/ra-i8 0))))



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
                (binf/wa-b16 0
                             max-b32)
                (binf/ra-i16 0))))


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
                (binf/wa-b32 0
                             max-b32)
                (binf/ra-u32 0))))


  ([b8-1 b8-2 b8-3 b8-4]

   (u32 (bit-or (<< b8-1
                    24)
                (<< b8-2
                    16)
                (<< b8-3
                    8)
                b8-4))))



(defn i32

  ""

  ([max-b32]

   #?(:clj  (unchecked-int max-b32)
      :cljs (-> binf/-view-cast
                (binf/wa-b32 0
                             max-b32)
                (binf/ra-i32 0))))

  ([b8-1 b8-2 b8-3 b8-4]

   (i32 (u32 b8-1
             b8-2
             b8-3
             b8-4))))
