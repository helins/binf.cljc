(ns helins.binf.native

  ""

  {:author "Adam Helinski"}

  (:require [helins.binf.int      :as binf.int]
            [helins.binf.protocol :as binf.protocol])
  (:import (java.nio Buffer
                     ByteBuffer
                     ByteOrder
                     DirectByteBuffer)
           sun.misc.Unsafe))


(set! *warn-on-reflection*
      true)


;;;;;;;;;; Reflection


(def ^:no-doc ^java.lang.reflect.Field -field-address

  ;;

  (doto (.getDeclaredField Buffer
                           "address")
    (.setAccessible true)))



(def ^:no-doc ^java.lang.reflect.Field -field-capacity

  ;;

  (doto (.getDeclaredField Buffer
                           "capacity")
    (.setAccessible true)))


;;;;;;;;;; Implementing necessary protocols


(extend-type DirectByteBuffer


  binf.protocol/-IByteBuffer

    (-array-index [_this position]
      position))


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


(defn free

  ""

  [pointer]

  (.freeMemory -unsafe
               pointer)
  nil)



(defn realloc

  ""

  [pointer n-byte]

  (.reallocateMemory -unsafe
                     pointer
                     n-byte))


;;;;; Translation between views and pointer


(defn view->pointer

  ""

  [view]

  (.getLong -field-address
            view))



(defn pointer->view

  ""

  [pointer ^long n-byte]

  (let [view (-> (view 0)
                 (.order (ByteOrder/nativeOrder)))]
    (.setLong -field-address
              view
              pointer)
    (.setInt -field-capacity
             view
             n-byte)
    (.limit view
            n-byte)
    view))


;;;;;;;;;; Reading and writing values using raw pointers


(defn r-i8

  ""

  [pointer]

  (.getByte -unsafe
            pointer))



(defn r-u8

  ""

  [pointer]

  (binf.int/u8 (r-i8 pointer)))



(defn w-b8

  ""

  [pointer b8]

  (.putByte -unsafe
            pointer
            b8))


;;;;;


(defn r-i16

  ""

  [pointer]

  (.getShort -unsafe
             pointer))



(defn r-u16

  ""

  [pointer]

  (binf.int/u16 (r-i16 pointer)))



(defn w-b16

  ""

  [pointer b16]

  (.putShort -unsafe
             pointer
             b16))


;;;;;


(defn r-i32

  ""

  [pointer]

  (.getInt -unsafe
           pointer))



(defn r-u32

  ""

  [pointer]

  (binf.int/u32 (r-i32 pointer)))



(defn w-b32

  ""

  [pointer b32]

  (.putInt -unsafe
           pointer
           b32))


;;;;;


(defn r-b64

  ""

  [pointer]

  (.getLong -unsafe
            pointer))



(defn w-b64

  ""

  [pointer b64]

  (.putLong -unsafe
            pointer
            b64))


;;;;;


(defn r-f32

  ""

  [pointer]

  (.getFloat -unsafe
             pointer))



(defn w-f32

  ""

  [pointer f32]

  (.putFloat -unsafe
             pointer
             f32))


;;;;;


(defn r-f64

  ""

  [pointer]

  (.getDouble -unsafe
              pointer))



(defn w-f64

  ""

  [pointer f64]

  (.putDouble -unsafe
              pointer
              f64))









(comment

  (require '[helins.binf :as binf])


  (def pt (alloc 10))
  (def v (pointer->view pt 5000))

  (binf/wa-b8 v
              4999
              42)

  )
