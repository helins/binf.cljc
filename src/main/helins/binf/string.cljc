(ns helins.binf.string

  ""

  {:author "Adam Helinski"}

#?(:clj (:import (java.nio.charset Charset
                                   StandardCharsets))))


;;;;;;;;;; Selecting encodings


(defn decoder

  ""

  [encoding]

  #?(:clj  (Charset/forName encoding)
     :cljs (js/TextDecoder. encoding)))



#?(:clj (defn encoder

  ""

  [encoding]

  (Charset/forName encoding)))


;;;;;;;;;; Default encoding is UTF-8


(def decoder-utf-8

  ""

  #?(:clj  StandardCharsets/UTF_8
     :cljs (js/TextDecoder.)))



#?(:clj (def ^Charset encoder-utf-8

  ""

  StandardCharsets/UTF_8))


#?(:cljs (def encoder-utf-8

  ""

  (js/TextEncoder.)))


;;;;;;;;; Translation between strings and buffers


(defn decode

  "Interprets the given `buffer` as a string.

   Uses [[decoder-utf-8]] in arity 1."

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

  [string]

  #?(:clj  (.getBytes ^String string
                      ^Charset encoder-utf-8)
     :cljs (.-buffer (.encode encoder-utf-8
                              string))))
