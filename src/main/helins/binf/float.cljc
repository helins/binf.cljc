;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns helins.binf.float

  ""

  {:author "Adam Helinski"})


;;;;;;;;;; 


(defn b32

  "Converts a 32-bit float to an integer preserving the bit pattern.
  
   Opposite of [[f32]]."

  [f32]

  #?(:clj  (Float/floatToIntBits f32)
     :cljs (let [data-view (js/DataView. (js/ArrayBuffer. 4))]
             (.setFloat32 data-view
                          0
                          f32)
             (.getUint32 data-view))))



(defn b64

  "Converts a 64-bit float to an integer preserving the bit pattern.
  
   Opposite of [[f32]]."

  [f64]

  #?(:clj  (Double/doubleToLongBits f64)
     :cljs (let [data-view (js/DataView. (js/ArrayBuffer. 8))]
             (.setFloat64 data-view
                          0
                          f64)
             (.getBigUint64 data-view
                            0))))



(defn from-b32

  "Interprets bits from an integer (at least 32 bits) as a 32-bit float.
  
   Opposite of [[bits-f32]]."

  [b32]

  #?(:clj  (Float/intBitsToFloat b32)
     :cljs (let [data-view (js/DataView. (js/ArrayBuffer. 4))]
             (.setUint32 data-view
                         0
                         b32)
             (.getFloat32 data-view
                          0))))



(defn from-b64

  "Interprets bits from a 64-bits integer as a 64-bit float.
  
   Opposite of [[bits-64]]."

  [b64]

  #?(:clj  (Double/longBitsToDouble b64)
     :cljs (let [data-view (js/DataView. (js/ArrayBuffer. 8))]
             (.setBigUint64 data-view
                            0
                            b64)
             (.getFloat64 data-view
                          0))))


;;;;;;;;;;


(defn f32

  ""

  [x]

  (float x))



(defn f64

  ""

  [x]

  (double x))
