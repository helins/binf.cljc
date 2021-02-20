(ns helins.binf.protocol

  ""

  {:author "Adam Helinski"})


;;;;;;;;;;


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

  "Additional functions related to views."

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
