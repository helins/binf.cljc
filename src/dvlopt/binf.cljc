(ns dvlopt.binf

  "Uninhibited library for any binary protocols.
  
   See README for an overview."

  {:author "Adam Helinski"}
  (:require [clojure.core :as clj])
  #?(:cljs (:require-macros [dvlopt.binf]))
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

  "Alias for `bit-shift-left`."

  bit-shift-left)



(def ^{:arglists '([x n])}
 
  >>

  "Alias for `bit-shift-right`."

  bit-shift-right)



(def ^{:arglists '([x n])}
 
  >>>

  "Alias for `unsigned-bit-shift-right`.'"

  unsigned-bit-shift-right)



(def ^{:arglists '([x y]
                   [x y & more])}
      
  and

  "Alias for `bit-and`."

  bit-and)



(def ^{:arglists '([x y]
                   [x y & more])}

  or

  "Alias for `bit-or`."

  bit-or)


(def ^{:arglist '([x y]
                  [x y & more])}

  xor

  "Alias for `bit-xor`."

  bit-xor)


(def ^{:arglists '([x])}

  !
  
  "Alias for `bit-not`."

  bit-not)


;;;;;;;;;; Protocols


(defprotocol IAbsoluteReader

  "Reading primitive values at an absolute position, without disturbing the current one."
  
  (ra-u8 [this position]
    "Reads an unsigned 8-bit interger from an absolute `position`.")

  (ra-i8 [this position]
    "Reads a signed 8-bit integer from an absolute `position`.")

  (ra-u16 [this position]
    "Reads an unsigned 16-bit integer from an absolute `position`.")

  (ra-i16 [this position]
    "Reads a signed 16-bit integer from an absolute `position`.")

  (ra-u32 [this position]
    "Reads an unsigned 32-bit integer from an absolute `position`.")

  (ra-i32 [this position]
    "Reads a signed 32-bit integer from an absolute `position`.")

  (ra-i64 [this position]
    "Reads a signed 64-bit integer from an absolute `position`.")

  (ra-f32 [this position]
    "Reads a 32-bit float at from absolute `position`.")

  (ra-f64 [this position]
    "Reads a 64-bit float at from absolute `position`.")
  
  (ra-string [this position n-bytes]
             [this decoder position n-bytes]
    "Reads a string consisting of `n-bytes` bytes from an absolute `position`.
    
     A decoder may be provided (default is UTF-8).
    
     Cf. [[text-decoder]]"))


(defprotocol IAbsoluteWriter

  "Writing primitive values at an absolute position, without disturbing the current one.
  
   When writing integers, sign is irrelevant and truncation is automatic."
  
  (wa-b8 [this position integer]
    "Writes an 8-bit integer to an absolute position.")

  (wa-b16 [this position integer]
    "Writes a 16-bit integer to an absolute `position`.")

  (wa-b32 [this position integer]
    "Writes a 32-bit integer to an absolute `position`.")

  (wa-b64 [this position integer]
    "Writes a 64-bit integer to an absolute `position`.")

  (wa-f32 [this position floating]
    "Writes a 32-bit float to an absolute `position`.")

  (wa-f64 [this position floating]
    "Writes a 64-bit float to an absolute `position`.")
  
  (wa-string [this position string]
    "Writes a string to an absolute `position`, encoded as UTF-8.
    
     Unlike other functions which are implemented as a fluent interface, this function returns
     a tuple indicating how many bytes and chars have been written, and if the process is finished:
     `[finished? n-bytes n-chars]`.
    
     With that information, the user can continue writing if needed. On the JVM, the tuple contains a 4th
     item which is a `CharBuffer` containing the rest of the unwritten string which can be passed in place
     of the `string` argument.
    
     Growing views will automatically grow and only one call will be sufficient."))


(defprotocol IEndianess

  "Retrieving or modifying the endianess."
  
  (endianess [this]
             [this new-endianess]
    "Arity 1 returns the current endianess, arity 2 sets it.
    
     Accepted values are `:little-endian` and `:big-endian`."))


(defprotocol IGrowing

  "Only applicable to `growing views` which are capable of growing size dynamically.
  
   Cf. [[growing-view]]"

  (garantee [this n-bytes]
    "Garantees that at least `n-bytes` bytes can be written.
    
     The growing view will automatically grow its size if needed.
    
     This function is rather a lower-level one, the common user will probably never use it directly."))


(defprotocol IRelativeReader

  "Reading primitive values from the current position, advancing it as needed. For instance,
   reading a 32-bit integer will advance the current position by 4 bytes."

  (rr-u8 [this]
    "Reads an unsigned 8-bit integer from the current position.")

  (rr-i8 [this]
    "Reads a signed 8-bit integer from the current position.")

  (rr-u16 [this]
    "Reads an unsigned 16-bit integer from the current position.")

  (rr-i16 [this]
    "Reads a signed 16-bit integer from the current position.")

  (rr-u32 [this]
    "Reads an unsigned 32-bit integer from the current position.")

  (rr-i32 [this]
    "Reads a signed 32-bit integer from the current position.")

  (rr-i64 [this]
    "Reads a signed 64-bit integer from the current position.")

  (rr-f32 [this]
    "Reads a 32-bit float from the current position.")

  (rr-f64 [this]
    "Reads a 64-bit float from the current position.")
  
  (rr-string [this n-bytes]
             [this decoder n-bytes]
    "Reads a string consisting of `n-bytes` from the current position.

     A decoder may be provided (default is UTF-8).
    
     Cf. [[text-decoder]]"))


(defprotocol IRelativeWriter

  "Writing primitive values to the current position, advancing it as needed. For instance,
   reading a 64-bit float will advance the current position by 8 bytes.

   When writing integers, sign is irrelevant and truncation is automatic."

  (wr-b8 [this integer]
    "Writes an 8-bit integer to the current position.")

  (wr-b16 [this integer]
    "Writes a 16-bit integer to the current position.")

  (wr-b32 [this integer]
    "Writes a 32-bit integer to the current position.")

  (wr-b64 [this integer]
    "Writes a 64-bit integer to the current position.")

  (wr-f32 [this floating]
    "Writes a 32-bit float to the current position.")

  (wr-f64 [this floating]
    "Writes a 64-bit float to the current position.")

  (wr-string [this string]
    "Writes a string to the current position, encoded at UTF-8.
    
     Cf. [[wa-string]] about the returned value"))


(defprotocol IView

  "Additional functions related to views (growing ones as well)."

  (copya [this position buffer]
         [this position buffer offset]
         [this position buffer offset n-bytes]
    "Copies the given `buffer` to an absolute `position`.
    
     An `offset` in the buffer as well as a number of bytes to copy (`n-bytes`) may be provided.")

  (copyr [this buffer]
         [this buffer offset]
         [this buffer offset n-bytes]
    "Copies the given `buffer` to the current position.

     An `offset` in the buffer as well as a number of bytes to copy (`n-bytes`) may be provided.")

  (garanteed? [this n-bytes]
    "Is it possible to write at least `n-bytes` bytes?
    
     Growing views always return true.")

  (offset [this]
    "Returns the offset in the original buffer (returned by [[to-buffer]]).")

  (position [this]

    "Current the current position.")

  (seek [this position]
    "Modifies the current position.")
  
  (skip [this n-bytes]
    "Skips `n-bytes` bytes.")

  (to-buffer [this]
    "Returns the buffer wrapped by the view."))


(defprotocol IViewBuilder

  "Building a new view."

  (view [this]
        [this offset]
        [this offset n-bytes]
    "A view can be created from a buffer (see [[buffer]]) or from another view.
    
     An `offset` as well as a size (`n-bytes`) may be provided.
    
     ```clojure
     (def my-buffer
          (binf/buffer 100))

     ;; View with an offset of 50 bytes, 40 bytes long
     (def my-view
          (binf/view my-buffer
                     50
                     40))

     ;; View from a view, offset of 60 bytes from the original buffer, 20 bytes long
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
  
   Default is UTF-8, but argument is non-nilable."

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

  "Creates a new buffer having `n-bytes` bytes.
  
   In JS, corresponds to an `ArrayBuffer`.
   On the JVM, corresponds to a plain byte array.
  
   In order to do anything interesting with this library, it needs to be wrapped in a [[view]]."

  [n-bytes]

  #?(:clj  (byte-array n-bytes)
     :cljs (js/ArrayBuffer. n-bytes)))



#?(:clj

(defmacro buffer*

  "Creates a new buffers from the given byte values.
  
   Cf. [[buffer]]"

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



#?(:cljs

(extend-protocol IViewBuilder

  js/ArrayBuffer

    (view

      ([this]
       (View. (js/DataView. this)
              false
              0))

      ([this offset]
       (View. (js/DataView. this
                            offset)
              false
              0))

      ([this offset size]
       (View. (js/DataView. this
                            offset
                            size)
              false
              0)))))



#?(:cljs

(extend-type js/ArrayBuffer

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

  "Creates a growing view, starting with the given buffer.
  
   A growing view will reallocate a bigger buffer everytime it reaches the end of the current one.
  
   This is a simple strategy for when the size is unknown in advance, but it has proven to be rather optimal
   and ultimately efficient for the most common use cases. Even more so it the size can be roughly estimated.
  
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

  "Converts an integer to an unsigned 8-bit integer."

  [integer]

  (and 0xff
       integer))



(defn i8

  "Converts an unsigned 8-bit integer to signed one."

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

  "Converts a float value to an integer (eg. `42.0` to `42`)."

  [floating]

  (long floating))



(defn bits-f32

  "Converts a 32-bit float to an integer preserving the bit pattern."

  [f32]

  #?(:clj  (Float/floatToIntBits f32)
     :cljs (-> -conv-view
               (wa-f32 0
                       f32)
               (ra-u32 0))))



(defn bits-f64

  "Converts a 64-bit float to an integer preserving the bit pattern."

  [f64]

  #?(:clj  (Double/doubleToLongBits f64)
     :cljs (-> -conv-view
               (wa-f64 0
                       f64)
               (ra-i64 0))))


;;;;;;;;;; Copying and miscellaneous


(defn copy

  "Copies a buffer to another buffer."

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

  "Returns the number of bytes remaining until the end of the view is reached.
  
   In the context of a growing view, this value means the number of byte that can be written before
   reallocating a bigger buffer."

  [view]

  (- (count view)
     (position view)))
