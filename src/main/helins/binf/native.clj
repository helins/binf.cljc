(ns helins.binf.native

  ""

  {:author "Adam Helinski"}

  (:require [helins.binf.protocol :as binf.protocol])
  (:import (java.nio Buffer
                     ByteBuffer
                     ByteOrder
                     DirectByteBuffer)))


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


;;;;;;;;;; Access to native memory


(defn view

  ""

  ^DirectByteBuffer

  [n-byte]

  (ByteBuffer/allocateDirect n-byte))


;;;;;


(defn native?

  ""

  ;; Also returns true on mmap'ed byte buffers

  [^ByteBuffer view]

  (.isDirect view))



(defn pointer

  ""

  [view]

  (.getLong -field-address
            view))



(defn pointer->view

  ""

  [pointer n-byte]

  (let [view (-> (view 0)
                 (.order (ByteOrder/nativeOrder)))]
    (.setLong -field-address
              view
              pointer)
    (.setInt -field-capacity
             view
             n-byte)
    view))





(comment

  (require '[helins.binf :as binf])
  (def v (view 5))

  (-> v
      (.position 1)
      .slice
      pointer)

  )
