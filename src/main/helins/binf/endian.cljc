;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns helins.binf.endian

  "Obtaining information about endianess and changing the endianess of primitive integers.
  
   Endianess is either `:big-endian` or `:little-endian`."

  {:author "Adam Helinski"}
  #?(:cljs (:require [goog.object]
                     [helins.binf.buffer :as binf.buffer]))
  #?(:clj (:import java.nio.ByteOrder)))


;;;;;;;;;;


#?(:cljs (def ^:private -d*js-order

  ;; Computes the native endianess of the host in JS.

  (delay
    (let [b16 (js/Uint32Array. 1)
          b8  (js/Uint8Array. (.-buffer b16))]
      (aset b16
            0
            0xaa)
      (if (= (aget b8
                   0)
             0xaa)
        :little-endian
        :big-endian)))))



(defn order-host

  "Returns the endianess of the host virtual machine.
   
   On the JVM, always returns `:big-endian`. In JS, returns the same as [[order-native]]."

  []

  #?(:clj  :big-endian
     :cljs @-d*js-order))



(defn order-native

  "Returns the natural endianess of the machine."

  []

  #?(:clj  (ByteOrder/nativeOrder)
     :cljs @-d*js-order))


;;;;;;;;;;


#?(:cljs (def ^:private -data-view

  ;; Used for swapping endianess.

  (js/DataView. (binf.buffer/alloc 8))))



(defn b16

  "Swaps the endianess of a 16-bit integer."

  [b16]

  #?@(:clj  [(Short/reverseBytes (unchecked-short b16))]
      :cljs [(.setUint16 -data-view
                         0
                         b16
                         false)
             (.getUint16 -data-view
                         0
                         true)]))



(defn b32

  "Swaps the endianess of a 32-bit integer."

  [b32]

  #?@(:clj  [(Integer/reverseBytes (unchecked-int b32))]
      :cljs [(.setUint32 -data-view
                         0
                         b32
                         false)
             (.getUint32 -data-view
                          0
                          true)]))



(defn b64

  "Swaps the endianess of a 64-bit integer."

  [b64]

  #?@(:clj  [(Long/reverseBytes b64)]
      :cljs [(.setBigUint64 -data-view
                            0
                            b64
                            false)
             (.getBigUint64 -data-view
                            0
                            true)]))
