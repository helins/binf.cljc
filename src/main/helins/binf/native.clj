(ns helins.binf.native

  ""

  {:author "Adam Helinski"}

  (:require [helins.binf.protocol :as binf.protocol])
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

  (.get (doto (.getDeclaredField Unsafe
                                 "theUnsafe")
          (.setAccessible true))
        nil))


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





(comment

  (require '[helins.binf :as binf])


  (def pt (alloc 10))
  (def v (pointer->view pt 50))

  (binf/wa-b8 v
              20
              42)

  )
