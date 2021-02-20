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




#_(defprotocol IAbsoluteWriter


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
