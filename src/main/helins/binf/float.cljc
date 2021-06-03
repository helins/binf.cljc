;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns helins.binf.float

  "Handling floating values, miscellaneous coercions."

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [=])
  (:require [clojure.core]))


;;;;;;;;;; Conversions between floats and bit patterns


(defn b32

  "Converts a 32-bit float to a 32-bit integer preserving the bit pattern.
  
   Opposite of [[from-b32]].
  
   See [[b64]] for an example."

  [f32]

  #?(:clj  (Float/floatToIntBits f32)
     :cljs (let [data-view (js/DataView. (js/ArrayBuffer. 4))]
             (.setFloat32 data-view
                          0
                          f32)
             (.getUint32 data-view))))



(defn b64

  "Converts a 64-bit float to a 64-bit integer preserving the bit pattern.
  
   Opposite of [[from-b64]].
  
   ```clojure
   (def bits
        (b64 42.24))

   ;; equals 4631166901565532406

   (= 42.24
      (from-b64 bits))
   ```
  
   Useful for places which can handle integers but not floats."

  [f64]

  #?(:clj  (Double/doubleToLongBits f64)
     :cljs (let [data-view (js/DataView. (js/ArrayBuffer. 8))]
             (.setFloat64 data-view
                          0
                          f64)
             (.getBigUint64 data-view
                            0))))



(defn from-b32

  "Interprets bits from a 32-bit integer as a 32-bit float.
  
   Opposite of [[b32]]."

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
  
   Opposite of [[b64]]."

  [b64]

  #?(:clj  (Double/longBitsToDouble b64)
     :cljs (let [data-view (js/DataView. (js/ArrayBuffer. 8))]
             (.setBigUint64 data-view
                            0
                            b64)
             (.getFloat64 data-view
                          0))))


;;;;;;;;;; Conversions to floats


(defn f32

  "Coerce `x` to a 32-bit float.
  
   In JS, there are no 32-bit floats. Hence, the returned value is a 32-bit representation embedded
   in a 64-bit number."

  [x]

  #?(:clj  (float x)
     :cljs (let [arr (js/Float32Array. 1)]
             (aset arr
                   0
                   x)
             (aget arr
                   0))))



(defn f64

  "Coerce `x` to a 64-bit float."

  [x]

  (double x))


;;;;;;;;;; Miscellaneous


(defn nan?

  "Returns true if `x` is `##NaN`."

  [x]

  #?(:clj  (Double/isNaN x)
     :cljs (js/isNaN x)))



(defn =

  "Tests for equality between 2 floats where `##NaN` is equal to itself (unlike regular `=` or `==`)."

  [x-1 x-2]

  (if (nan? x-1)
    (nan? x-2)
    (clojure.core/= x-1
                    x-2)))
