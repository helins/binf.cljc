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


;;;;; Translation between views and pointer


(defn view->ptr

  ""

  [view]

  (.getLong -field-address
            view))



(defn ptr->view

  ""

  [ptr ^long n-byte]

  (let [view (-> (view 0)
                 (.order (ByteOrder/nativeOrder)))]
    (.setLong -field-address
              view
              ptr)
    (.setInt -field-capacity
             view
             n-byte)
    (.limit view
            n-byte)
    view))


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
               ptr-value))





(comment

  (require '[helins.binf :as binf])


  (def pt (alloc 10))
  (def v (ptr->view pt 5000))

  (binf/wa-b8 v
              4999
              42)

  )
