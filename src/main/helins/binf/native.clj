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
      position)


  ;binf.protocol/IPosition

  ;  (offset [this]
  ;    nil)


  ;  (to-buffer [this]
  ;    nil)
    )


;;;;;;;;;; Access to native memory


(defn view

  ""

  ^DirectByteBuffer

  [n-byte]

  (ByteBuffer/allocateDirect n-byte))
