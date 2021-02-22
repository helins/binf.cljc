(ns helins.binf.int64

  ""

  {:author "Adam Helinski"}

  #?(:clj (:require [clojure.core]
                    [helins.binf.int :as binf.int]))
  #?(:cljs (:require-macros [helins.binf.int64 :refer [i*
                                                       u*]]))
  (:refer-clojure :exclude [bit-clear
                            bit-flip
                            bit-set
                            bit-test]
                  :rename  {bit-shift-left <<}))


;;;;;;;;;; Private


#?(:cljs (defn ^:private -ix

  ;;

  [n-bit big-int]

  (-> (js/BigInt.asIntN n-bit
                        big-int)
      js/Number)))



#?(:cljs (defn ^:private -ux

  ;;

  [n-bit big-int]

  (-> (js/BigInt.asUintN n-bit
                         big-int)
      js/Number)))


;;;;;;;;;; Casting to ints <= 32-bits


(defn i8

  [b64]

  #?(:clj  (binf.int/i8 b64)
     :cljs (-ix 8
                b64)))



(defn u8

  [b64]

  #?(:clj  (binf.int/u8 b64)
     :cljs (-ux 8
                b64)))



(defn i16

  [b64]

  #?(:clj  (binf.int/i16 b64)
     :cljs (-ix 16
                b64)))



(defn u16

  [b64]

  #?(:clj  (binf.int/u16 b64)
     :cljs (-ux 16
                b64)))



(defn i32

  [b64]

  #?(:clj  (binf.int/i32 b64)
     :cljs (-ix 32
                b64)))



(defn u32

  [b64]

  #?(:clj  (binf.int/u32 b64)
     :cljs (-ux 32
                b64)))


;;;;;;;;;; Macros for declaring 64-bit integers


#?(:clj (defn- -b64*

  ;;

  [env as-xint x]

  (let [x-2 (unchecked-long x)]
    (if (:ns env)
      `(~as-xint 64
                 (js/BigInt ~(str x-2)))
      x-2))))



#?(:clj (defmacro i*

  ""

  [n]

  (-b64* &env
         'js/BigInt.asIntN
         n)))



#?(:clj (defmacro u*

  ""

  [n]

  (-b64* &env
         'js/BigInt.asUintN
         n)))


;;;;;;;;;; Bitwise operations from standard lib which does not work with js/BigInt


(defn bit-clear

  ""

  [x n]

  #?(:clj  (clojure.core/bit-clear x
                                   n)
     :cljs (bit-and x
                    (bit-not (<< (u* 1)
                                 n)))))



(defn bit-flip

  ""

  [x n]

  #?(:clj  (clojure.core/bit-flip x
                                  n)
     :cljs (bit-xor x
                    (<< (u* 1)
                        n))))


(defn bit-set

  ""

  [x n]

  #?(:clj  (clojure.core/bit-set x
                                 n)
     :cljs (bit-or x
                   (<< (u* 1)
                       n))))



(defn bit-test

  ""

  [x n]

  #?(:clj  (clojure.core/bit-test x
                                  n)
     :cljs (not (= (bit-and x
                            (<< (u* 1)
                                      n))
                   (u* 0)))))


;;;;;;;;;; Unsigned logic tests


(defn u=

  ""

  [u64-1 u64-2]

  #?(:clj  (zero? (Long/compareUnsigned u64-1
                                        u64-2))
     :cljs (= u64-1
              u64-2)))



(defn u<

  ""

  [u64-1 u64-2]

  #?(:clj  (neg? (Long/compareUnsigned u64-1
                                       u64-2))
     :cljs (< u64-1
              u64-2)))



(defn u<=

  ""

  [u64-1 u64-2]

  #?(:clj  (<= (Long/compareUnsigned u64-1
                                     u64-2)
               0)
     :cljs (<= u64-1
               u64-2)))



(defn u>

  ""

  [u64-1 u64-2]

  #?(:clj  (pos? (Long/compareUnsigned u64-1
                                       u64-2))
     :cljs (> u64-1
              u64-2)))



(defn u>=

  ""

  [u64-1 u64-2]

  #?(:clj  (>= (Long/compareUnsigned u64-1
                                     u64-2)
               0)
     :cljs (>= u64-1
               u64-2)))


;;;;;;;;;; Unsigned maths


(defn udiv

  ""

  [u64-1 u64-2]

  #?(:clj  (Long/divideUnsigned u64-1
                                u64-2)
     :cljs (/ u64-1
              u64-2)))



(defn urem

  ""

  [u64-1 u64-2]

  #?(:clj  (Long/remainderUnsigned u64-1
                                   u64-2)
     :cljs (js-mod u64-1
                   u64-2)))
