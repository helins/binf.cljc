(ns helins.binf

  "Uninhibited library for handling any kind binary format or protocol.
  
   See README for an overview."

  {:author "Adam Helins"}

  (:require [clojure.core :as clj])
  #?(:cljs (:require-macros [helins.binf]))
  #?(:clj (:import clojure.lang.Counted
                   (java.nio ByteBuffer
                             ByteOrder
                             CharBuffer)
                   (java.nio.charset Charset
                                     CoderResult
                                     StandardCharsets)))
  (:refer-clojure :rename {bit-shift-left           <<
                           bit-shift-right          >>
                           unsigned-bit-shift-right >>>}))


(declare buffer
         copy-buffer
         text-decoder)



#?(:cljs (def ^:private -text-decoder-utf-8

  ;;

  (js/TextEncoder.)))



#?(:cljs (def ^:private -text-encoder

  ;;

  (js/TextEncoder.)))


;;;;;;;;;; Primitive type sizes


(def sz-b8

  "Number of bytes in an 8-bit integer."

  1)



(def sz-b16

  "Number of bytes in a 16-bit integer."

  2)



(def sz-b32

  "Number of bytes in a 32-bit integer."

  4)



(def sz-b64

  "Number of bytes in a 64-bit integer."

  8)



(def sz-f32

  "Number of bytes in a 32-bit float."

  4)



(def sz-f64

  "Number of bytes in a 64-bit float."

  8)


;;;;;;;;;; Types and protocol extensions


#?(:clj

(deftype View [^ByteBuffer byte-buffer
                           -offset]


  clojure.lang.Counted


    (count [_]
      (- (.limit byte-buffer)
         -offset))


  IAbsoluteReader

    (ra-buffer [this position n-byte]
      (ra-buffer this
                 position
                 n-byte
                 (buffer n-byte)
                 0))

    (ra-buffer [this position n-byte buffer]
      (ra-buffer this
                 position
                 n-byte
                 buffer
                 0))

    (ra-buffer [this position n-byte buffer offset]
      (copy-buffer buffer
                   offset
                   (to-buffer this)
                   (+ -offset
                      position)
                   n-byte))

    (ra-u8 [_ position]
      (u8 (.get byte-buffer
                (int (+ -offset
                         position)))))

    (ra-i8 [_ position]
      (.get byte-buffer
            (int (+ -offset
                    position))))


    (ra-u16 [_ position]
      (u16 (.getShort byte-buffer
                      (+ -offset
                         position))))

    (ra-i16 [_ position]
      (.getShort byte-buffer
                 (+ -offset
                    position)))


    (ra-u32 [_ position]
      (u32 (.getInt byte-buffer
                    (+ -offset
                       position))))

    (ra-i32 [_ position]
      (.getInt byte-buffer
               (+ -offset
                  position)))

    (ra-i64 [_ position]
      (.getLong byte-buffer
                (+ -offset
                   position)))

    (ra-f32 [_ position]
      (.getFloat byte-buffer
                 (+ -offset
                    position)))

    (ra-f64 [_ position]
      (.getDouble byte-buffer
                  (+ -offset
                     position)))

    (ra-string [this position n-byte]
      (ra-string this
                 nil
                 position
                 n-byte))

    (ra-string [this decoder position n-byte]
      (String. (.array byte-buffer)
               (int (+ -offset
                       position))
               ^long n-byte
               (or ^Charset decoder
                   -charset-utf-8)))


  IAbsoluteWriter


    (wa-buffer [this position buffer]
      (wa-buffer this
                 position
                 buffer
                 offset
                 (count buffer)))

    (wa-buffer [this position buffer offset]
      (wa-buffer this
                 position
                 buffer
                 offset
                 (- (count buffer)
                    offset)))

    (wa-buffer [this position buffer offset n-byte]
      (copy-buffer (to-buffer this)
                   (+ -offset
                      position)
                   buffer
                   offset
                   n-byte)
      this)

    (wa-b8 [this position integer]
      (.put byte-buffer
            (+ -offset
               position)
            (unchecked-byte integer))
      this)

    (wa-b16 [this position integer]
      (.putShort byte-buffer
                 (+ -offset
                    position)
                 (unchecked-short integer))
      this)

    (wa-b32 [this position integer]
      (.putInt byte-buffer
               (+ -offset
                  position)
               (unchecked-int integer))
      this)

    (wa-b64 [this position integer]
      (.putLong byte-buffer
                (+ -offset
                   position)
                integer)
      this)

    (wa-f32 [this position floating]
      (.putFloat byte-buffer
                 (+ -offset
                    position)
                 floating)
      this)

    (wa-f64 [this position floating]
      (.putDouble byte-buffer
                  (+ -offset
                     position)
                  floating)
      this)

    (wa-string [this position string]
      (let [saved-position (.position byte-buffer)]
        (.position byte-buffer
                   (+ -offset
                      position))
        (let [res (wr-string this
                             string)]
          (.position byte-buffer
                     saved-position)
          res)))
    

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


  IRelativeReader

    
    (rr-buffer [this n-byte]
      (rr-buffer this
                 n-byte
                 (buffer n-byte)
                 0))

    (rr-buffer [this n-byte buffer]
      (rr-buffer this
                 n-byte
                 buffer
                 0))

    (rr-buffer [this n-byte buffer offset]
      (let [b (copy-buffer buffer
                           offset
                           (to-buffer this)
                           (.position byte-buffer)
                           n-byte)]
        (skip this
              n-byte)
        b))

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

    (rr-string [this n-byte]
      (rr-string this
                 nil
                 n-byte))

    (rr-string [this decoder n-byte]
      (let [string (String. (.array byte-buffer)
                            (.position byte-buffer)
                            ^long n-byte
                            (or ^Charset decoder
                                -charset-utf-8))]
        (skip this
              n-byte)
        string))


  IRelativeWriter


    (wr-buffer [this buffer]
      (wr-buffer this
                 buffer
                 0
                 (count buffer)))

    (wr-buffer [this buffer offset]
      (wr-buffer this
                 buffer
                 offset
                 (- (count buffer)
                    offset)))

    (wr-buffer [this buffer offset n-byte]
      (copy-buffer (to-buffer this)
                   (.position byte-buffer)
                   buffer
                   offset
                   n-byte)
      (skip this
            n-byte)
      this)

    (wr-b8 [this integer]
      (.put byte-buffer
            (unchecked-byte integer))
      this)

    (wr-b16 [this integer]
      (.putShort byte-buffer
                 (unchecked-short integer))
      this)

    (wr-b32 [this integer]
      (.putInt byte-buffer
               (unchecked-int integer))
      this)

    (wr-b64 [this integer]
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

    (wr-string [this string]
      (let [encoder     (.newEncoder -charset-utf-8)
            char-buffer (if (instance? CharBuffer
                                       string)
                          string
                          (CharBuffer/wrap ^String string))
            position-bb (.position byte-buffer)
            position-cb (.position ^CharBuffer char-buffer)
            res         (.encode encoder
                                 char-buffer 
                                 byte-buffer
                                 true)
            n-byte      (- (.position byte-buffer)
                           position-bb)
            n-chars     (- (.position ^CharBuffer char-buffer)
                           position-cb)]
        (condp =
               res
          CoderResult/UNDERFLOW [true n-byte n-chars]
          CoderResult/OVERFLOW  [false n-byte n-chars char-buffer]
          (throw (ex-info (str "Unable to write string: "
                               string)
                          {::error  :string-encoding
                           ::string string})))))

        ;; It seems the encoder does not have to be flushed when writing UTF-8
        ;;
        ; (condp =
        ;        (.flush encoder
        ;                byte-buffer)
        ;   CoderResult/UNDERFLOW (- (.position byte-buffer)
        ;                            offset)
        ;   CoderResult/OVERFLOW  (throw (ex-info "Not enough bytes to flush string encoder"
        ;                                         {::error  :insufficient-output
        ;                                          ::string string}))
        ;   (throw (ex-info "Unable to flush string encoder"
        ;                   {::error  :string-encoding
        ;                    ::string string}))))


  IView

    (garanteed? [_ n-byte]
      (>= (- (.limit byte-buffer)
             (.position byte-buffer))
          n-byte))

    (offset [_]
      -offset)

    (position [_]
      (- (.position byte-buffer)
         -offset))

    (seek [this position]
      (.position byte-buffer
                 (+ -offset
                    position))
      this)

    (skip [this n-byte]
      (.position byte-buffer
                 (+ (.position byte-buffer)
                    n-byte))
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



;; Copying in CLJS is almost exactly the same as in CLJ

#?(:cljs

(deftype View [^js/DataView dataview
               ^:mutable little-endian?
               ^:mutable -position]

  ICounted


    (-count [_]
      (.-byteLength dataview))


  IAbsoluteReader

    (ra-buffer [this position n-byte]
      (ra-buffer this
                 position
                 n-byte
                 (buffer n-byte)
                 0))

    (ra-buffer [this position n-byte buffer]
      (ra-buffer this
                 position
                 n-byte
                 buffer
                 0))

    (ra-buffer [this position n-byte buffer offset]
      (copy-buffer buffer
                   offset
                   (to-buffer this)
                   (+ (.-byteOffset dataview)
                      position)
                   n-byte))

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

    (ra-string [this position n-byte]
      (ra-string this
                 nil
                 position
                 n-byte))

    (ra-string [this decoder position n-byte]
      (.decode (or decoder 
                   -text-decoder-utf-8)
               (js/Uint8Array. (.-buffer dataview)
                               (+ (.-byteOffset dataview)
                                  position)
                               n-byte)))


  IAbsoluteWriter

    (wa-buffer [this position buffer]
      (wa-buffer this
                 position
                 buffer
                 0
                 (count buffer)))

    (wa-buffer [this position buffer offset]
      (wa-buffer this
                 position
                 buffer
                 offset
                 (- (count buffer)
                    offset)))

    (wa-buffer [this position buffer offset n-byte]
      (copy-buffer (to-buffer this)
                   (+ (.-byteOffset dataview)
                      position)
                   buffer
                   offset
                   n-byte)
      this)

    (wa-b8 [this position integer]
       (.setUint8 dataview
                  position
                  integer
                  little-endian?)
        this)

    (wa-b16 [this position integer]
      (.setUint16 dataview
                  position
                  integer
                  little-endian?)
      this)

    (wa-b32 [this position integer]
      (.setUint32 dataview
                  position
                  integer
                  little-endian?)
      this)

    (wa-b64 [this position integer]
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

    (wa-string [this position string]
      (let [res         (.encodeInto -text-encoder
                                     string
                                     (js/Uint8Array. (.-buffer dataview)
                                                     (+ (.-byteOffset dataview)
                                                        position)
                                                     (- (.-byteLength dataview)
                                                        position)))
            read-UTF-16 (.-read res)]
        [(= (.-length string)
            read-UTF-16)
         (.-written res)
         read-UTF-16]))


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


    (rr-buffer [this n-byte]
      (rr-buffer this
                 n-byte
                 (buffer n-byte)
                 0))

    (rr-buffer [this n-byte buffer]
      (rr-buffer this
                 n-byte
                 buffer
                 0))

    (rr-buffer [this n-byte buffer offset]
      (let [b (ra-buffer this
                         (position this)
                         n-byte
                         buffer
                         offset)]
        (skip this
              n-byte)
        b))

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

    (rr-string [this n-byte]
      (rr-string this
                 nil
                 n-byte))

    (rr-string [this decoder n-byte]
      (let [string (ra-string this
                              decoder
                              -position
                              n-byte)]
        (skip this
              n-byte)
        string))


  IRelativeWriter
    
    (wr-buffer [this buffer]
      (wr-buffer this
                 buffer
                 0
                 (count buffer)))

    (wr-buffer [this buffer offset]
      (wr-buffer this
             buffer
             offset
             (- (count buffer)
                offset)))

    (wr-buffer [this buffer offset n-byte]
      (wa-buffer this
                 (position this)
                 buffer
                 offset
                 n-byte)
      (skip this
            n-byte)
      this)

    (wr-b8 [this integer]
      (wa-b8 this
             -position
             integer)
      (set! -position
            (inc -position))
      this)

    (wr-b16 [this integer]
      (wa-b16 this
              -position
              integer)
      (set! -position
            (+ -position
               2))
      this)

    (wr-b32 [this integer]
      (wa-b32 this
              -position
              integer)
      (set! -position
            (+ -position
               4))
      this)

    (wr-b64 [this integer]
      (wa-b64 this
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

    (wr-string [this string]
      (let [res (wa-string this
                           -position
                           string)]
        (skip this
              (res 1))
        res))


  IView


    (garanteed? [_ n-byte]
      (>= (- (.-byteLength dataview)
             -position)
          n-byte))

    (offset [_]
      (.-byteOffset dataview))

    (position [_]
      -position)

    (seek [this position]
      (set! -position
            position)
      this)

    (skip [this n-byte]
      (set! -position
            (+ -position
               n-byte))
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

  "Allocates a new buffer having `n-byte` bytes.
  
   In Clojurescript, corresponds to a JS `ArrayBuffer`.

   In Clojure on the JVM, corresponds to a plain byte array.
  
   In order to do anything interesting with this library, it needs to be wrapped in a [[view]]."

  [n-byte]

  #?(:clj  (byte-array n-byte)
     :cljs (js/ArrayBuffer. n-byte)))



#?(:cljs (defn buffer-shared

  ""

  [n-byte]

  (js/SharedArrayBuffer. n-byte)))



#?(:clj

(defmacro buffer*

  "Macro for instanting a new buffer populated by the given bytes.

   For allocating a zeroed one, see [[buffer]]."

  [& b8s]

  (let [sym-buffer (gensym)
        sym-view   (gensym)
        n          (count b8s)]
    `(let [~sym-buffer (buffer ~n)
           ~sym-view   (view ~sym-buffer)]
       ~@(map (fn set-b8 [b]
                `(wr-b8 ~sym-view
                        ~b))
              b8s)
       ~sym-buffer))))



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



#?(:cljs (defn- -buffer->view

  ;;


  ([buffer]

   (View. (js/DataView. buffer)
          false
          0))


  ([buffer offset]

   (View. (js/DataView. buffer
                        offset)
          false
          0))


  ([buffer offset n-byte]

   (View. (js/DataView. buffer
                        offset
                        n-byte)
          false
          0))))



#?(:cljs

(extend-protocol IViewBuilder


  js/ArrayBuffer

    (view

      ([this]
       (-buffer->view this))

      ([this offset]
       (-buffer->view this
                      offset))

      ([this offset n-byte]
       (-buffer->view this
                      offset
                      n-byte)))


    js/SharedArrayBuffer

      (view

        ([this]
         (-buffer->view this))

        ([this offset]
         (-buffer->view this
                        offset))

        ([this offset n-byte]
         (-buffer->view this
                        offset
                        n-byte)))))



#?(:cljs

(extend-type js/ArrayBuffer

  ICounted

    (-count [this]
      (.-byteLength this))


  ISeqable

    (-seq [this]
      (array-seq (js/Int8Array. this)))))



#?(:cljs

(extend-type js/SharedArrayBuffer

  ICounted

    (-count [this]
      (.-byteLength this))


  ISeqable

    (-seq [this]
      (array-seq (js/Int8Array. this)))))


;;;;;;;;;; Copying and miscellaneous


(defn copy-buffer

  "Copies a buffer to another buffer."

  ([src-buffer]

   (let [n-byte (count src-buffer)]
     (copy-buffer (buffer n-byte)
                  0
                  src-buffer
                  0
                  n-byte)))


  ([dest-buffer src-buffer]

   (copy-buffer dest-buffer
                0
                src-buffer
                0
                (count src-buffer)))


  ([dest-buffer dest-offset src-buffer]

   (copy-buffer dest-buffer
                dest-offset
                src-buffer
                0
                (count src-buffer)))


  ([dest-buffer dest-offset src-buffer src-offset]

   (copy-buffer dest-buffer
                dest-offset
                src-buffer
                src-offset
                (- (count src-buffer)
                   src-offset)))


  ([dest-buffer dest-offset src-buffer src-offset n-byte]

   #?(:clj  (System/arraycopy ^bytes src-buffer
                              src-offset
                              ^bytes dest-buffer
                              dest-offset
                              n-byte)
      :cljs (.set (js/Uint8Array. dest-buffer)
                  (js/Uint8Array. src-buffer
                                  src-offset
                                  n-byte)
                  dest-offset))
   dest-buffer))



(defn remaining

  "Returns the number of bytes remaining until the end of the view."

  [view]

  (- (count view)
     (position view)))


;;;;;


;#?(:cljs (defn view->data-view
;
;  ""
;
;  [view]
;
;  ))


;;;;;;;;;; Creating primitives from bytes


#?(:cljs (def ^:no-doc -view-cast (view (buffer 8))))
