(ns helins.binf.float

  ""

  {:author "Adam Helinski"}

  (:require #?(:cljs [helins.binf       :as binf])
                     [helins.binf.int   :as binf.int]
                     [helins.binf.int64 :as binf.int64]))


;;;;;;;;;; 


(defn bit-32

  "Converts a 32-bit float to an integer preserving the bit pattern.
  
   Opposite of [[f32]]."

  [f32]

  #?(:clj  (Float/floatToIntBits f32)
     :cljs (-> binf/-view-cast
               (binf/wa-f32 0
                            f32)
               (binf/ra-u32 0))))



(defn bit-64

  "Converts a 64-bit float to an integer preserving the bit pattern.
  
   Opposite of [[f32]]."

  [f64]

  #?(:clj  (Double/doubleToLongBits f64)
     :cljs (-> binf/-view-cast
               (binf/wa-f64 0
                            f64)
               (binf/ra-i64 0))))



(defn from-bit-32

  "Interprets bits from an integer (at least 32 bits) as a 32-bit float.
  
   Opposite of [[bits-f32]]."

  ([bits]

   #?(:clj  (Float/intBitsToFloat bits)
      :cljs (-> binf/-view-cast
                (binf/wa-b32 0
                             bits)
                (binf/ra-f32 0))))


  ([b8-1 b8-2 b8-3 b8-4]

   (from-bit-32 (binf.int/u32 b8-1
                              b8-2
                              b8-3
                              b8-4))))



(defn from-bit-64

  "Interprets bits from a 64-bits integer as a 64-bit float.
  
   Opposite of [[bits-64]]."

  ([bits]

   #?(:clj  (Double/longBitsToDouble bits)
      :cljs (-> binf/-view-cast
                (binf/wa-b64 0
                             bits)
                (binf/ra-f64 0))))


  #_([b8-1 b8-2 b8-3 b8-4 b8-5 b8-6 b8-7 b8-8]

   (f64 (binf/i64 b8-1
                  b8-2
                  b8-3
                  b8-4
                  b8-5
                  b8-6
                  b8-7
                  b8-8))))



(defn integer

  "Truncates a float value to a 64-bit unsigned integer (eg. `42.0` to `42`)."

  [floating]

  (long floating))
