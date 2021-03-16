;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns helins.binf.protocol

  "Protocols related to views.
  
   This namespace should be relevant only if a new type needs to implement those. The common user
   should not have to bother.
  
   Those functions are described in the core `helins.binf` namespace as well as in the README file.
  
   Depending on the type, not all protocols must be implemented. For instance, a stream-like type
   might not be seekable and might implement only `IRelative*` protocols and not `IAbsolute` ones."

  {:author "Adam Helinski"})


;;;;;;;;;; Public


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
  
   When writing integers, sign is irrelevant and truncation is automatic between all integers that
   are <= 32-bits."
  
  (wa-buffer [view position buffer offset n-byte])

  (wa-b8 [view position int])

  (wa-b16 [view position int])

  (wa-b32 [view position int])

  (wa-b64 [view position int])

  (wa-f32 [view position floating])

  (wa-f64 [view position floating])
  
  (wa-string [view position string]))



(defprotocol IBackingBuffer

  "Some views operates over a buffer which can be directly accessed using this protocol."

  (backing-buffer [this])

  (buffer-offset [this]))



(defprotocol IEndianess

  "Retrieving or modifying the endianess."
  
  (endian-get [view])

  (endian-set [view endianess]))



(defprotocol IGrow

  "Growing a view means returning a new view with a higher capacity, copying the initial one."

  (grow [this n-additional-byte]))



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
  
  (rr-string [view decoder n-byte]))



(defprotocol IRelativeWriter

  "Writing primitive values to the current position, advancing it as needed. For instance,
   reading a 64-bit float will advance the current position by 8 bytes.

   When writing integers, sign is irrelevant and truncation is automatic between all integers that
   are <= 32-bits."

  (wr-buffer [view buffer offset n-byte])
  
  (wr-b8 [view int])

  (wr-b16 [view int])

  (wr-b32 [view int])

  (wr-b64 [view int])

  (wr-f32 [view floating])

  (wr-f64 [view floating])

  (wr-string [view string]))



(defprotocol IPosition

  "Handling the position of a view if it is seekable."

  (limit [view])

  (position [view])

  (seek [view position])
  
  (skip [view n-byte]))



(defprotocol IViewable

  "Building a new view."

  (view [viewable]
        [viewable offset]
        [viewable offset n-byte]))


;;;;;;;;;; Hidden


#?(:clj (defprotocol ^:no-doc -IByteBuffer

  ;; Direct ByteBuffer do not implement `.arrayOffset`.
  ;; 
  ;; Implementing this protocol in both direct and regular ByteBuffers allows for maximum code reuse.
 
  (-array-index [this position]
    ;; Given a position in a view (ie. ByteBuffer), returns the index in the backing array.
    )))
