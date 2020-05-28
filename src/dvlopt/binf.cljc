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


(defprotocol IViewBuilder

  ""

  (view [this]
        [this offset]
        [this offset size]
    ""))


(defprotocol IView

  ""

  (garanteed? [this n-bytes]
    "")

  (offset [this]
    "")

  (to-buffer [this]
    ""))


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

  (position [this]

    "")

  (seek [this position]
    ""))


(defprotocol IEndianess

  ""
  
  (endianess [this]
             [this new-endianess]
    ""))


;;;;;;;;;; Types and protocol extensions


#?(:cljs

(extend-protocol ICounted

  js/ArrayBuffer

    (-count [this]
      (.-byteLength this))))



#?(:clj

(deftype View [^ByteBuffer byte-buffer
               -offset]

  clojure.lang.Counted

    (count [_]
      (- (.limit byte-buffer)
         -offset))

  IView

    (garanteed? [_ n-bytes]
      (>= (- (.limit byte-buffer)
             (.position byte-buffer))
          n-bytes))

    (offset [_]
      -offset)

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
            size))


  IRelative

    (position [_]
      (- (.position byte-buffer)
         -offset))

    (seek [this position]
      (.position byte-buffer
                 position)
      this)


  IEndianess

    (endianess [_]
      (condp =
             (.order byte-buffer)
        ByteOrder/BIG_ENDIAN    :big-endian
        ByteOrder/LITTLE_ENDIAN :little-endian))

    (endianess [this new-endianess]
      (.order byte-buffer
              (case new-endianess
                :big-endian    ByteOrder/BIG_ENDIAN
                :little-endian ByteOrder/LITTLE_ENDIAN))
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

(deftype View [^js/DataView dataview
               ^:mutable little-endian?
               ^:mutable -position]

  ICounted


    (-count [_]
      (.-byteLength dataview))


  IView


    (garanteed? [_ n-bytes]
      (>= (- (.byteLength dataview)
             -position)
          n-bytes))

    (offset [_]
      (.-byteOffset dataview))

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
             0))


  IRelative


    (position [_]
      -position)

    (seek [this position]
      (set! -position
            position)
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
      this)))



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


;;;;;;;;;; Miscellaneous


(defn copy

  ""

  ([^bytes src dest dest-offset]

   (copy src
         0
         dest
         dest-offset
         (alength src)))


  ([^bytes src src-offset ^bytes dest dest-offset n]

   #?(:clj  (System/arraycopy src
                              src-offset
                              dest
                              dest-offset
                              n)
      :cljs (.set dest
                  (.subarray src
                             src-offset
                             (+ src-offset
                                n))
                  dest-offset))))



(defn remaining

  ""

  [view]

  (- (count view)
     (position view)))
