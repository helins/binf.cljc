(ns dvlopt.binf

  ""

  {:author "Adam Helinski"}
  ;;
  ;; <!> Attention, higly confusing if not kept in mind <!>
  ;;
  (:refer-clojure :exclude [and
                            or]))


;;;;;;;;; Aliases for bitwise operations


(def ^{:arglists '([x n])}
  
  <<

  ""

  bit-shift-left)



(def ^{:arglists '([x n])}
 
  >>

  ""

  bit-shift-right)



(def ^{:arglists '([x n])}
 
  >>>

  ""

  unsigned-bit-shift-right)



(def ^{:arglists '([x y]
                   [x y & more])}
      
  and

  ""

  bit-and)



(def ^{:arglists '([x y]
                   [x y & more])}

  or

  ""

  bit-or)


(def ^{:arglist '([x y]
                  [x y & more])}

  xor

  ""

  bit-xor)


(def ^{:arglists '([x])}

  !

  bit-not)


;;;;;;;;;; Protocols


(defprotocol IAbsoluteReader

  ""
  
  (ra-u8 [this offset]
    "")

  (ra-i8 [this offset]
    "")

  (ra-u16 [this offset]
    "")

  (ra-i16 [this offset]
    "")

  (ra-u32 [this offset]
    "")

  (ra-i32 [this offset]
    "")

  (ra-i64 [this offset]
    "")

  (ra-f32 [this offset]
    "")

  (ra-f64 [this offset]
    ""))


(defprotocol IAbsoluteWriter

  ""
  
  (wa-u8 [this offset u8]
    "")

  (wa-i8 [this offset i8]
    "")

  (wa-u16 [this offset u16]
    "")

  (wa-i16 [this offset i16]
    "")

  (wa-u32 [this offset u32]
    "")

  (wa-i32 [this offset i32]
    "")

  (wa-i64 [this offset i64]
    "")

  (wa-f32 [this offset f32]
    "")

  (wa-f64 [this offset f64]
    ""))


;;;;;;;;;; Types and protocol extensions


#?(:cljs

(deftype View [dataview
               endianess]

  IAbsoluteReader

    (ra-u8 [this offset]
      (.getUint8 this
                 offset
                 endianess))

    (ra-i8 [this offset]
      (.getInt8 this
                offset
                endianess))

    (ra-u16 [this offset]
      (.getUint16 this
                  offset
                  endianess))

    (ra-i16 [this offset]
      (.getInt16 this
                 offset
                 endianess))

    (ra-u32 [this offset]
      (.getUint32 this
                  offset
                  endianess))

    (ra-i64 [this offset]
      (.getInt64 this
                 offset
                 endianess))

    (ra-f32 [this offset]
      (.getFloat32 this
                   offset
                   endianess))

    (ra-f64 [this offset]
      (.getFloat64 this
                   offset
                   endianess))


  IAbsoluteWriter

    (wa-u8 [this offset u8]
       (.setUint8 this
                  offset
                  u8
                  endianess)
        this)

    (wa-i8 [this offset i8]
      (.setInt8 this
                offset
                i8
                endianess)
      this)

    (wa-u16 [this offset u16]
      (.setUint16 this
                  offset
                  u16
                  endianess)
        this)

    (wa-i16 [this offset i16]
      (.setInt16 this
                 offset
                 i16
                 endianess)
      this)

    (wa-u32 [this offset u32]
      (.setUint32 this
                  offset
                  u32
                  endianess)
      this)

    (wa-i32 [this offset i32]
      (.setInt32 this
                 offset
                 i32
                 endianess)
      this)

    (wa-i64 [this offset i64]
      (.setInt64 this
                 offset
                 i64
                 endianess)
      this)

    (wa-f32 [this offset f32]
      (.setFloat32 this
                   offset
                   f32
                   endianess)
      this)

    (wa-f64 [this offset f64]
      (.setFloat64 this
                   offset
                   f64
                   endianess)
      this)))



(defn buffer

  ""

  [n]

  #?(:clj  (byte-array n)
     :cljs (js/ArrayBuffer. n)))



(defn view

  ""

  ([buffer]

   (view buffer
         nil))


  ([buffer endianess]

   #?(:cljs (View. buffer
                   (if endianess
                     (case endianess
                       :big    false
                       :little true)
                     true)))))


;;;;;;;;;; Creating primitives from bytes


#?(:cljs (def ^:private -conv-view (view (buffer 8))))



(defn u8

  ""

  [i8]

  (and 0xff
       i8))



(defn i8

  ""

  [u8]

  #?(:clj  (unchecked-byte u8)
     :cljs (-> -conv-view
               (wa-u8 0
                      u8)
               (ra-i8 0))))



(defn u16

  ""

  ([i16]

   (and 0xffff
        i16))


  ([i8-1 i8-2]

   (u16 (or (<< i8-1
                8)
            i8-2))))



(defn i16

  ""

  ([u16]

   #?(:clj  (unchecked-short u16)
      :cljs (-> -conv-view
                (wa-u16 0
                        u16)
                (ra-i16 0))))


  ([i8-1 i8-2]

   (i16 (u16 i8-1
             i8-2))))



(defn u32

  ""

  ([i32]

   (and 0xffffffff
        i32))

  ([i8-1 i8-2 i8-3 i8-4]

   (u32 (or (<< i8-1
                24)
            (<< i8-2
                16)
            (<< i8-3
                8)
            i8-4))))



(defn i32

  ""

  ([u32]

   #?(:clj  (unchecked-int u32)
      :cljs (-> -conv-view
                (wa-u32 0
                        u32)
                (ra-i32 0))))

  ([i8-1 i8-2 i8-3 i8-4]

   (i32 (u32 i8-1
             i8-2
             i8-3
             i8-4))))



(defn i64

  ""

  [i8-1 i8-2 i8-3 i8-4 i8-5 i8-6 i8-7 i8-8]

  (or (<< i8-1
          56)
      (<< i8-2
          48)
      (<< i8-3
          40)
      (<< i8-4
          32)
      (<< i8-5
          24)
      (<< i8-6
          16)
      (<< i8-7
          8)
      i8-8))



(defn f32

  ""

  ([u32]

   #?(:clj  (Float/intBitsToFloat u32)
      :cljs (-> -conv-view
                (wa-u32 0
                        u32)
                (ra-f32 0))))


  ([i8-1 i8-2 i8-3 i8-4]

   (f32 (u32 i8-1
             i8-2
             i8-3
             i8-4))))



(defn f64

  ""

  ([i64]

   #?(:clj  (Double/longBitsToDouble i64)
      :cljs (-> -conv-view
                (wa-i64 0
                        i64)
                (ra-f64 0))))


  ([i8-1 i8-2 i8-3 i8-4 i8-5 i8-6 i8-7 i8-8]

   (f64 (i64 i8-1
             i8-2
             i8-3
             i8-4
             i8-5
             i8-6
             i8-7
             i8-8))))
