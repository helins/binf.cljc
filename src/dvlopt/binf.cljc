(ns dvlopt.binf

  ""

  {:author "Adam Helinski"}
  (:require [clojure.core :as clj])
  #?(:clj (:import clojure.lang.Counted
                   (java.nio ByteBuffer
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
  
  (ra-u8 [this position]
    "")

  (ra-i8 [this position]
    "")

  (ra-u16 [this position]
    "")

  (ra-i16 [this position]
    "")

  (ra-u32 [this position]
    "")

  (ra-i32 [this position]
    "")

  (ra-i64 [this position]
    "")

  (ra-f32 [this position]
    "")

  (ra-f64 [this position]
    ""))


(defprotocol IAbsoluteWriter

  ""
  
  (wa-8 [this position integer]
    "")

  (wa-16 [this position integer]
    "")

  (wa-32 [this position integer]
    "")

  (wa-64 [this position integer]
    "")

  (wa-f32 [this position floating]
    "")

  (wa-f64 [this position floating]
    ""))


(defprotocol IEndianess

  ""
  
  (endianess [this]
             [this new-endianess]
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


(defprotocol IView

  ""

  (garanteed? [this n-bytes]
    "")

  (offset [this]
    "")

  (position [this]

    "")

  (seek [this position]
    "")
  
  (skip [this n-bytes]
    "")

  (to-buffer [this]
    ""))


(defprotocol IViewBuilder

  ""

  (view [this]
        [this offset]
        [this offset size]
    ""))


;;;;;;;;;; Types and protocol extensions


#?(:cljs

(extend-type js/ArrayBuffer

  ICounted

    (-count [this]
      (.-byteLength this))


  ISeqable

    (-seq [this]
      (array-seq (js/Uint8Array. this)))))



#?(:clj

(deftype View [^ByteBuffer byte-buffer
               -offset]


  clojure.lang.Counted


    (count [_]
      (- (.limit byte-buffer)
         -offset))


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


  IEndianess


    (endianess [_]
      (condp =
             (.order byte-buffer)
        ByteOrder/BIG_ENDIAN    :big-endian
        ByteOrder/LITTLE_ENDIAN :little-endian))

    (endianess [this new-endianess ]
      (.order byte-buffer
              (case new-endianess
                :big-endian    ByteOrder/BIG_ENDIAN
                :little-endian ByteOrder/LITTLE_ENDIAN))
      this)


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
      this)


  IView


    (garanteed? [_ n-bytes]
      (>= (- (.limit byte-buffer)
             (.position byte-buffer))
          n-bytes))

    (offset [_]
      -offset)

    (position [_]
      (- (.position byte-buffer)
         -offset))

    (seek [this position]
      (.position byte-buffer
                 position)
      this)

    (skip [this n-bytes]
      (.position byte-buffer
                 (+ (.position byte-buffer)
                    n-bytes))
      this)

    (to-buffer [_]
      (.array byte-buffer))


  IViewBuilder


    (view [this]
      this)

    (view [_ offset]
      (view (.array byte-buffer)
            (+ -offset
               offset)))

    (view [_ offset size]
      (view (.array byte-buffer)
            (+ -offset
               offset)
            size))))


#?(:cljs

(deftype View [^js/DataView dataview
               ^:mutable little-endian?
               ^:mutable -position]

  ICounted


    (-count [_]
      (.-byteLength dataview))


  IAbsoluteReader


    (ra-u8 [_ position]
      (.getUint8 dataview
                 position
                 little-endian?))

    (ra-i8 [_ position]
      (.getInt8 dataview
                position
                little-endian?))

    (ra-u16 [_ position]
      (.getUint16 dataview
                  position
                  little-endian?))

    (ra-i16 [_ position]
      (.getInt16 dataview
                 position
                 little-endian?))

    (ra-u32 [_ position]
      (.getUint32 dataview
                  position
                  little-endian?))

    (ra-i32 [_ position]
      (.getInt32 dataview
                 position
                 little-endian?))

    (ra-i64 [_ position]
      (.getBigInt64 dataview
                    position
                    little-endian?))

    (ra-f32 [_ position]
      (.getFloat32 dataview
                   position
                   little-endian?))

    (ra-f64 [_ position]
      (.getFloat64 dataview
                   position
                   little-endian?))


  IAbsoluteWriter


    (wa-8 [this position integer]
       (.setUint8 dataview
                  position
                  integer
                  little-endian?)
        this)

    (wa-16 [this position integer]
      (.setInt16 dataview
                 position
                 integer
                 little-endian?)
      this)

    (wa-32 [this position integer]
      (.setInt32 dataview
                 position
                 integer
                 little-endian?)
      this)

    (wa-64 [this position integer]
      (.setBigInt64 dataview
                    position
                    integer
                    little-endian?)
      this)

    (wa-f32 [this position floating]
      (.setFloat32 dataview
                   position
                   floating
                   little-endian?)
      this)

    (wa-f64 [this position floating]
      (.setFloat64 dataview
                   position
                   floating
                   little-endian?)
      this)


  IEndianess


    (endianess [_]
      (if little-endian?
        :little-endian
        :big-endian))

    (endianess [this new-endianess]
      (set! little-endian?
            (case new-endianess
              :big-endian    false
              :little-endian true))
      this)


  IRelativeReader

    (rr-u8 [this]
      (let [ret (ra-u8 this
                       -position)]
        (set! -position
              (inc -position))
        ret))

    (rr-i8 [this]
      (let [ret (ra-i8 this
                       -position)]
        (set! -position
              (inc -position))
        ret))

    (rr-u16 [this]
      (let [ret (ra-u16 this
                        -position)]
        (set! -position
              (+ -position
                 2))
        ret))

    (rr-i16 [this]
      (let [ret (ra-i16 this
                        -position)]
        (set! -position
              (+ -position
                 2))
        ret))

    (rr-u32 [this]
      (let [ret (ra-u32 this
                        -position)]
        (set! -position
              (+ -position
                 4))
        ret))

    (rr-i32 [this]
      (let [ret (ra-i32 this
                        -position)]
        (set! -position
              (+ -position
                 4))
        ret))

    (rr-i64 [this]
      (let [ret (ra-i64 this
                        -position)]
        (set! -position
              (+ -position
                 8))
        ret))

    (rr-f32 [this]
      (let [ret (ra-f32 this
                        -position)]
        (set! -position
              (+ -position
                 8))
        ret))

    (rr-f64 [this]
      (let [ret (ra-f64 this
                       -position)]
        (set! -position
              (+ -position
                 8))
        ret))


  IRelativeWriter
    

    (wr-8 [this integer]
      (wa-8 this
            -position
            integer)
      (set! -position
            (inc -position))
      this)

    (wr-16 [this integer]
      (wa-16 this
             -position
             integer)
      (set! -position
            (+ -position
               2))
      this)

    (wr-32 [this integer]
      (wa-32 this
             -position
             integer)
      (set! -position
            (+ -position
               4))
      this)

    (wr-64 [this integer]
      (wa-64 this
             -position
             integer)
      (set! -position
            (+ -position
               8))
      this)

    (wr-f32 [this floating]
      (wa-f32 this
              -position
              floating)
      (set! -position
            (+ -position
               8))
      this)

    (wr-f64 [this floating]
      (wa-f64 this
             -position
             floating)
      (set! -position
            (+ -position
               8))
      this)


  IView


    (garanteed? [_ n-bytes]
      (>= (- (.byteLength dataview)
             -position)
          n-bytes))

    (offset [_]
      (.-byteOffset dataview))

    (position [_]
      -position)

    (seek [this position]
      (set! -position
            position)
      this)

    (skip [this n-bytes]
      (set! -position
            (+ -position
               n-bytes))
      this)

    (to-buffer [_]
      (.-buffer dataview))


  IViewBuilder

    (view [this]
      this)

    (view [this offset]
      (View. (js/DataView. (.-buffer dataview)
                           (+ (.-byteOffset dataview)
                              offset))
             little-endian?
             0))

    (view [this offset size]
      (View. (js/DataView. (.-buffer dataview)
                           (+ (.-byteOffset dataview)
                              offset)
                           size)
             little-endian?
             0))))


(defn buffer

  ""

  [n]

  #?(:clj  (byte-array n)
     :cljs (js/ArrayBuffer. n)))



#?(:clj

(extend-protocol IViewBuilder

  (Class/forName "[B")

    (view
     
      ([this]
       (View. (ByteBuffer/wrap this)
              0))

      ([this offset]
       (View. (doto (ByteBuffer/wrap this)
                (.position offset))
              offset))

      ([this offset size]
       (View. (doto (ByteBuffer/wrap this)
                (.position offset)
                (.limit (+ offset
                           size)))
              offset)))))



#?(:cljs

(extend-protocol IViewBuilder

  js/ArrayBuffer

    (view

      ([this]
       (View. (js/DataView. this)
              true
              0))

      ([this offset]
       (View. (js/DataView. this
                            offset)
              true
              0))

      ([this offset size]
       (View. (js/DataView. this
                            offset
                            size)
              true
              0)))))
   

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

  ([integer]

   #?(:clj  (Float/intBitsToFloat integer)
      :cljs (-> -conv-view
                (wa-32 0
                       integer)
                (ra-f32 0))))


  ([i8-1 i8-2 i8-3 i8-4]

   (f32 (u32 i8-1
             i8-2
             i8-3
             i8-4))))



(defn f64

  ""

  ([integer]

   #?(:clj  (Double/longBitsToDouble integer)
      :cljs (-> -conv-view
                (wa-64 0
                       integer)
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


;;;;;;;;;; Copying and miscellaneous


(defn copy

  ""

  ([dest-buffer dest-offset src-buffer]

   (copy dest-buffer
         dest-offset
         src-buffer
         nil))


  ([dest-buffer dest-offset src-buffer src-offset]

   (copy dest-buffer
         dest-offset
         src-buffer
         src-offset
         nil))


  ([dest-buffer dest-offset src-buffer src-offset n-bytes]

   (let [src-offset-2 (clj/or src-offset
                              0)
         n-bytes-2    (clj/or n-bytes
                              (- (count src-buffer)
                                 src-offset-2))]
   #?(:clj  (System/arraycopy ^bytes src-buffer
                              src-offset-2
                              ^bytes dest-buffer
                              dest-offset
                              n-bytes-2)
      :cljs (.set (js/Uint8Array. dest-buffer)
                  (js/Uint8Array. src-buffer
                                  src-offset-2
                                  n-bytes-2)
                  dest-offset)))
   dest-buffer))



(defn acopy

  ""

  ([dest-view dest-position src-buffer]

   (acopy dest-view
          dest-position
          src-buffer
          nil
          nil))


  ([dest-view dest-position src-buffer src-offset]

   (acopy dest-view
          dest-position
          src-buffer
          src-offset
          nil))


  ([dest-view dest-position src-buffer src-offset n-bytes]

   (copy (to-buffer dest-view)
         (+ dest-position
            (offset dest-view))
         src-buffer
         src-offset
         n-bytes)
   dest-view))



(defn rcopy

  ""

  ([dest-view src-buffer]

   (rcopy dest-view
          src-buffer
          nil
          nil))

  ([dest-view src-buffer src-offset]

   (rcopy dest-view
          src-buffer
          src-offset
          nil))


  ([dest-view src-buffer src-offset n-bytes]

   (let [src-offset-2 (clj/or src-offset
                              0)
         n-bytes-2    (clj/or n-bytes
                              (- (count src-buffer)
                                 src-offset-2))]
     (copy (to-buffer dest-view)
           (position dest-view)
           src-buffer
           src-offset-2
           n-bytes-2)
     (skip dest-view
           n-bytes-2))
   dest-view))



(defn remaining

  ""

  [view]

  (- (count view)
     (position view)))
