;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns helins.binf.native

  "Contains [[view]] for creating a view over native memory (as opposed to the JVM heap)
   as well as utilities for handling raw pointers.
  
   The main utility is [[view]] which returns a `DirectByteBuffer`. Allocating native memory
   can lead to performance increase and simplifies things when using tools such as JNI.
  
   Other utilities are related to raw pointers and are DANGEROUS.

   Manipulating raw pointers and managing memory is just as error prone as in native programming.
   Those utilities are for users knowing both what they want and what it implies. Otherwise,
   segfaults are to be expected."

  {:author "Adam Helinski"}

  (:require [helins.binf.int :as binf.int])
  (:import (java.nio ByteBuffer
                     DirectByteBuffer)
           sun.misc.Unsafe))


;;;;;;;;;; Access to the Unsafe API


(def ^:no-doc ^Unsafe -unsafe

  (let [field (doto (.getDeclaredField Unsafe
                                       "theUnsafe")
                (.setAccessible true))]
    (.get field
          nil)))


;;;;;;;;;;  Creating native views


(defn view

  "Allocates `n-byte` bytes in native memory, outside of the JVM heap, and returns a view over it.
  
   Very useful for performance increase in some situations or for interacting with native functions.
   The returned view is actually a `DirectByteBuffer` commonly understood by many tools (such as JNI)."

  ^DirectByteBuffer

  [n-byte]

  (ByteBuffer/allocateDirect n-byte))


;;;;;;;;;; Handling raw pointers


(defn alloc

  "Returns a raw pointer after allocating `n-byte` bytes in native memory."

  [n-byte]

  (.allocateMemory -unsafe
                   n-byte))



(defn copy

  "Copies `n-byte` bytes from a raw pointer to another raw pointer."

  [ptr-dest ptr-src n-byte]

  (.copyMemory -unsafe
               ptr-src
               ptr-dest
               n-byte))



(defn free

  "De-allocates a raw pointer."

  [ptr]

  (.freeMemory -unsafe
               ptr)
  nil)



(defn realloc

  "Re-allocates a raw pointer."

  [ptr n-byte]

  (.reallocateMemory -unsafe
                     ptr
                     n-byte))


;;;;;;;;;; Reading and writing values using raw pointers


(defn r-i8

  "Dereferences a raw pointer to a signed 8-bit integer."

  [ptr]

  (.getByte -unsafe
            ptr))



(defn r-u8

  "Dereferences a raw pointer to an unsigned 8-bit integer."

  [ptr]

  (binf.int/u8 (r-i8 ptr)))



(defn w-b8

  "Writes an 8-bit integer at the given raw pointer."

  [ptr b8]

  (.putByte -unsafe
            ptr
            b8))


;;;;;


(defn r-i16

  "Dereferences a raw pointer to a signed 16-bit integer."

  [ptr]

  (.getShort -unsafe
             ptr))



(defn r-u16

  "Dereferences a raw pointer to an unsigned 16-bit integer."

  [ptr]

  (binf.int/u16 (r-i16 ptr)))



(defn w-b16

  "Writes a 16-bit integer at the given raw pointer."

  [ptr b16]

  (.putShort -unsafe
             ptr
             b16))


;;;;;


(defn r-i32

  "Dereferences a raw pointer to a signed 32-bit integer."

  [ptr]

  (.getInt -unsafe
           ptr))



(defn r-u32

  "Dereferences a raw pointer to an unsigned 32-bit integer."

  [ptr]

  (binf.int/u32 (r-i32 ptr)))



(defn w-b32

  "Writes a 32-bit integer at the given raw pointer."

  [ptr b32]

  (.putInt -unsafe
           ptr
           b32))


;;;;;


(defn r-b64

  "Dereferences a raw pointer to a 64-bit integer.
  
   Meant to be used with the `helins.binf.int64` namespace."

  [ptr]

  (.getLong -unsafe
            ptr))



(defn w-b64

  "Writes a 64-bit integer at the given raw pointer."

  [ptr b64]

  (.putLong -unsafe
            ptr
            b64))


;;;;;


(defn r-f32

  "Dereferences a raw pointer to a 32-bit float"

  [ptr]

  (.getFloat -unsafe
             ptr))



(defn w-f32

  "Writes a 32-bit float at the given raw pointer."

  [ptr f32]

  (.putFloat -unsafe
             ptr
             f32))


;;;;;


(defn r-f64

  "Dereferences a raw pointer to a 64-bit float"

  [ptr]

  (.getDouble -unsafe
              ptr))



(defn w-f64

  "Writes a 64-bit float at the given raw pointer."

  [ptr f64]

  (.putDouble -unsafe
              ptr
              f64))


;;;;;;;;;; Reading and writing pointers


(def n-byte-ptr

  "Number of bytes in a pointer on this machine.
  
   Should be 4 on a 32-bit machine, 8 on a 64-bit machine."

  (.addressSize -unsafe))



(defn r-ptr

  "Dereference a raw pointer to a pointer."

  [ptr]

  (.getAddress -unsafe
               ptr))



(defn w-ptr

  "Writes a pointer at the given raw pointer."

  [ptr ptr-value]

  (.putAddress -unsafe
               ptr
               (unchecked-long ptr-value)))
