(ns helins.binf.protocol

  ""

  {:author "Adam Helinski"})


;;;;;;;;;;


(defprotocol IAbsoluteReader

  "Reading primitive values at an absolute position, without disturbing the current one."
  
  (ra-buffer [view position n-byte buffer offset])

  (ra-u8 [view position])

  (ra-i8 [view position])

  (ra-u16 [view position])

  (ra-i16 [view position])

  (ra-u32 [view position])

  (ra-i32 [view position])

  (ra-u64 [view position])

  (ra-i64 [view position])

  (ra-f32 [view position])

  (ra-f64 [view position])
  
  (ra-string [view decoder position n-byte]))



(defprotocol IAbsoluteWriter

  "Writing primitive values at an absolute position, without disturbing the current one.
  
   When writing integers, sign is irrelevant and truncation is automatic."
  
  (wa-buffer [view position buffer offset n-byte])

  (wa-b8 [view position integer])

  (wa-b16 [view position integer])

  (wa-b32 [view position integer])

  (wa-b64 [view position integer])

  (wa-f32 [view position floating])

  (wa-f64 [view position floating])
  
  (wa-string [view position string]))



(defprotocol IEndianess

  "Retrieving or modifying the endianess."
  
  (endian-get [view])

  (endian-set [view endianess]))



(defprotocol IRelativeReader

  "Reading primitive values from the current position, advancing it as needed. For instance,
   reading a 32-bit integer will advance the current position by 4 bytes."

  (rr-buffer [view n-byte buffer offset])

  (rr-u8 [view])

  (rr-i8 [view])

  (rr-u16 [view])

  (rr-i16 [view])

  (rr-u32 [view])

  (rr-i32 [view])

  (rr-u64 [view])

  (rr-i64 [view])

  (rr-f32 [view])

  (rr-f64 [view])
  
  (rr-string [view n-byte]
             [view decoder n-byte]))



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


(defprotocol IViewable

  "Building a new view."

  (view [viewable]
        [viewable offset]
        [viewable offset n-byte]))
