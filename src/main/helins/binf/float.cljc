(ns helins.binf.float

  ""

  {:author "Adam Helinski"}

  (:require #?(:cljs [helins.binf :as binf])))


;;;;;;;;;; 


(defn b32

  "Converts a 32-bit float to an integer preserving the bit pattern.
  
   Opposite of [[f32]]."

  [f32]

  #?(:clj  (Float/floatToIntBits f32)
     :cljs (-> binf/-view-cast
               (binf/wa-f32 0
                            f32)
               (binf/ra-u32 0))))



(defn b64

  "Converts a 64-bit float to an integer preserving the bit pattern.
  
   Opposite of [[f32]]."

  [f64]

  #?(:clj  (Double/doubleToLongBits f64)
     :cljs (-> binf/-view-cast
               (binf/wa-f64 0
                            f64)
               (binf/ra-i64 0))))



(defn from-b32

  "Interprets bits from an integer (at least 32 bits) as a 32-bit float.
  
   Opposite of [[bits-f32]]."

  [bits]

  #?(:clj  (Float/intBitsToFloat bits)
     :cljs (-> binf/-view-cast
               (binf/wa-b32 0
                            bits)
               (binf/ra-f32 0))))


(defn from-b64

  "Interprets bits from a 64-bits integer as a 64-bit float.
  
   Opposite of [[bits-64]]."

  [bits]

  #?(:clj  (Double/longBitsToDouble bits)
     :cljs (-> binf/-view-cast
               (binf/wa-b64 0
                            bits)
               (binf/ra-f64 0))))


;;;;;;;;;;


(defn f32

  ""

  [x]

  (float x))



(defn f64

  ""

  [x]

  (double x))



(defn integer

  "Truncates a float value to a 64-bit unsigned integer (eg. `42.0` to `42`)."

  [floating]

  (long floating))
