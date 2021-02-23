;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns helins.binf.base64

  "Base64 encoding and decoding."

  {:author "Adam Helinski"}

  (:require #?(:cljs [goog.crypt.base64])
            [helins.binf          :as binf]
            [helins.binf.buffer   :as binf.buffer]
            [helins.binf.protocol :as binf.protocol])
  #?(:clj (:import java.nio.ByteBuffer
                   java.util.Base64)))


;;;;;;;;;;


#?(:cljs (defn- -decode

  ;; Strongly inspired by https://google.github.io/closure-library/api/goog.crypt.base64.html#encodeString

  [string make-buffer]

  (let [n-utf-16-code     (.-length string)
        n-byte-estimate   (/ (* n-utf-16-code
                                3)
                             4)
        n-byte-estimate-2 (cond
                            (not (zero? (mod n-byte-estimate
                                             3)))
                            (js/Math.floor n-byte-estimate)
                            ;;
                            (goog.crypt.base64/isPadding_ (aget string
                                                                (dec n-utf-16-code)))
                            (- n-byte-estimate
                               (if (goog.crypt.base64/isPadding_ (aget string
                                                                       (- n-utf-16-code
                                                                          2)))
                                 2
                                 1))
                            ;;
                            :else
                            n-byte-estimate)
        buffer            (make-buffer n-byte-estimate-2)
        arr-u8            (js/Uint8Array. buffer)
        v*n-byte          (volatile! 0)]
    (goog.crypt.base64/decodeStringInternal_ string
                                             (fn [b8]
                                               (aset arr-u8
                                                     @v*n-byte
                                                     b8)
                                               (vswap! v*n-byte
                                                       inc)))
    (binf.protocol/view buffer
                        0
                        @v*n-byte))))



(defn decode

  "Decodes a string into a [[buffer]] according to the Base64 basic scheme (RFC 4648 section 4)"

  #?@(:clj  [[^String string]
             (binf.protocol/view (.decode (Base64/getDecoder)
                                          string))]

      :cljs [([string]
              (decode string
                      binf.buffer/alloc))
             ([string make-buffer]
              (-decode string
                       make-buffer))]))



(defn encode

  "Encodes a [[buffer]] into a string according to the Base64 basic scheme (RFC 4648 section 4)"

  ([buffer]

   #?(:clj  (.encodeToString (Base64/getEncoder)
                             buffer)
      :cljs (goog.crypt.base64/encodeByteArray (js/Uint8Array. buffer))))


  ([buffer offset]

   #?(:clj  (encode buffer
                    offset
                    (- (count buffer)
                       offset))
      :cljs (goog.crypt.base64/encodeByteArray (js/Uint8Array. buffer
                                                               offset))))


  ([buffer offset n-byte]

   #?(:clj  (String. (.array (.encode (Base64/getEncoder)
                                      (ByteBuffer/wrap buffer
                                                       offset
                                                       n-byte))))
      :cljs (goog.crypt.base64/encodeByteArray (js/Uint8Array. buffer
                                                               offset
                                                               n-byte)))))
