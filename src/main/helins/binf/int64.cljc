(ns helins.binf.int64

  ""

  {:author "Adam Helinski"}

  #?(:clj (:require [helins.binf.int :as binf.int])))


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





(comment

(defn i64

  "Recombines the given bytes in big endian order to form a 64-bit signed integer."

  [b8-1 b8-2 b8-3 b8-4 b8-5 b8-6 b8-7 b8-8]

  (bit-or (<< b8-1
              56)
          (<< b8-2
              48)
          (<< b8-3
              40)
          (<< b8-4
              32)
          (<< b8-5
              24)
          (<< b8-6
              16)
          (<< b8-7
              8)
          b8-8)))
