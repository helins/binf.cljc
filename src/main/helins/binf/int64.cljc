(ns helins.binf.int64

  ""

  {:author "Adam Helinski"}

  (:require [helins.binf.int :as binf.int])
  #?(:cljs (:require-macros [helins.binf.int64 :refer [i64*
                                                       u64*]]))
  (:refer-clojure :exclude [bit-clear
                            bit-flip
                            bit-set
                            bit-test]
                  :rename  {bit-shift-left <<}))


;;;;;;;;;;


#?(:cljs (defn ^:private -ix

  ;;

  [n-bit big-int]

  (-> (js/BigInt.asIntN n-bit
                        big-int)
      js/Number.)))



#?(:cljs (defn ^:private -ux

  ;;

  [n-bit big-int]

  (-> (js/BigInt.asUintN n-bit
                         big-int)
      js/Number.)))


;;;;;;;;;;


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

  #?(:clj  (u16 b64)
     :cljs (-ux 16
                b64)))



(defn i32

  [b64]

  #?(:clj  (binf.int/i32 b64)
     :cljs (-ix 32
                b64)))



(defn u32

  [b64]

  #?(:clj  (u32 b64)
     :cljs (-ux 32
                b64)))


;;;;;;;;;;


#?(:clj (defn- -b64*

  ;;

  [env as-xint x]

  (if (:ns env)
    `(~as-xint 64
               (js/BigInt ~(if (number? x)
                             (str x)
                             x)))
    (unchecked-long x))))



#?(:clj (defmacro i64*

  ""

  [n]

  (-b64* &env
         'js/BigInt.asIntN
         n)))



#?(:clj (defmacro u64*

  ""

  [n]

  (-b64* &env
         'js/BigInt.asUintN
         n)))


;;;;;;;;;;


(defn bit-clear

  ""

  [x n]

  (bit-and x
           (bit-not (<< (u64* 1)
                        n))))



(defn bit-flip

  ""

  [x n]

  (bit-xor x
           (<< (u64* 1)
               n)))


(defn bit-set

  ""

  [x n]

  (bit-or x
          (<< (u64* 1)
              n)))



(defn bit-test

  ""

  [x n]

  (not (= (bit-and x
                   (<< (u64* 1)
                             n))
          (u64* 0))))
