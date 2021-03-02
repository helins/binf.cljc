;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns helins.binf.native

  ""

  {:author "Adam Helinski"}

  (:require [helins.binf.int      :as binf.int]
            [helins.binf.protocol :as binf.protocol])
  (:import (java.nio ByteBuffer
                     DirectByteBuffer)
           sun.misc.Unsafe))


;;;;;;;;;; Implementing necessary protocols


(extend-type DirectByteBuffer


  binf.protocol/-IByteBuffer

    (-array-index [_this position]
      position)


  binf.protocol/IGrow

    (grow [this n-additional-byte]
      (.position this
                 0)
      (let [bb-new (ByteBuffer/allocateDirect (+ (binf.protocol/limit this)
                                                 n-additional-byte))]
        (.put bb-new
              this)
        bb-new)))


;;;;;;;;;; Access to the Unsafe API


(def ^:no-doc ^Unsafe -unsafe

  (let [field (doto (.getDeclaredField Unsafe
                                       "theUnsafe")
                (.setAccessible true))]
    (.get field
          nil)))


;;;;;;;;;;  Creating native views


(defn view

  ""

  ^DirectByteBuffer

  [n-byte]

  (ByteBuffer/allocateDirect n-byte))


;;;;;;;;;; Handling raw pointers


(defn alloc

  ""

  [n-byte]

  (.allocateMemory -unsafe
                   n-byte))



(defn copy

  ""

  [ptr-dest ptr-src n-byte]

  (.copyMemory -unsafe
               ptr-src
               ptr-dest
               n-byte))



(defn free

  ""

  [ptr]

  (.freeMemory -unsafe
               ptr)
  nil)



(defn realloc

  ""

  [ptr n-byte]

  (.reallocateMemory -unsafe
                     ptr
                     n-byte))


;;;;;;;;;; Reading and writing values using raw pointers


(defn r-i8

  ""

  [ptr]

  (.getByte -unsafe
            ptr))



(defn r-u8

  ""

  [ptr]

  (binf.int/u8 (r-i8 ptr)))



(defn w-b8

  ""

  [ptr b8]

  (.putByte -unsafe
            ptr
            b8))


;;;;;


(defn r-i16

  ""

  [ptr]

  (.getShort -unsafe
             ptr))



(defn r-u16

  ""

  [ptr]

  (binf.int/u16 (r-i16 ptr)))



(defn w-b16

  ""

  [ptr b16]

  (.putShort -unsafe
             ptr
             b16))


;;;;;


(defn r-i32

  ""

  [ptr]

  (.getInt -unsafe
           ptr))



(defn r-u32

  ""

  [ptr]

  (binf.int/u32 (r-i32 ptr)))



(defn w-b32

  ""

  [ptr b32]

  (.putInt -unsafe
           ptr
           b32))


;;;;;


(defn r-b64

  ""

  [ptr]

  (.getLong -unsafe
            ptr))



(defn w-b64

  ""

  [ptr b64]

  (.putLong -unsafe
            ptr
            b64))


;;;;;


(defn r-f32

  ""

  [ptr]

  (.getFloat -unsafe
             ptr))



(defn w-f32

  ""

  [ptr f32]

  (.putFloat -unsafe
             ptr
             f32))


;;;;;


(defn r-f64

  ""

  [ptr]

  (.getDouble -unsafe
              ptr))



(defn w-f64

  ""

  [ptr f64]

  (.putDouble -unsafe
              ptr
              f64))


;;;;;;;;;; Reading and writing pointers


(def sz-ptr

  ""

  (.addressSize -unsafe))



(defn r-ptr

  ""

  [ptr]

  (.getAddress -unsafe
               ptr))



(defn w-ptr

  ""

  [ptr ptr-value]

  (.putAddress -unsafe
               ptr
               (unchecked-long ptr-value)))
