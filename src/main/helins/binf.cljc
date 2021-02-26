;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns helins.binf

  "Uninhibited library for handling any kind of binary format or protocol.
  
   See README for an overview."

  {:author "Adam Helins"}

  (:require [helins.binf.buffer         :as binf.buffer]
            [helins.binf.int            :as binf.int]
            [helins.binf.int64          :as binf.int64]
            [helins.binf.protocol       :as binf.protocol]
            [helins.binf.protocol.impl]
            [helins.binf.string         :as binf.string])
  (:refer-clojure :rename {bit-shift-left           <<
                           bit-shift-right          >>
                           unsigned-bit-shift-right >>>}))


(declare remaining)


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


;;;;;;;;;; Pointing to protocol implementations


;;;;; IAbsoluteReader


(defn ra-buffer

  "Reads `n-byte` bytes from an absolute `position` and returns them in a new buffer or in the
   given `buffer` at the specified `offset` (or 0)."


  ([this position n-byte]

   (binf.protocol/ra-buffer this
                            position
                            n-byte
                            (binf.buffer/alloc n-byte)
                            0))


  ([this position n-byte buffer]

   (binf.protocol/ra-buffer this
                            position
                            n-byte
                            buffer
                            0))


  ([view position n-byte buffer offset]

   (binf.protocol/ra-buffer view
                            position
                            n-byte
                            buffer
                            offset)))



(defn ra-u8
  
  "Reads an unsigned 8-bit integer from an absolute `position`."

  [view position]

  (binf.protocol/ra-u8 view
                       position))



(defn ra-i8
  
  "Reads a signed 8-bit integer from an absolute `position`."

  [view position]

  (binf.protocol/ra-i8 view
                       position))



(defn ra-u16

  "Reads an unsigned 16-bit integer from an absolute `position`."

  [view position]

  (binf.protocol/ra-u16 view
                        position))



(defn ra-i16

  "Reads a signed 16-bit integer from an absolute `position`."

  [view position]

  (binf.protocol/ra-i16 view
                        position))



(defn ra-u32

  "Reads an unsigned 32-bit integer from an absolute `position`."

  [view position]

  (binf.protocol/ra-u32 view
                        position))



(defn ra-i32

  "Reads a signed 32-bit integer from an absolute `position`."

  [view position]

  (binf.protocol/ra-i32 view
                        position))


(defn ra-u64

  "Reads an unsigned 64-bit integer from an absolute `position`."

  [view position]

  (binf.protocol/ra-u64 view
                        position))



(defn ra-i64

  "Reads a signed 64-bit integer from an absolute `position`."

  [view position]

  (binf.protocol/ra-i64 view
                        position))



(defn ra-f32

  "Reads a 32-bit float at from absolute `position`."

  [view position]

  (binf.protocol/ra-f32 view
                        position))



(defn ra-f64

  "Reads a 64-bit float at from absolute `position`."

  [view position]

  (binf.protocol/ra-f64 view
                        position))



(defn ra-string

  "Reads a string consisting of `n-byte` bytes from an absolute `position`.
  
   A decoder may be provided (default is UTF-8).
  
   Cf. [[text-decoder]]"


  ([view position n-byte]

   (binf.protocol/ra-string view
                            binf.string/decoder-utf-8
                            position
                            n-byte))


  ([view decoder position n-byte]

   (binf.protocol/ra-string view
                            decoder
                            position
                            n-byte)))


;;;;; IAbsoluteWriter


(defn wa-buffer

  "Copies the given `buffer` to an absolute `position`.
  
   An `offset` in the buffer as well as a number of bytes to copy (`n-byte`) may be provided."


  ([view position buffer]

    (binf.protocol/wa-buffer view
                             position
                             buffer
                             0
                             (count buffer)))


  ([view position buffer offset]

   (binf.protocol/wa-buffer view
                            position
                            buffer
                            offset
                            (- (count buffer)
                               offset)))


  ([view position buffer offset n-byte]

   (binf.protocol/wa-buffer view
                            position
                            buffer
                            offset
                            n-byte)))



(defn wa-b8

  "Writes an 8-bit integer to an absolute position."

  [view position int]

  (binf.protocol/wa-b8 view
                       position
                       int))



(defn wa-b16

  "Writes a 16-bit integer to an absolute `position`."

  [view position int]

  (binf.protocol/wa-b16 view
                        position
                        int))



(defn wa-b32

  "Writes a 32-bit integer to an absolute `position`."

  [view position int]

  (binf.protocol/wa-b32 view
                        position
                        int))



(defn wa-b64

  "Writes a 64-bit integer to an absolute `position`."

  [view position int64]

  (binf.protocol/wa-b64 view
                        position
                        int64))



(defn wa-f32

  "Writes a 32-bit float to an absolute `position`."

  [view position floating]

  (binf.protocol/wa-f32 view
                        position
                        floating))



(defn wa-f64

  "Writes a 64-bit float to an absolute `position`."

  [view position floating]

  (binf.protocol/wa-f64 view
                        position
                        floating))



(defn wa-string
  
  "Writes a string (encoded as UTF-8) to an absolute `position`.

   Unlike other  `wa-*` functions which are implemented as a fluent interface, this function returns
   a tuple indicating how many bytes and chars have been written, and if the process is finished:
   `[finished? n-byte n-chars]`.
  
   With that information, the user can continue writing if needed. On the JVM, the tuple contains a 4th
   item which is a `CharBuffer` containing the rest of the unwritten string which can be passed in place
   of the `string` argument.
  
   Growing views will automatically grow and only one call will be sufficient."

  [view position string]

  (binf.protocol/wa-string view
                           position
                           string))


;;;;; IBackingBuffer


(defn backing-buffer

  ""

  [view]

  (binf.protocol/backing-buffer view))



(defn buffer-offset

  ""

  [view]

  (binf.protocol/buffer-offset view))


;;;;; IEndianess


(defn endian-get

  "Returns the endianess of the given `view`, either `:big-endian` or `:little-endian`."

  [view]

  (binf.protocol/endian-get view))



(defn endian-set

  "Sets the endianess of the given `view`, either `:big-endian` or `:little-endian`."

  [view endianess]

  (binf.protocol/endian-set view
                            endianess))


;;;;; IGrow


(defn grow

  ""

  [view n-additional-byte]

  (binf.protocol/grow view
                      n-additional-byte))


;;;;; IRelativeReader


(defn rr-buffer

  "Reads n-byte and returns them in a new buffer or in the given one at the specified `offset` (or 0)."


  ([view n-byte]

   (binf.protocol/rr-buffer view
                            n-byte
                            (binf.buffer/alloc n-byte)
                            0))


  ([view n-byte buffer]

   (binf.protocol/rr-buffer view
                            n-byte
                            buffer
                            0))


  ([view n-byte buffer offset]

   (binf.protocol/rr-buffer view
                            n-byte
                            buffer
                            offset)))



(defn rr-u8

  "Reads an unsigned 8-bit integer from the current position."

  [view]

  (binf.protocol/rr-u8 view))



(defn rr-i8

  "Reads a signed 8-bit integer from the current position."

  [view]

  (binf.protocol/rr-i8 view))



(defn rr-u16

  "Reads an unsigned 16-bit integer from the current position."

  [view]

  (binf.protocol/rr-u16 view))



(defn rr-i16

  "Reads a signed 16-bit integer from the current position."

  [view]

  (binf.protocol/rr-i16 view))



(defn rr-u32

  "Reads an unsigned 32-bit integer from the current position."

  [view]

  (binf.protocol/rr-u32 view))



(defn rr-i32

  "Reads a signed 32-bit integer from the current position."

  [view]

  (binf.protocol/rr-i32 view))



(defn rr-u64

  "Reads an unsigned 64-bit integer from the current position."

  [view]

  (binf.protocol/rr-u64 view))



(defn rr-i64

  "Reads a signed 64-bit integer from the current position."

  [view]

  (binf.protocol/rr-i64 view))



(defn rr-f32

  "Reads a 32-bit float from the current position."

  [view]

  (binf.protocol/rr-f32 view))



(defn rr-f64

  "Reads a 64-bit float from the current position."

  [view]

  (binf.protocol/rr-f64 view))



(defn rr-string
  
  "Reads a string consisting of `n-byte` from the current position.

   A non-nil decoder may be provided (default is UTF-8).
  
   See [[text-decoder]]"


  ([view n-byte]

   (binf.protocol/rr-string view
                            binf.string/decoder-utf-8
                            n-byte))


  ([view decoder n-byte]

   (binf.protocol/rr-string view
                            decoder
                            n-byte)))


;;;;; IRelativeWriter


(defn wr-buffer
  
  "Copies the given `buffer` to the current position.

   An `offset` in the buffer as well as a number of bytes to copy (`n-byte`) may be provided."

  ([view buffer]

   (binf.protocol/wr-buffer view
                            buffer
                            0
                            (count buffer)))


  ([view buffer offset]

   (binf.protocol/wr-buffer view
                            buffer
                            offset
                            (- (count buffer)
                               offset)))


  ([view buffer offset n-byte]

   (binf.protocol/wr-buffer view
                            buffer
                            offset
                            n-byte)))



(defn wr-b8

  "Writes an 8-bit integer to the current position."

  [view int]

  (binf.protocol/wr-b8 view
                       int))



(defn wr-b16

  "Writes a 16-bit integer to the current position."

  [view int]

  (binf.protocol/wr-b16 view
                        int))



(defn wr-b32

  "Writes a 32-bit integer to the current position."

  [view int]

  (binf.protocol/wr-b32 view
                        int))



(defn wr-b64

  "Writes a 64-bit integer to the current position."

  [view int]

  (binf.protocol/wr-b64 view
                        int))



(defn wr-f32

  "Writes a 32-bit float to the current position."

  [view floating]

  (binf.protocol/wr-f32 view
                        floating))



(defn wr-f64

  "Writes a 64-bit float to the current position."

  [view floating]

  (binf.protocol/wr-f64 view
                        floating))



(defn wr-string
  
  "Writes a string to the current position, encoded at UTF-8.
  
   Cf. [[wa-string]] about the returned value"

  [view string]

  (binf.protocol/wr-string view
                           string))


;;;; IPosition


(defn limit

  ""

  [view]

  (binf.protocol/limit view))



(defn position

  "Returns the current position."

  [view]

  (binf.protocol/position view))



(defn seek

  "Modifies the current position."

  [view position]

  (binf.protocol/seek view
                      position))



(defn skip

  "Advances the current position by `n-byte` bytes."

  [view n-byte]

  (binf.protocol/skip view
                      n-byte))


;;;;; IViewable


(defn view
  
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
   ```"

  ([viewable]

   (binf.protocol/view viewable))


  ([viewable offset]

   (binf.protocol/view viewable
                       offset))


  ([viewable offset n-byte]

   (binf.protocol/view viewable
                       offset
                       n-byte)))


;;;;;;;;;; Helper functions


(defn garanteed?

  ""

  [view n-byte]

  (>= (remaining view)
      n-byte))



(defn remaining

  "Returns the number of bytes remaining until the end of the view."

  [view]

  (- (binf.protocol/limit view)
     (binf.protocol/position view)))


;;;;;;;;;; R/W LEB128


(defn rr-leb128-u32

  ""

  [view]

  (loop [u32   0
         shift 0]
    (let [b8    (rr-u8 view)
          u32-2 (bit-or u32
                        (<< (bit-and b8
                                     0x7f)
                            shift))]
      (if (zero? (bit-and b8
                          0x80))
        #?(:clj  u32-2
           :cljs (binf.int/u32 u32-2))
        (recur u32-2
               (+ shift
                  7))))))



(defn rr-leb128-u64

  ""

  [view]

  (loop [u64   (binf.int64/u* 0)
         shift (binf.int64/u* 0)]
    (let [b8    (rr-u8 view)
          u64-2 (bit-or u64
                        (<< (binf.int64/u* (bit-and b8
                                                    0x7f))
                            shift))]
      (if (zero? (bit-and b8
                          0x80))
        u64-2
        (recur u64-2
               (+ shift
                  (binf.int64/u* 7)))))))



(defn rr-leb128-i32

  ""

  [view]

  (loop [i32   0
         shift 0]
    (let [b8      (rr-u8 view)
          i32-2   (bit-or i32
                          (<< (bit-and b8
                                       0x7f)
                              shift))
          shift-2 (+ shift
                     7)]
      (if (zero? (bit-and b8
                          0x80))
        (if (and (< shift-2
                    32)
                 (not (zero? (bit-and b8
                                      0x40))))
          (binf.int/i32 (bit-or i32-2
                               (<< (bit-not 0)
                                   shift-2)))
          (binf.int/i32 i32-2))
        (recur i32-2
               shift-2)))))







(defn wr-leb128-u32

  ""

  [view u32]

  (loop [u32-2 u32]
    (let [b8    (bit-and u32-2
                         0x7f)
          u32-3 (>>> u32-2
                     7)]
      (if (zero? u32-3)
        (do
          (wr-b8 view
                 b8)
          view)
        (do
          (wr-b8 view
                 (bit-or b8
                         0x80))
          (recur u32-3))))))



(defn wr-leb128-i32

  ""

  [view i32]

  (loop [i32-2 i32]
    (let [b8    (bit-and i32-2
                         0x7f)
          i32-3 (>> i32-2
                    7)]
      (if (or (and (zero? i32-3)
                   (zero? (bit-and b8
                                   0x40)))
              (and (= i32-3
                      -1)
                   (not (zero? (bit-and b8
                                        0x40)))))
        (do
          (wr-b8 view
                 b8)
          view)
        (do
          (wr-b8 view
                 (bit-or b8
                         0x80))
          (recur i32-3))))))



(defn wr-leb128-u64

  ""

  [view u64]

  (loop [u64-2 u64]
    (let [b8    (bit-and u64-2
                         (binf.int64/u* 0x7f))
          u64-3 (binf.int64/u>> u64-2
                                (binf.int64/u* 7))]
      (if (= (binf.int64/u* 0)
             u64-3)
        (do
          (wr-b8 view
                 (binf.int64/u8 b8))
          view)
        (do
          (wr-b8 view
                 (binf.int64/u8 (bit-or b8
                                        (binf.int64/u* 0x80))))
          (recur u64-3))))))




