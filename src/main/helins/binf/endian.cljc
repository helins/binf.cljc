;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns helins.binf.endian

  ""

  {:author "Adam Helinski"}
  #?(:cljs (:require [goog.object]
                     [helins.binf.buffer :as binf.buffer]))
  #?(:clj (:import java.nio.ByteOrder)))


;;;;;;;;;;


#?(:cljs (def ^:private -d*js-order

  ;;

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

  ""

  []

  #?(:clj  :big-endian
     :cljs @-d*js-order))



(defn order-native

  ""

  []

  #?(:clj  (ByteOrder/nativeOrder)
     :cljs @-d*js-order))


;;;;;;;;;;


#?(:cljs (def ^:private -data-view

  ;;

  (js/DataView. (binf.buffer/alloc 8))))



(defn b16

  ""

  [b16]

  #?(:clj  (Short/reverseBytes (unchecked-short b16))
     :cljs (do
             (.setUint16 -data-view
                         0
                         b16
                         false)
             (.getUint16 -data-view
                         0
                         true))))



(defn b32

  ""

  [b32]

  #?(:clj  (Integer/reverseBytes (unchecked-int b32))
     :cljs (do
             (.setUint32 -data-view
                         0
                         b32
                         false)
             (.getUint32 -data-view
                          0
                          true))))



(defn b64

  ""

  [b64]

  #?(:clj  (Long/reverseBytes b64)
     :cljs (do
             (.setBigUint64 -data-view
                            0
                            b64
                            false)
             (.getBigUint64 -data-view
                            0
                            true))))
