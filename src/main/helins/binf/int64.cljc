;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns helins.binf.int64

  "Handling 64-bit integers, miscellaneous coercions and unsigned math operations.
  
   64-bit integers deserves special attention. On the JVM, there is no explicit unsigned 64-bit
   type and in JS, there are no 64-bit integers at all. This is why creating and handling them in
   a cross-platform manner, signed or unsigned, is done via this namespace.
  
   On the JVM, unsigned 64-bit integers are actually regular 64-bit signed values but treated differently.
   What matters is the bit pattern. For human-readability, see [[str-u]].
  
   In JS, they are `BigInt`, a type which does not even interoperate with regular numbers.
  
   As a rule, all values in any operation must be 64-bit integers since THEY DO NOT MIX with <= 32-bit integers.
   This namespace provides functions for logical and mathematical operations for which sign matters (such as [u<=]]
   or [[udiv]]) as well as bitwise operations which need an alternative implementation (such as [[bit-clear]])."

  {:author "Adam Helinski"}

  #?(:clj (:require [clojure.core]
                    [helins.binf.int :as binf.int]))
  #?(:cljs (:require-macros [helins.binf.int64 :refer [i*
                                                       u*]]))
  (:refer-clojure :exclude [bit-clear
                            bit-flip
                            bit-set
                            bit-test]
                  :rename  {bit-shift-left           <<
                            bit-shift-right          >>
                            unsigned-bit-shift-right >>>}))


;;;;;;;;;; Private


#?(:cljs (defn ^:private -ix

  ;; Signed js/BigInt to number conversion.

  [n-bit big-int]

  (-> (js/BigInt.asIntN n-bit
                        big-int)
      js/Number)))



#?(:cljs (defn ^:private -ux

  ;; Unsigned js/BigInt to number conversion.

  [n-bit big-int]

  (-> (js/BigInt.asUintN n-bit
                         big-int)
      js/Number)))


;;;;;;;;;; Casting to ints <= 32-bits


(defn i8

  "Converts a 64-bit integer to a signed 8-bit integer."

  [b64]

  #?(:clj  (binf.int/i8 b64)
     :cljs (-ix 8
                b64)))



(defn u8

  "Converts a 64-bit integer to an unsigned 8-bit integer."

  [b64]

  #?(:clj  (binf.int/u8 b64)
     :cljs (-ux 8
                b64)))



(defn i16

  "Converts a 64-bit integer to a signed 16-bit integer."

  [b64]

  #?(:clj  (binf.int/i16 b64)
     :cljs (-ix 16
                b64)))



(defn u16

  "Converts a 64-bit integer to an unsigned 16-bit integer."
  [b64]

  #?(:clj  (binf.int/u16 b64)
     :cljs (-ux 16
                b64)))



(defn i32

  "Converts a 64-bit integer to a signed 32-bit integer."
  [b64]

  #?(:clj  (binf.int/i32 b64)
     :cljs (-ix 32
                b64)))



(defn u32

  "Converts a 64-bit integer to an unsigned -bit integer."
  [b64]

  #?(:clj  (binf.int/u32 b64)
     :cljs (-ux 32
                b64)))


;;;;;;;;;; Macros for declaring 64-bit integers


#?(:clj (defn- -b64*

  ;; In JS, optimize if `x` is a number between `Number.MIN_SAFE_INTEGER´ and `Number.MAX_SAFE_INTEGER`.

  [env as-xint x]

  (if (:ns env)
    `(~as-xint 64
               (js/BigInt ~(if (number? x)
                             (if (<= -9007199254740991
                                     x
                                     9007199254740991)
                               x
                               (str x))
                             x)))
    `(unchecked-long ~x))))



#?(:clj (defmacro i*

  "Macro for declaring a signed 64-bit integer.

   ```clojure
   (def n
        (i* -9223372036854775808))
   ```"


  [n]

  (-b64* &env
         'js/BigInt.asIntN
         n)))



#?(:clj (defmacro u*

  "Macro for declaring an unsigned 64-bit integer.
  
   ```clojure
   (def n
        (u* 18446744073709551615))
   ```"

  [n]

  (-b64* &env
         'js/BigInt.asUintN
         n)))


;;;;;;;;;; Bitwise operations from standard lib which does not work with js/BigInt


(defn u>>

  "Unsigned bit-shift right."

  [x n]

  #?(:clj  (>>> x
                n)
     :cljs (>> x
               n)))

(defn bit-clear

  "64-bit equivalent of the related standard function."

  [x n]

  #?(:clj  (clojure.core/bit-clear x
                                   n)
     :cljs (bit-and x
                    (bit-not (<< (u* 1)
                                 n)))))



(defn bit-flip

  "64-bit equivalent of the related standard function."

  [x n]

  #?(:clj  (clojure.core/bit-flip x
                                  n)
     :cljs (bit-xor x
                    (<< (u* 1)
                        n))))


(defn bit-set

  "64-bit equivalent of the related standard function."

  [x n]

  #?(:clj  (clojure.core/bit-set x
                                 n)
     :cljs (bit-or x
                   (<< (u* 1)
                       n))))



(defn bit-test

  "64-bit equivalent of the related standard function."

  [x n]

  #?(:clj  (clojure.core/bit-test x
                                  n)
     :cljs (not (= (bit-and x
                            (<< (u* 1)
                                      n))
                   (u* 0)))))


;;;;;;;;;; Unsigned logic tests


(defn u<

  "Unsigned 64-bit equivalent of standard `<`."

  [u64-1 u64-2]

  #?(:clj  (neg? (Long/compareUnsigned u64-1
                                       u64-2))
     :cljs (< u64-1
              u64-2)))



(defn u<=

  "Unsigned 64-bit equivalent of standard `<=`."

  [u64-1 u64-2]

  #?(:clj  (<= (Long/compareUnsigned u64-1
                                     u64-2)
               0)
     :cljs (<= u64-1
               u64-2)))



(defn u>

  "Unsigned 64-bit equivalent of standard `>`."

  [u64-1 u64-2]

  #?(:clj  (pos? (Long/compareUnsigned u64-1
                                       u64-2))
     :cljs (> u64-1
              u64-2)))



(defn u>=

  "Unsigned 64-bit equivalent of standard `>=`."

  [u64-1 u64-2]

  #?(:clj  (>= (Long/compareUnsigned u64-1
                                     u64-2)
               0)
     :cljs (>= u64-1
               u64-2)))


;;;;;;;;;; Unsigned maths


(defn udiv

  "Unsigned division."

  [u64-1 u64-2]

  #?(:clj  (Long/divideUnsigned u64-1
                                u64-2)
     :cljs (/ u64-1
              u64-2)))



(defn urem

  "Unsigned remainder."

  [u64-1 u64-2]

  #?(:clj  (Long/remainderUnsigned u64-1
                                   u64-2)
     :cljs (js-mod u64-1
                   u64-2)))


;;;;;;;;;; Converting to strings


(defn str-i

  "Converts the given signed 64-bit integer into a string.
  
   A radix can be provided (eg. 16 for hexadecimal or 2 for binary)."


  ([i64]

   (str i64))


  ([radix i64]

   #?(:clj  (Long/toString ^long i64
                           radix)
      :cljs (.toString i64
                       radix))))



(defn str-u

  "Converts the given unsigned 64-bit integer into a string.
  
   A radix can be provided (eg. 16 for hexadecimal or 2 for binary)."


  ([u64]

   (str u64))


  ([radix u64]

   #?(:clj  (Long/toUnsignedString ^long u64
                                   radix)
      :cljs (.toString u64
                       radix))))
