(ns helins.binf.int

  {:author "Adam Helinski"}

  #?(:cljs (:require [helins.binf :as binf]))
  (:refer-clojure :rename {bit-shift-left           <<
                           bit-shift-right          >>
                           unsigned-bit-shift-right >>>}))


;;;;;;;;;; Creating primitives from bytes


#?(:cljs (def ^:private -conv-view (binf/view (binf/buffer 8))))



;;;;;;;;;; Casting to u8


(defn i8

  ""

  [max-b32]

  #?(:clj  (unchecked-byte max-b32)
     :cljs (-> -conv-view
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
      :cljs (-> -conv-view
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
      :cljs (-> -conv-view
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
      :cljs (-> -conv-view
                (binf/wa-b32 0
                             max-b32)
                (binf/ra-i32 0))))

  ([b8-1 b8-2 b8-3 b8-4]

   (i32 (u32 b8-1
             b8-2
             b8-3
             b8-4))))




;;;;;;;;;; 



(comment




(defn f32

  "Interprets bits from an integer (at least 32 bits) as a 32-bit float.
  
   Opposite of [[bits-f32]]."

  ([bits]

   #?(:clj  (Float/intBitsToFloat bits)
      :cljs (-> -conv-view
                (wa-b32 0
                        bits)
                (ra-f32 0))))


  ([b8-1 b8-2 b8-3 b8-4]

   (f32 (u32 b8-1
             b8-2
             b8-3
             b8-4))))



(defn f64

  "Interprets bits from a 64-bits integer as a 64-bit float.
  
   Opposite of [[bits-64]]."

  ([bits]

   #?(:clj  (Double/longBitsToDouble bits)
      :cljs (-> -conv-view
                (wa-b64 0
                        bits)
                (ra-f64 0))))


  ([b8-1 b8-2 b8-3 b8-4 b8-5 b8-6 b8-7 b8-8]

   (f64 (i64 b8-1
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



(defn bits-f32

  "Converts a 32-bit float to an integer preserving the bit pattern.
  
   Opposite of [[f32]]."

  [f32]

  #?(:clj  (Float/floatToIntBits f32)
     :cljs (-> -conv-view
               (wa-f32 0
                       f32)
               (ra-u32 0))))



(defn bits-f64

  "Converts a 64-bit float to an integer preserving the bit pattern.
  
   Opposite of [[f32]]."

  [f64]

  #?(:clj  (Double/doubleToLongBits f64)
     :cljs (-> -conv-view
               (wa-f64 0
                       f64)
               (ra-i64 0))))


)
