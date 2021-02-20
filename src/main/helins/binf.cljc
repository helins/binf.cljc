(ns helins.binf

  "Uninhibited library for handling any kind binary format or protocol.
  
   See README for an overview."

  {:author "Adam Helins"}

  (:require [helins.binf.buffer           :as binf.buffer]
            [helins.binf.protocol         :as binf.protocol]
            [helins.binf.protocol.impl]))


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


;;;;;;;;;; Helper functions


(defn garanteed?

  ""

  [view n-byte]

  (>= (remaining view)
      n-byte))



(defn remaining

  "Returns the number of bytes remaining until the end of the view."

  [view]

  (- (count view)
     (binf.protocol/position view)))


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

  "Reads a signed 64-bit integer from an absolute `position`."

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
                            nil
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


;;;;;


;#?(:cljs (defn view->data-view
;
;  ""
;
;  [view]
;
;  ))


;;;;;;;;;; Used by other namespaces for casting primitive types in CLJS


#?(:cljs (def ^:no-doc -view-cast (binf.protocol/view (binf.buffer/alloc 8))))
