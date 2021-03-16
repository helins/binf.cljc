;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns helins.binf.int

  "Handling integers (maximum 32-bits), miscellaneous coercions.
  
   64-bit integers are handled in `helins.binf.int64`."

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [str]
                  :rename  {bit-shift-left <<}))


;;;;;;;;;; Casting between  integers <= 32 bit


(defn u8

  "Converts an integer (maximum 32-bits) to an unsigned 8-bit integer."

  [max-b32]

  (bit-and 0xff
           max-b32))



(defn i8

  "Converts an integer (maximum 32-bits) to a signed 8-bit integer."

  [max-b32]

  #?(:clj  (unchecked-byte max-b32)
     :cljs (let [data-view (js/DataView. (js/ArrayBuffer. 1))]
             (.setUint8 data-view
                        0
                        max-b32)
             (.getInt8 data-view))))



(defn u16

  "Converts an integer (maximum 32-bits) to an unsigned 16-bit integer."

  ([max-b32]

   (bit-and 0xffff
            max-b32))


  ([b8-1 b8-2]

   (u16 (bit-or (<< b8-1
                    8)
                b8-2))))



(defn i16

  "Converts an integer (maximum 32-bits) to a signed 8-bit integer."


  ([max-b32]

   #?(:clj  (unchecked-short max-b32)
      :cljs (let [data-view (js/DataView. (js/ArrayBuffer. 2))]
              (.setUint16 data-view
                          0
                          max-b32)
              (.getInt16 data-view
                         0))))


  ([b8-1 b8-2]

   (i16 (u16 b8-1
             b8-2))))



(defn u32

  "Converts an integer (maximum 32-bits) to an unsigned 32-bit integer."


  ([max-b32]

   #?(:clj  (bit-and 0xffffffff
                     max-b32)
      ;; Because bitwise operations in JS are 32 bits, bit-and'ing does not work in this case.

      :cljs (let [data-view (js/DataView. (js/ArrayBuffer. 4))]
              (.setUint32 data-view
                          0
                          max-b32)
              (.getUint32 data-view
                          0))))


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

  "Converts an integer (maximum 32-bits) to a signed 32-bit integer."


  ([max-b32]

   #?(:clj  (unchecked-int max-b32)
      :cljs (let [data-view (js/DataView. (js/ArrayBuffer. 4))]
              (.setUint32 data-view
                          0
                          max-b32)
              (.getInt32 data-view
                         0))))


  ([b16-1 b16-2]

   (i32 (u32 b16-1
             b16-2)))

  ([b8-1 b8-2 b8-3 b8-4]

   (i32 (u32 b8-1
             b8-2
             b8-3
             b8-4))))



(defn- -b64

  ;; Helper for [[i64]] and [[i-u64]].

  [b32-1 b32-2]

  #?(:clj  (bit-or (<< b32-1
                       32)
                   b32-2)
     :cljs (bit-or (<< (js/BigInt b32-1)
                       (js/BigInt 32))
                   (js/BigInt b32-2))))


(defn i64

  "Combines different integers (maximum 32-bits) into a signed 64-bit integer.
  
   See the `helins.binf.int64` namespace for how to use 64-bit integers."


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

  "Combines different integers (maximum 32-bits) into an unsigned 64-bit integer.
  
   See the `helins.binf.int64` namespace for how to use 64-bit integers."


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


;;;;;;;;;; Relation to floating numbers


(defn from-float

  "Truncates a float value to an integer (eg. `42.7` to `42`)."

  [floating]

  (long floating))


;;;;;;;;;; Converting to strings


(defn str

  "Converts the given integer into a string.
  
   A radix can be provided (eg. 16 for hexadecimal or 2 for binary)."


  ([max-b32]

   (clojure.core/str max-b32))


  ([radix max-b32]

   #?(:clj  (Long/toString max-b32
                           radix)
      :cljs (.toString max-b32
                       radix))))
