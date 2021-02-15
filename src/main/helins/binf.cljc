(ns helins.binf

  "Uninhibited library for handling any kind binary format or protocol.
  
   See README for an overview."

  {:author "Adam Helins"}
  (:require [clojure.core :as clj]
            #?(:cljs [goog.crypt.base64]))
  #?(:cljs (:require-macros [helins.binf]))
  #?(:clj (:import clojure.lang.Counted
                   (java.nio ByteBuffer
                             ByteOrder
                             CharBuffer)
                   (java.nio.charset Charset
                                     CharsetEncoder
                                     CoderResult
                                     StandardCharsets)
                   java.util.Base64))
  (:refer-clojure :rename {bit-shift-left           <<
                           bit-shift-right          >>
                           unsigned-bit-shift-right >>>}))


(declare buffer
         copy-buffer
         i8
         i16
         i32
         text-decoder
         u8
         u16
         u32)


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


;;;;;;;;;; Miscellaneous


(defn array-endianess

  ""

  []

  #?(:clj  :big-endian
     :cljs (let [b16 (js/Uint32Array. 1)
                 b8  (js/Uint8Array. (.-buffer b16))]
             (aset b16
                   0
                   0xaa)
             (if (= (aget b8
                          0)
                    0xaa)
               :little-endian
               :big-endian))))


;;;;;;;;;; Protocols


(defprotocol IAbsoluteReader

  "Reading primitive values at an absolute position, without disturbing the current one."
  
  (ra-buffer [view position n-byte]
             [view position n-byte buffer]
             [view position n-byte buffer offset]
    "Reads `n-byte` bytes from an absolute `position` and returns them in a new buffer or in the
     given one at the specified `offset` (or 0).")

  (ra-u8 [view position]
    "Reads an unsigned 8-bit integer from an absolute `position`.")

  (ra-i8 [view position]
    "Reads a signed 8-bit integer from an absolute `position`.")

  (ra-u16 [view position]
    "Reads an unsigned 16-bit integer from an absolute `position`.")

  (ra-i16 [view position]
    "Reads a signed 16-bit integer from an absolute `position`.")

  (ra-u32 [view position]
    "Reads an unsigned 32-bit integer from an absolute `position`.")

  (ra-i32 [view position]
    "Reads a signed 32-bit integer from an absolute `position`.")

  (ra-i64 [view position]
    "Reads a signed 64-bit integer from an absolute `position`.")

  (ra-f32 [view position]
    "Reads a 32-bit float at from absolute `position`.")

  (ra-f64 [view position]
    "Reads a 64-bit float at from absolute `position`.")
  
  (ra-string [view position n-byte]
             [view decoder position n-byte]
    "Reads a string consisting of `n-byte` bytes from an absolute `position`.
    
     A decoder may be provided (default is UTF-8).
    
     Cf. [[text-decoder]]"))


(defprotocol IAbsoluteWriter

  "Writing primitive values at an absolute position, without disturbing the current one.
  
   When writing integers, sign is irrelevant and truncation is automatic."
  
  (wa-buffer [view position buffer]
             [view position buffer offset]
             [view position buffer offset n-byte]
    "Copies the given `buffer` to an absolute `position`.
    
     An `offset` in the buffer as well as a number of bytes to copy (`n-byte`) may be provided.")

  (wa-b8 [view position integer]
    "Writes an 8-bit integer to an absolute position.")

  (wa-b16 [view position integer]
    "Writes a 16-bit integer to an absolute `position`.")

  (wa-b32 [view position integer]
    "Writes a 32-bit integer to an absolute `position`.")

  (wa-b64 [view position integer]
    "Writes a 64-bit integer to an absolute `position`.")

  (wa-f32 [view position floating]
    "Writes a 32-bit float to an absolute `position`.")

  (wa-f64 [view position floating]
    "Writes a 64-bit float to an absolute `position`.")
  
  (wa-string [view position string]
    "Writes a string (encoded as UTF-8) to an absolute `position`.

     Unlike other functions which are implemented as a fluent interface, this function returns
     a tuple indicating how many bytes and chars have been written, and if the process is finished:
     `[finished? n-byte n-chars]`.
    
     With that information, the user can continue writing if needed. On the JVM, the tuple contains a 4th
     item which is a `CharBuffer` containing the rest of the unwritten string which can be passed in place
     of the `string` argument.
    
     Growing views will automatically grow and only one call will be sufficient."))


(defprotocol IEndianess

  "Retrieving or modifying the endianess."
  
  (endianess [view]
             [view new-endianess]
    "Arity 1 returns the current endianess, arity 2 sets it.
    
     Accepted values are `:little-endian` and `:big-endian`."))


(defprotocol IGrowing

  "Only applicable to `growing views` which are capable of growing size dynamically.
  
   See [[growing-view]]."

  (garantee [growing-view n-byte]
    "Garantees that at least `n-byte` bytes can be written.
    
     The growing view will automatically grow its size if needed.
    
     This function is rather a lower-level one, the common user will probably never use it directly."))


(defprotocol IRelativeReader

  "Reading primitive values from the current position, advancing it as needed. For instance,
   reading a 32-bit integer will advance the current position by 4 bytes."

  (rr-buffer [view n-byte]
             [view n-byte buffer]
             [view n-byte buffer offset]
    "Reads n-byte and returns them in a new buffer or in the given one at the specified `offset` (or 0).")

  (rr-u8 [view]
    "Reads an unsigned 8-bit integer from the current position.")

  (rr-i8 [view]
    "Reads a signed 8-bit integer from the current position.")

  (rr-u16 [view]
    "Reads an unsigned 16-bit integer from the current position.")

  (rr-i16 [view]
    "Reads a signed 16-bit integer from the current position.")

  (rr-u32 [view]
    "Reads an unsigned 32-bit integer from the current position.")

  (rr-i32 [view]
    "Reads a signed 32-bit integer from the current position.")

  (rr-i64 [view]
    "Reads a signed 64-bit integer from the current position.")

  (rr-f32 [view]
    "Reads a 32-bit float from the current position.")

  (rr-f64 [view]
    "Reads a 64-bit float from the current position.")
  
  (rr-string [view n-byte]
             [view decoder n-byte]
    "Reads a string consisting of `n-byte` from the current position.

     A decoder may be provided (default is UTF-8).
    
     See [[text-decoder]]"))


(defprotocol IRelativeWriter

  "Writing primitive values to the current position, advancing it as needed. For instance,
   reading a 64-bit float will advance the current position by 8 bytes.

   When writing integers, sign is irrelevant and truncation is automatic."

  (wr-buffer [view buffer]
             [view buffer offset]
             [view buffer offset n-byte]
    "Copies the given `buffer` to the current position.

     An `offset` in the buffer as well as a number of bytes to copy (`n-byte`) may be provided.")
  
  (wr-b8 [view integer]
    "Writes an 8-bit integer to the current position.")

  (wr-b16 [view integer]
    "Writes a 16-bit integer to the current position.")

  (wr-b32 [view integer]
    "Writes a 32-bit integer to the current position.")

  (wr-b64 [view integer]
    "Writes a 64-bit integer to the current position.")

  (wr-f32 [view floating]
    "Writes a 32-bit float to the current position.")

  (wr-f64 [view floating]
    "Writes a 64-bit float to the current position.")

  (wr-string [view string]
    "Writes a string to the current position, encoded at UTF-8.
    
     Cf. [[wa-string]] about the returned value"))


(defprotocol IView

  "Additional functions related to views (growing ones as well)."

  (garanteed? [view n-byte]
    "Is it possible to write at least `n-byte` bytes?
    
     Growing views always return true since they can grow automatically.")

  (offset [view]
    "Returns the offset in the original buffer this view starts from.
    
     Views can be counted using Clojure's `count` which expresses the number of bytes wrapped by the view
     starting from the offset.")

  (position [view]
    "Returns the current position.")

  (seek [view position]
    "Modifies the current position.")
  
  (skip [view n-byte]
    "Advances the current position by `n-byte` bytes.")

  (to-buffer [view]
    "Returns the buffer wrapped by the view.
    
     Also see [[offset]]."))


(defprotocol IViewBuilder

  "Building a new view."

  (view [viewable]
        [viewable offset]
        [viewable offset n-byte]
    "A view can be created from a buffer (see [[buffer]]) or from another view.
    
     An `offset` as well as a size (`n-byte`) may be provided.
    
     ```clojure
     (def my-buffer
          (binf/buffer 100))

     ;; View with an offset of 50 bytes, 40 bytes long
     (def my-view
          (binf/view my-buffer
                     50
                     40))

     ;; View from a view, offset of 60 bytes in the original buffer (50 + 10), 20 bytes long
     (def inner-view
          (binf/view my-view
                     10
                     20))
     ```"))


;;;;;;;;;; Encoding and decoding strings


#?(:clj
   
(def ^Charset -charset-utf-8

  ;; UTF charset on the JVM.

  StandardCharsets/UTF_8))



(def ^:private -text-decoder-utf-8

  ;; Crossplatform UTF-8 decoding.

  #?(:clj  -charset-utf-8
     :cljs (js/TextDecoder. "utf-8")))



(def ^:private -text-decoders

  ;; Available decoders by encoding.

  {:iso-8859-1 #?(:clj  StandardCharsets/ISO_8859_1
                  :cljs (js/TextDecoder. "iso-8859-1")) 
   :utf-8      -text-decoder-utf-8
   :utf-16-be  #?(:clj  StandardCharsets/UTF_16BE
                  :cljs (js/TextDecoder. "utf-16be"))
   :utf-16-le  #?(:clj  StandardCharsets/UTF_16LE
                  :cljs (js/TextDecoder. "utf-16le"))})



(defn text-decode

  "Interprets the given `buffer` as a string.
  
   A decoder may be provided (see [[text-decoder]])."

  ([buffer]

   (text-decode (text-decoder)
                buffer))


  ([text-decoder buffer]

   #?(:clj  (String. ^bytes buffer
                     ^Charset text-decoder)
      :cljs (.decode text-decoder
                     buffer))))



(defn text-decoder

  "Some functions accepts a text decoder.
  
   Available encodings are: `:iso-8859-1`, `:utf-8`, `:utf-16-be`, `:utf-16-le`
  
   Default is UTF-8 but argument is non-nilable."

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

  ;; UTF-8 encoder.

  (js/TextEncoder.)))



(defn text-encode

  "Returns a buffer containing the given `string` encoded in UTF-8."

  [string]

  #?(:clj  (.getBytes ^String string
                      -charset-utf-8)
     :cljs (.-buffer (.encode -text-encoder
                              string))))


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



(defprotocol ^:private -IGrowing

  ;; Private protocol for growing views.

  (-grow [this]
    ;; Grows the size of the growing view.
    ))



(deftype GrowingView [next-size
                      #?@(:clj  [^:unsynchronized-mutable -view])
                      #?@(:cljs [^:mutable -view])]


  -IGrowing

  
    (-grow [this]
      (let [position-saved (position -view)
            buffer-new     (buffer (next-size (count -view)))]
        (copy-buffer buffer-new
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

    (ra-buffer [this position-given n-byte buffer offset]
      (ra-buffer -view
                 position-given
                 n-byte
                 buffer
                 offset))

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
    
    (ra-string [_ position n-byte]
      (ra-string -view
                 position
                 n-byte))

    (ra-string [_ decoder position n-byte]
      (ra-string -view
                 decoder 
                 position
                 n-byte))


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

    (wa-buffer [this position-given buffer offset n-byte]
      (garantee this
                (- (+ position-given
                      n-byte)
                   (position this)))
      (wa-buffer -view
                 position-given
                 buffer
                 offset
                 n-byte)
      this)


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

    (garantee [this n-byte]
      (when-not (garanteed? -view
                            n-byte)
        (let [position-saved (position -view)
              size-minimum   (+ position-saved
                                n-byte)
              buffer-new     (buffer (loop [size-next (next-size (count -view))]
                                       (if (>= size-next
                                               size-minimum)
                                         size-next
                                         (recur (next-size size-next)))))]
          (copy-buffer buffer-new
                       0
                       (to-buffer -view))
          (set! -view
                (view buffer-new))
          (seek -view
                position-saved)))
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
      (rr-buffer -view
                 n-byte
                 buffer
                 offset))

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
    
    (rr-string [_ n-byte]
      (rr-string -view
                 n-byte))

    (rr-string [_ decoder n-byte]
      (rr-string -view
                 decoder
                 n-byte))


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
      (garantee this
                n-byte)
      (wr-buffer -view
                 buffer
                 offset
                 n-byte)
      this)

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
                (recur res-next
                       (+ (res-next 1)
                          written-bytes)
                       (+ (res-next 2)
                          written-chars))))))))


    IView


      (garanteed? [this n-byte]
        (garantee this
                  n-byte)
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

      (skip [this n-byte]
        (garantee this
                  n-byte)
        (skip -view
              n-byte))

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

  "Creates a growing view, starting with the given buffer.
  
   A growing view will reallocate a bigger buffer everytime it reaches the end of the current one, but ONLY WHEN WRITING.
  
   This is a simple strategy for when the size is unknown in advance, but it has proven to be rather optimal
   and ultimately efficient for the most common use cases. Even more so if the size can be roughly estimated.
  
   The size of the bigger buffer is decided by calling `next-size` which is a function `old-size` -> `new-size`.
   Thus the user can be in full control of the process. When not provided, size is always multiplied by 1.5."

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
                                         {::error         :reallocation
                                          ::previous-size size-previous 
                                          ::next-size     size-next})))))
                   (fn default-reallocate [size-previous]
                     (Math/round (* 1.5
                                    size-previous))))
                 (view buffer))))


;;;;;;;;;; Creating primitives from bytes


#?(:cljs (def ^:private -conv-view (view (buffer 8))))



(defn u8

  "Casts the given `bits` (any integer) into a 8-bit unsigned integer."

  [integer]

  (bit-and 0xff
           integer))



(defn i8

  "Casts the given `bits` (any integer) into a 8-bit signed integer."

  [u8]

  #?(:clj  (unchecked-byte u8)
     :cljs (-> -conv-view
               (wa-b8 0
                      u8)
               (ra-i8 0))))



(defn u16

  "Casts the given `bits` (any integer) into a 16-bit unsigned integer.
  
   Or recombines the given bytes in big endian order."

  ([bits]

   (bit-and 0xffff
            bits))


  ([b8-1 b8-2]

   (u16 (bit-or (<< b8-1
                    8)
                b8-2))))



(defn i16

  "Casts the given `bits` (any integer) into a 16-bit signed integer.
  
   Or recombines the given bytes in big endian order."

  ([bits]

   #?(:clj  (unchecked-short bits)
      :cljs (-> -conv-view
                (wa-b16 0
                        bits)
                (ra-i16 0))))


  ([b8-1 b8-2]

   (i16 (u16 b8-1
             b8-2))))



(defn u32

  "Casts the given `bits` (any integer) into a 32-bit unsigned integer.
  
   Or recombines the given bytes in big endian order."

  ([bits]

   #?(:clj  (bit-and 0xffffffff
                     bits)
      ;; Because bitwise operations in JS are 32 bits, bit-and'ing does not work in this case.
      :cljs (-> -conv-view
                (wa-b32 0
                        bits)
                (ra-u32 0))))

  ([b8-1 b8-2 b8-3 b8-4]

   (u32 (bit-or (<< b8-1
                    24)
                (<< b8-2
                    16)
                (<< b8-3
                    8)
                b8-4))))



(defn i32

  "Casts the given `bits` (any integer) into a 32-bit signed integer.
  
   Or recombines the given bytes in big endian order."

  ([bits]

   #?(:clj  (unchecked-int bits)
      :cljs (-> -conv-view
                (wa-b32 0
                        bits)
                (ra-i32 0))))

  ([b8-1 b8-2 b8-3 b8-4]

   (i32 (u32 b8-1
             b8-2
             b8-3
             b8-4))))



(defn i64

  "Recombines the given byets in big endian order to form a 64-bit signed integer."

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
          b8-8))



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

  "Truncates a float value to 64-bit unsigned integer (eg. `42.0` to `42`)."

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



(defn growing?

  "Returns true if `x` is a growing view instead of a fixed one.
  
   See [[growing-view]]."

  [x]

  (satisfies? IGrowing
              x))



(defn remaining

  "Returns the number of bytes remaining until the end of the view.
  
   In the context of a growing view, this value means the number of bytes before hitting the end of the currently
   allocated buffer, which matters for reading. When writing, this value indicates the number of bytes remaining
   before reallocating a bigger buffer. See [[growing-view]]."

  [view]

  (- (count view)
     (position view)))


;;;;;;;;;; Base64 utilities


(defn base64-decode

  "Decodes a string into a [[buffer]] according to the Base64 basic scheme (RFC 4648 section 4)"

  [string]

  #?(:clj  (.decode (Base64/getDecoder)
                    ^String string)
     :cljs (.-buffer (goog.crypt.base64/decodeStringToUint8Array string))))



(defn base64-encode

  "Encodes a [[buffer]] into a string according to the Base64 basic scheme (RFC 4648 section 4)"

  ([buffer]

   #?(:clj  (.encodeToString (Base64/getEncoder)
                             buffer)
      :cljs (goog.crypt.base64/encodeByteArray (js/Uint8Array. buffer))))


  ([buffer offset]

   #?(:clj  (base64-encode buffer
                           offset
                           (- (count buffer)
                              offset))
      :cljs (goog.crypt.base64/encodeByteArray (js/Uint8Array. buffer
                                                               offset))))


  ([buffer offset n-byte]

   #?(:clj  (String. (.array (.encode (Base64/getEncoder)
                                      (ByteBuffer/wrap buffer
                                                       offset
                                                       n-byte))))
      :cljs (goog.crypt.base64/encodeByteArray (js/Uint8Array. buffer
                                                               offset
                                                               n-byte)))))
