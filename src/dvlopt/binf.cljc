(ns dvlopt.binf

  ""

  {:author "Adam Helinski"}
  #?(:clj (:import (java.nio ByteBuffer
                             ByteOrder)))
  ;;
  ;; <!> Attention, higly confusing if not kept in mind <!>
  ;;
  (:refer-clojure :exclude [and
                            or]))


;;;;;;;;;; Gathering declarations


(declare u8
         i8
         u16
         i16
         u32
         i32)


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


(defprotocol IRelativeReader

  ""

  (rr-u8 [this]
    "")

  (rr-i8 [this]
    "")

  (rr-u16 [this]
    "")

  (rr-i16 [this]
    "")

  (rr-u32 [this]
    "")

  (rr-i32 [this]
    "")

  (rr-i64 [this]
    "")

  (rr-f32 [this]
    "")

  (rr-f64 [this]
    ""))


(defprotocol IAbsoluteWriter

  ""
  
  (wa-8 [this offset integer]
    "")

  (wa-16 [this offset integer]
    "")

  (wa-32 [this offset integer]
    "")

  (wa-64 [this offset integer]
    "")

  (wa-f32 [this offset floating]
    "")

  (wa-f64 [this offset floating]
    ""))


(defprotocol IRelativeWriter

  ""

  (wr-8 [this integer]
    "")

  (wr-16 [this integer]
    "")

  (wr-32 [this integer]
    "")

  (wr-64 [this integer]
    "")

  (wr-f32 [this floating]
    "")

  (wr-f64 [this floating]
    ""))


(defprotocol IRelative

  ""

  (offset [this]

    "")

  (seek [this offset]
    ""))

;;;;;;;;;; Types and protocol extensions


#?(:clj

(deftype View [^ByteBuffer byte-buffer
               endianess]

  IRelative

    (offset [_]
      (.position byte-buffer))

    (seek [this offset]
      (.position byte-buffer
                 offset)
      this)


  IAbsoluteReader

    (ra-u8 [_ offset]
      (u8 (.get byte-buffer
                ^long offset)))

    (ra-i8 [_ offset]
      (.get byte-buffer
            ^long offset))


    (ra-u16 [_ offset]
      (u16 (.getShort byte-buffer
                      offset)))

    (ra-i16 [_ offset]
      (.getShort byte-buffer
                 offset))


    (ra-u32 [_ offset]
      (u32 (.getInt byte-buffer
                    offset)))


    (ra-i32 [_ offset]
      (.getInt byte-buffer
               offset))


    (ra-i64 [_ offset]
      (.getLong byte-buffer
                offset))


    (ra-f32 [_ offset]
      (.getFloat byte-buffer
                 offset))

    (ra-f64 [_ offset]
      (.getDouble byte-buffer
                  offset))


  IRelativeReader

    
    (rr-u8 [_]
      (u8 (.get byte-buffer)))

    (rr-i8 [_]
      (.get byte-buffer))

    (rr-u16 [_]
      (u16 (.getShort byte-buffer)))

    (rr-i16 [_]
      (.getShort byte-buffer))

    (rr-u32 [_]
      (u32 (.getInt byte-buffer)))

    (rr-i32 [_]
      (.getInt byte-buffer))

    (rr-i64 [_]
      (.getLong byte-buffer))

    (rr-f32 [_]
      (.getFloat byte-buffer))

    (rr-f64 [_]
      (.getDouble byte-buffer))


  IAbsoluteWriter


    (wa-8 [this offset integer]
      (.put byte-buffer
            offset
            (unchecked-byte integer))
      this)

    (wa-16 [this offset integer]
      (.putShort byte-buffer
                 offset
                 (unchecked-short integer))
      this)

    (wa-32 [this offset integer]
      (.putInt byte-buffer
               offset
               (unchecked-int integer))
      this)

    (wa-64 [this offset integer]
      (.putLong byte-buffer
                offset
                integer)
      this)

    (wa-f32 [this offset floating]
      (.putFloat byte-buffer
                 offset
                 floating)
      this)

    (wa-f64 [this offset floating]
      (.putDouble byte-buffer
                  offset
                  floating)
      this)


  IRelativeWriter


    (wr-8 [this integer]
      (.put byte-buffer
            (unchecked-byte integer))
      this)

    (wr-16 [this integer]
      (.putShort byte-buffer
                 (unchecked-short integer))
      this)

    (wr-32 [this integer]
      (.putInt byte-buffer
               (unchecked-int integer))
      this)

    (wr-64 [this integer]
      (.putLong byte-buffer
                integer)
      this)

    (wr-f32 [this floating]
      (.putFloat byte-buffer
                 floating)
      this)

    (wr-f64 [this floating]
      (.putDouble byte-buffer
                  floating)
      this)))



#?(:cljs

(deftype View [dataview
               endianess
               ^:mutable -offset]

  IRelative

    (offset [_]
      -offset)

    (seek [this offset]
      (set! -offset
            offset)
      this)


  IAbsoluteReader

    (ra-u8 [_ offset]
      (.getUint8 dataview
                 offset
                 endianess))

    (ra-i8 [_ offset]
      (.getInt8 dataview
                offset
                endianess))

    (ra-u16 [_ offset]
      (.getUint16 dataview
                  offset
                  endianess))

    (ra-i16 [_ offset]
      (.getInt16 dataview
                 offset
                 endianess))

    (ra-u32 [_ offset]
      (.getUint32 dataview
                  offset
                  endianess))

    (ra-i32 [_ offset]
      (.getInt32 dataview
                 offset
                 endianess))

    (ra-i64 [_ offset]
      (.getBigInt64 dataview
                    offset
                    endianess))

    (ra-f32 [_ offset]
      (.getFloat32 dataview
                   offset
                   endianess))

    (ra-f64 [_ offset]
      (.getFloat64 dataview
                   offset
                   endianess))


  IRelativeReader


    (rr-u8 [this]
      (let [ret (ra-u8 this
                       -offset)]
        (set! -offset
              (inc -offset))
        ret))

    (rr-i8 [this]
      (let [ret (ra-i8 this
                       -offset)]
        (set! -offset
              (inc -offset))
        ret))

    (rr-u16 [this]
      (let [ret (ra-u16 this
                        -offset)]
        (set! -offset
              (+ -offset
                 2))
        ret))

    (rr-i16 [this]
      (let [ret (ra-i16 this
                        -offset)]
        (set! -offset
              (+ -offset
                 2))
        ret))

    (rr-u32 [this]
      (let [ret (ra-u32 this
                        -offset)]
        (set! -offset
              (+ -offset
                 4))
        ret))

    (rr-i32 [this]
      (let [ret (ra-i32 this
                        -offset)]
        (set! -offset
              (+ -offset
                 4))
        ret))

    (rr-i64 [this]
      (let [ret (ra-i64 this
                        -offset)]
        (set! -offset
              (+ -offset
                 8))
        ret))

    (rr-f32 [this]
      (let [ret (ra-f32 this
                        -offset)]
        (set! -offset
              (+ -offset
                 8))
        ret))

    (rr-f64 [this]
      (let [ret (ra-f64 this
                       -offset)]
        (set! -offset
              (+ -offset
                 8))
        ret))


  IAbsoluteWriter

    (wa-8 [this offset integer]
       (.setUint8 dataview
                  offset
                  integer
                  endianess)
        this)

    (wa-16 [this offset integer]
      (.setInt16 dataview
                 offset
                 integer
                 endianess)
      this)

    (wa-32 [this offset integer]
      (.setInt32 dataview
                 offset
                 integer
                 endianess)
      this)

    (wa-64 [this offset integer]
      (.setBigInt64 dataview
                    offset
                    integer
                    endianess)
      this)

    (wa-f32 [this offset floating]
      (.setFloat32 dataview
                   offset
                   floating
                   endianess)
      this)

    (wa-f64 [this offset floating]
      (.setFloat64 dataview
                   offset
                   floating
                   endianess)
      this)


  IRelativeWriter
    

    (wr-8 [this integer]
      (wa-8 this
            -offset
            integer)
      (set! -offset
            (inc -offset))
      this)

    (wr-16 [this integer]
      (wa-16 this
             -offset
             integer)
      (set! -offset
            (+ -offset
               2))
      this)

    (wr-32 [this integer]
      (wa-32 this
             -offset
             integer)
      (set! -offset
            (+ -offset
               4))
      this)

    (wr-64 [this integer]
      (wa-64 this
             -offset
             integer)
      (set! -offset
            (+ -offset
               8))
      this)

    (wr-f32 [this floating]
      (wa-f32 this
              -offset
              floating)
      (set! -offset
            (+ -offset
               8))
      this)

    (wr-f64 [this floating]
      (wa-f64 this
             -offset
             floating)
      (set! -offset
            (+ -offset
               8))
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

   #?(:clj  (let [endianess-2 (if endianess
                                (case endianess
                                  :big-endian    ByteOrder/BIG_ENDIAN
                                  :little-endian ByteOrder/LITTLE_ENDIAN)
                                ByteOrder/LITTLE_ENDIAN)]
              (View. (doto (ByteBuffer/wrap buffer)
                       (.order endianess-2))
                     endianess-2))
      :cljs (View. (js/DataView. buffer)
                   (if endianess
                     (case endianess
                       :big-endian    false
                       :little-endian true)
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
               (wa-8 0
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
                (wa-16 0
                       u16)
                (ra-i16 0))))


  ([i8-1 i8-2]

   (i16 (u16 i8-1
             i8-2))))



(defn u32

  ""

  ([i32]

   #?(:clj  (and 0xffffffff
                 i32)
      ;; Because bitwise operations in JS are 32 bits, bit-and'ing does not work in this case.
      :cljs (-> -conv-view
                (wa-32 0
                       i32)
                (ra-u32 0))))

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
                (wa-32 0
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
                (wa-32 0
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
                (wa-64 0
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



(defn integer

  ""

  [floating]

  (long floating))



(defn bits-f32

  ""

  [f32]

  #?(:clj  (Float/floatToIntBits f32)
     :cljs (-> -conv-view
               (wa-f32 0
                       f32)
               (ra-u32 0))))



(defn bits-f64

  ""

  [f64]

  #?(:clj  (Double/doubleToLongBits f64)
     :cljs (-> -conv-view
               (wa-f64 0
                       f64)
               (ra-i64 0))))
