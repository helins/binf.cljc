;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns helins.binf.string

  "Decoding a string directly to a buffer and vice-versa."
  
  {:author "Adam Helinski"}

#?(:clj (:import (java.nio.charset Charset
                                   StandardCharsets))))


;;;;;;;;;; Selecting encodings


(defn decoder

  "A decoder knows how to translate a buffer into a string given an encoding.
  
   Supported cross-platform encodings are:
  
     \"iso-8859-1\"
     \"utf-8\"
     \"utf-16be\"
     \"utf-16le\"

   Other encodings are platform dependent. On the JVM, it can be anything accepted
   by the `Charset` class while in JS it can be anything accepted by a `TextDecoder`."

  [encoding]

  #?(:clj  (Charset/forName encoding)
     :cljs (js/TextDecoder. encoding)))



#?(:clj (defn encoder

  "An encoder knows how to translate a string encoded in a given encoding into a buffer.
  
   In JS, strings can only be encoded as UTF-8 which is why string encoding utilities
   from this library use exclusively UTF-8.
  
   However, on the JVM, any encoding can be used in [[encode]].
  
   See [[decoder]] for available encodings."

  [encoding]

  (Charset/forName encoding)))


;;;;;;;;;; Default encoding is UTF-8


(def decoder-utf-8

  "Default decoder used by this library (UTF-8)."

  #?(:clj  StandardCharsets/UTF_8
     :cljs (js/TextDecoder.)))



#?(:clj (def ^Charset encoder-utf-8

  "Default encoder used by this library (UTF-8)."

  StandardCharsets/UTF_8))


#?(:cljs (def encoder-utf-8

  "Default encoder used by this library (UTF-8)."

  (js/TextEncoder.)))


;;;;;;;;; Translation between strings and buffers


(defn decode

  "Interprets the given `buffer` as a string.

   Defaults to UTF-8.

   See [[decoder]]."

  ([buffer]

   (decode buffer
           decoder-utf-8))


  ([buffer decoder]

   #?(:clj  (String. ^bytes buffer
                     ^Charset decoder)
      :cljs (.decode decoder
                     buffer))))



(defn encode

  "Returns a buffer containing the given `string` encoded in UTF-8."


  (^bytes
   [string]

   #?(:clj  (.getBytes ^String string
                       ^Charset encoder-utf-8)
      :cljs (.-buffer (.encode encoder-utf-8
                               string))))

  #?(:clj (^bytes
           [string encoder]

           (.getBytes ^String string
                      ^Charset encoder))))

