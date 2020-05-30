(ns dvlopt.binf

  ""

  {:author "Adam Helinski"}
  (:require [clojure.core :as clj])
  #?(:clj (:import clojure.lang.Counted
                   (java.nio ByteBuffer
                             ByteOrder
                             CharBuffer)
                   (java.nio.charset Charset
                                     CharsetEncoder
                                     CoderResult
                                     StandardCharsets)))
  ;;
  ;; <!> Attention, higly confusing if not kept in mind <!>
  ;;
  (:refer-clojure :exclude [and
                            or]))


;;;;;;;;;; Gathering declarations


(declare copy
         i8
         i16
         i32
         text-decoder
         u8
         u16
         u32)


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
    "")
  
  (ra-string [this position n-bytes]
             [this decoder position n-bytes]
    ""))


(defprotocol IAbsoluteWriter

  ""
  
  (wa-b8 [this position integer]
    "")

  (wa-b16 [this position integer]
    "")

  (wa-b32 [this position integer]
    "")

  (wa-b64 [this position integer]
    "")

  (wa-f32 [this position floating]
    "")

  (wa-f64 [this position floating]
    "")
  
  (wa-string [this position string]
    ""))


(defprotocol IEndianess

  ""
  
  (endianess [this]
             [this new-endianess]
    ""))


(defprotocol IGrowing

  ""

  (garantee [this n-bytes]
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
    "")
  
  (rr-string [this n-bytes]
             [this decoder n-bytes]
    ""))


(defprotocol IRelativeWriter

  ""

  (wr-b8 [this integer]
    "")

  (wr-b16 [this integer]
    "")

  (wr-b32 [this integer]
    "")

  (wr-b64 [this integer]
    "")

  (wr-f32 [this floating]
    "")

  (wr-f64 [this floating]
    "")

  (wr-string [this string]
    ""))


(defprotocol IView

  ""

  (copya [this position buffer]
         [this position buffer offset]
         [this position buffer offset n-bytes]
    "")

  (copyr [this buffer]
         [this buffer offset]
         [this buffer offset n-bytes]
    "")

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


;;;;;;;;;; Encoding and decoding strings


#?(:clj
   
(def ^Charset -charset-utf-8

  ;;

  StandardCharsets/UTF_8))



(def ^:private -text-decoder-utf-8

  ;;

  #?(:clj  -charset-utf-8
     :cljs (js/TextDecoder. "utf-8")))



(def ^:private -text-decoders

  ;;

  {:iso-8859-1 #?(:clj  StandardCharsets/ISO_8859_1
                  :cljs (js/TextDecoder. "iso-8859-1")) 
   :utf-8      -text-decoder-utf-8
   :utf-16-be  #?(:clj  StandardCharsets/UTF_16BE
                  :cljs (js/TextDecoder. "utf-16be"))
   :utf-16-le  #?(:clj  StandardCharsets/UTF_16LE
                  :cljs (js/TextDecoder. "utf-16le"))})



(defn text-decode

  ""

  ;; TODO. Advanced compilation ?

  ([buffer]

   (text-decode (text-decoder)
                buffer))


  ([text-decoder buffer]

   #?(:clj  (String. ^bytes buffer
                     ^Charset text-decoder)
      :cljs (.decode text-decoder
                     buffer))))



(defn text-decoder

  ""

  ([]

   -text-decoder-utf-8)


  ([encoding]

   (or (-text-decoders encoding)
       (throw (ex-info (str "Unknown encoding: "
                            encoding)
                       {::encoding encoding
                        ::error    :unknown-encoding})))))



#?(:cljs

(def ^:private -text-encoder

  ;;

  (js/TextEncoder.)))



(defn text-encode

  ""

  [string]

  #?(:clj  (.getBytes ^String string
                      -charset-utf-8)
     :cljs (.-buffer (.encode -text-encoder
                              string))))


  ; ([view string]

  ;  (let [remaining (- (count buffer)
  ;                     offset)]
  ;    #?(
  ;       :cljs (let [res (.encodeInto -text-encoder
  ;                                    string
  ;                                    (.subarray ba
  ;                                               offset
  ;                                               remaining))]
  ;               (if (= (count string)
  ;                      (.-read res))
  ;                 (.-written res)
  ;                 (throw (ex-info (str "Not enough bytes to write string: "
  ;                                      string)
  ;                                 {::error  :insufficient-output
  ;                                  ::string string}))))))))


;;;;;;;;;; Types and protocol extensions


#?(:clj

(deftype View [^ByteBuffer byte-buffer
               -offset]


  clojure.lang.Counted


    (count [_]
      (- (.limit byte-buffer)
         -offset))


  IAbsoluteReader


    (ra-u8 [_ position]
      (u8 (.get byte-buffer
                ^long position)))

    (ra-i8 [_ position]
      (.get byte-buffer
            ^long position))


    (ra-u16 [_ position]
      (u16 (.getShort byte-buffer
                      position)))

    (ra-i16 [_ position]
      (.getShort byte-buffer
                 position))


    (ra-u32 [_ position]
      (u32 (.getInt byte-buffer
                    position)))


    (ra-i32 [_ position]
      (.getInt byte-buffer
               position))


    (ra-i64 [_ position]
      (.getLong byte-buffer
                position))


    (ra-f32 [_ position]
      (.getFloat byte-buffer
                 position))

    (ra-f64 [_ position]
      (.getDouble byte-buffer
                  position))

    (ra-string [this position n-bytes]
      (ra-string this
                 nil
                 position
                 n-bytes))

    (ra-string [this decoder position n-bytes]
      (String. (.array byte-buffer)
               ^long position
               ^long n-bytes
               (clj/or ^Charset decoder
                       -charset-utf-8)))


  IAbsoluteWriter


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
      (let [saved-position (.position byte-buffer)
            res            (wr-string this
                                      string)]
        (.position byte-buffer
                   saved-position)
        res))
    

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

    (rr-string [this n-bytes]
      (rr-string this
                 nil
                 n-bytes))

    (rr-string [this decoder n-bytes]
      (let [string (ra-string this
                              decoder
                              (.position byte-buffer)
                              n-bytes)]
        (skip this
              n-bytes)
        string))


  IRelativeWriter


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
            n-bytes     (- (.position byte-buffer)
                           position-bb)
            n-chars     (- (.position ^CharBuffer char-buffer)
                           position-cb)]
        (condp =
               res
          CoderResult/UNDERFLOW [true n-bytes n-chars]
          CoderResult/OVERFLOW  [false n-bytes n-chars char-buffer]
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


    (copya [this position buffer]
      (copya this
             position
             buffer
             nil))

    (copya [this position buffer offset]
      (copya this
             position
             buffer
             offset
             nil))

    (copya [this position buffer offset n-bytes]
      (copy (to-buffer this)
            (+ -offset
               position)
            buffer
            offset
            n-bytes)
      this)

    (copyr [this buffer]
      (copyr this
             buffer
             nil))

    (copyr [this buffer offset]
      (copyr this
             buffer
             offset
             nil))

    (copyr [this buffer offset n-bytes]
      (let [offset-2  (clj/or offset 
                              0)
            n-bytes-2 (clj/or n-bytes
                              (- (count buffer)
                                 offset-2))]
        (copy (to-buffer this)
              (+ -offset
                 (position this))
              buffer
              offset-2
              n-bytes-2)
        (skip this
              n-bytes-2))
      this)

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

    (ra-string [this position n-bytes]
      (ra-string this
                 nil
                 position
                 n-bytes))

    (ra-string [this decoder position n-bytes]
      (.decode (clj/or decoder 
                       -text-decoder-utf-8)
               (js/Uint8Array. (.-buffer dataview)
                               (+ (.-byteOffset dataview)
                                  position)
                               n-bytes)))


  IAbsoluteWriter


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

    (rr-string [this n-bytes]
      (rr-string this
                 nil
                 n-bytes))

    (rr-string [this decoder n-bytes]
      (let [string (ra-string this
                              decoder
                              -position
                              n-bytes)]
        (skip this
              n-bytes)
        string))


  IRelativeWriter
    

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

    ;; [START] Copied from CLJ implementation

    (copya [this position buffer]
      (copya this
             position
             buffer
             nil))

    (copya [this position buffer offset]
      (copya this
             position
             buffer
             offset
             nil))

    (copya [this position buffer offset n-bytes]
      (copy (to-buffer this)
            (+ (.-byteOffset dataview)
               position)
            buffer
            offset
            n-bytes)
      this)

    (copyr [this buffer]
      (copyr this
             buffer
             nil))

    (copyr [this buffer offset]
      (copyr this
             buffer
             offset
             nil))

    (copyr [this buffer offset n-bytes]
      (let [offset-2  (clj/or offset 
                              0)
            n-bytes-2 (clj/or n-bytes
                              (- (count buffer)
                                 offset-2))]
        (copy (to-buffer this)
              (position this)
              buffer
              offset-2
              n-bytes-2)
        (skip this
              n-bytes-2))
      this)

    ;; [END] Copied from CLJ implementation

    (garanteed? [_ n-bytes]
      (>= (- (.-byteLength dataview)
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



#?(:cljs

(extend-type js/ArrayBuffer

  ICounted

    (-count [this]
      (.-byteLength this))


  ISeqable

    (-seq [this]
      (array-seq (js/Uint8Array. this)))))





(defprotocol ^:private -IGrowing

  ;;

  (-grow [this]
    ;;
    ))



(deftype GrowingView [next-size
                      #?@(:clj  [^:unsynchronized-mutable -view])
                      #?@(:cljs [^:mutable -view])]


  -IGrowing

  
    (-grow [this]
      (let [position-saved (position -view)
            buffer-new     (buffer (next-size (count -view)))]
        (copy buffer-new
              0
              (to-buffer -view))
        (set! -view
              (view buffer-new))
        (seek -view
              position-saved))
      this)


  #?(:clj  clojure.lang.Counted
     :cljs ICounted)


  #?(:clj  (count [_]
             (count -view))
     :cljs (-count [_]
             (count -view)))


  IAbsoluteReader


    (ra-u8 [_ position]
      (ra-u8 -view
             position))

    (ra-i8 [_ position]
      (ra-i8 -view
             position))

    (ra-u16 [_ position]
      (ra-u16 -view
              position))

    (ra-i16 [_ position]
      (ra-i16 -view
              position))

    (ra-u32 [_ position]
      (ra-u32 -view
              position))

    (ra-i32 [_ position]
      (ra-i32 -view
              position))

    (ra-i64 [_ position]
      (ra-i64 -view
              position))

    (ra-f32 [_ position]
      (ra-f32 -view
              position))

    (ra-f64 [_ position]
      (ra-f64 -view
              position))
    
    (ra-string [_ position n-bytes]
      (ra-string -view
                 position
                 n-bytes))

    (ra-string [_ decoder position n-bytes]
      (ra-string -view
                 decoder 
                 position
                 n-bytes))


  IAbsoluteWriter


    (wa-b8 [this position integer]
      (garantee this
                1)
      (wa-b8 -view
             position
             integer)
      this)

    (wa-b16 [this position integer]
      (garantee this
                2)
      (wa-b16 -view
             position
             integer)
      this)

    (wa-b32 [this position integer]
      (garantee this
                4)
      (wa-b32 -view
              position
              integer)
      this)

    (wa-b64 [this position integer]
      (garantee this
                8)
      (wa-b64 -view
              position
              integer)
      this)

    (wa-f32 [this position floating]
      (garantee this
                4)
      (wa-f32 -view
              position
              floating)
      this)

    (wa-f64 [this position floating]
      (garantee this
                8)
      (wa-f64 -view
              position
              floating)
      this)
    
    (wa-string [this given-position string]
      (let [position-saved (position -view)]
        (seek this
              given-position)
        (let [res (wr-string this
                             string)]
          (seek -view
                position-saved)
          res)))


  IEndianess

  
    (endianess [_]
      (endianess -view))

    (endianess [this new-endianess]
      (endianess -view
                 new-endianess)
      this)


  IGrowing

    (garantee [this n-bytes]
      (when-not (garanteed? -view
                            n-bytes)
        (let [position-saved (position -view)
              size-minimum   (+ position-saved
                                n-bytes)
              buffer-new     (buffer (loop [size-next (next-size (count -view))]
                                       (if (>= size-next
                                               size-minimum)
                                         size-next
                                         (recur (next-size size-next)))))]
          (copy buffer-new
                0
                (to-buffer -view))
          (set! -view
                (view buffer-new))
          (seek -view
                position-saved)))
      this)


  IRelativeReader


    (rr-u8 [_]
      (rr-u8 -view))

    (rr-i8 [_]
      (rr-i8 -view))

    (rr-u16 [_]
      (rr-u16 -view))

    (rr-i16 [_]
      (rr-i16 -view))

    (rr-u32 [_]
      (rr-u32 -view))

    (rr-i32 [_]
      (rr-i32 -view))

    (rr-i64 [_]
      (rr-i64 -view))

    (rr-f32 [_]
      (rr-f32 -view))

    (rr-f64 [_]
      (rr-f64 -view))
    
    (rr-string [_ n-bytes]
      (rr-string -view
                 n-bytes))

    (rr-string [_ decoder n-bytes]
      (rr-string -view
                 decoder
                 n-bytes))


  IRelativeWriter


    (wr-b8 [this integer]
      (garantee this
                1)
      (wr-b8 -view
            integer)
      this)

    (wr-b16 [this integer]
      (garantee this
                2)
      (wr-b16 -view
            integer)
      this)

    (wr-b32 [this integer]
      (garantee this
                4)
      (wr-b32 -view
             integer)
      this)

    (wr-b64 [this integer]
      (garantee this
                8)
      (wr-b64 -view
             integer)
      this)

    (wr-f32 [this floating]
      (garantee this
                4)
      (wr-f32 -view
              floating)
      this)

    (wr-f64 [this floating]
      (garantee this
                8)
      (wr-f64 -view
              floating)
      this)
  
    (wr-string [this string]
      (let [res (wr-string -view
                           string)]
        (if (res 0)
          res
          (loop [res-2         res
                 written-bytes (res 1)
                 written-chars (res 2)]
            (-grow this)
            (let [res-next (wr-string -view
                                      #?(:clj  (res-2 3)
                                         :cljs (.substring string
                                                           written-chars)))]
              (if (res-next 0)
                (-> res-next
                    (update 1
                            +
                            written-bytes)
                    (update 2
                            +
                            written-chars))
                (do
                  (recur res-next
                         (+ (res-next 1)
                            written-bytes)
                         (+ (res-next 2)
                            written-chars)))))))))


    IView

      (copya [this position buffer]
        (copya this
               position
               buffer
               nil))

      (copya [this position buffer offset]
        (copya this
               position
               buffer
               offset
               nil))

      (copya [this given-position buffer offset n-bytes]
        (let [offset-2  (clj/or offset 
                                0)
              n-bytes-2 (clj/or n-bytes
                                (- (count buffer)
                                   offset-2))]
          (garantee this
                    (- (+ given-position
                          n-bytes-2)
                       (position this)))
          (copy (to-buffer this)
                given-position
                buffer
                offset-2
                n-bytes-2))
        this)

      (copyr [this buffer]
        (copyr this
               buffer
               nil))

      (copyr [this buffer offset]
        (copyr this
               buffer
               offset
               nil))

      (copyr [this buffer offset n-bytes]
        (let [offset-2  (clj/or offset 
                                0)
              n-bytes-2 (clj/or n-bytes
                                (- (count buffer)
                                   offset-2))]
          (garantee this
                    n-bytes-2)
          (copy (to-buffer this)
                (position this)
                buffer
                offset-2
                n-bytes-2)
          (skip this
                n-bytes-2))
        this)

      (garanteed? [this n-bytes]
        (garantee this
                  n-bytes)
        true)

      (offset [_]
        0)

      (position [_]
        (position -view))

      (seek [this position-new]
        (let [position-old (position -view)]
          (when (> position-new
                   position-old)
            (garantee this
                      (- position-new
                         position-old))))
        (seek -view
              position-new))

      (skip [this n-bytes]
        (garantee this
                  n-bytes)
        (skip -view
              n-bytes))

      (to-buffer [_]
        (to-buffer -view))


    IViewBuilder

      (view [_]
        (view -view))

      (view [_ offset]
        (view -view
              offset))

      (view [_ offset size]
        (view -view
              offset
              size)))



(defn growing-view

  ""

  ([buffer]

   (growing-view buffer
                 nil))


  ([buffer next-size]

   (GrowingView. (if next-size
                   (fn next-size-2 [size-previous]
                     (let [size-next (next-size size-previous)]
                       (if (> size-next
                              size-previous)
                         size-next
                         (throw (ex-info "Must reallocate bigger buffer"
                                         {::error          :reallocation
                                          ::previous-sizes size-previous 
                                          ::next-size      size-next})))))
                   (fn default-reallocate [size-previous]
                     (Math/round (* 1.5
                                    size-previous))))
                 (view buffer))))


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
               (wa-b8 0
                      u8)
               (ra-i8 0))))



(defn u16

  ""

  ([i16]

   (and 0xffff
        i16))


  ([b8-1 b8-2]

   (u16 (or (<< b8-1
                8)
            b8-2))))



(defn i16

  ""

  ([u16]

   #?(:clj  (unchecked-short u16)
      :cljs (-> -conv-view
                (wa-b16 0
                        u16)
                (ra-i16 0))))


  ([b8-1 b8-2]

   (i16 (u16 b8-1
             b8-2))))



(defn u32

  ""

  ([i32]

   #?(:clj  (and 0xffffffff
                 i32)
      ;; Because bitwise operations in JS are 32 bits, bit-and'ing does not work in this case.
      :cljs (-> -conv-view
                (wa-b32 0
                        i32)
                (ra-u32 0))))

  ([b8-1 b8-2 b8-3 b8-4]

   (u32 (or (<< b8-1
                24)
            (<< b8-2
                16)
            (<< b8-3
                8)
            b8-4))))



(defn i32

  ""

  ([u32]

   #?(:clj  (unchecked-int u32)
      :cljs (-> -conv-view
                (wa-b32 0
                        u32)
                (ra-i32 0))))

  ([b8-1 b8-2 b8-3 b8-4]

   (i32 (u32 b8-1
             b8-2
             b8-3
             b8-4))))



(defn i64

  ""

  [b8-1 b8-2 b8-3 b8-4 b8-5 b8-6 b8-7 b8-8]

  (or (<< b8-1
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
      b8-8))



(defn f32

  ""

  ([integer]

   #?(:clj  (Float/intBitsToFloat integer)
      :cljs (-> -conv-view
                (wa-b32 0
                        integer)
                (ra-f32 0))))


  ([b8-1 b8-2 b8-3 b8-4]

   (f32 (u32 b8-1
             b8-2
             b8-3
             b8-4))))



(defn f64

  ""

  ([integer]

   #?(:clj  (Double/longBitsToDouble integer)
      :cljs (-> -conv-view
                (wa-b64 0
                        integer)
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



(defn remaining

  ""

  [view]

  (- (count view)
     (position view)))
