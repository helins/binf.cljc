;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns helins.binf.buffer

  "Buffers are the native representation of a byte array which can be wrapped in a view
   and manipulated by using the core `helins.binf` namespaces. They represent a raw, fixed-size
   chunk of memory."

  {:author "Adam Helinski"}

  #?(:cljs (:require [helins.binf.protocol :as binf.protocol])))


;;;;;;;;;; Creating new buffers


(defn alloc

  "Allocates a new buffer having `n-byte` bytes.
  
   In Clojurescript, corresponds to a JS `ArrayBuffer`.

   In Clojure on the JVM, corresponds to a plain byte array.
  
   In order to do anything interesting with this library, it needs to be wrapped in a [[helins.binf/view]]."

  [n-byte]

  #?(:clj  (byte-array n-byte)
     :cljs (js/ArrayBuffer. n-byte)))



#?(:cljs (def ^{:arglists '([n-byte])}
              alloc-shared

  "Akin to [[alloc]], allocates a JS `SharedArrayBuffer`.
  
   Throws if they are not supported by the JS environment."

  (if (exists? js/SharedArrayBuffer)
    (fn [n-byte]
      (js/SharedArrayBuffer. n-byte))
    (fn [_n-byte]
      (throw (js/Error. "SharedArrayBuffer are not supported by this JS environmen"))))))


#?(:cljs (def alloc-shared?

  "True if the JS environment supports using [[alloc-shared]]."

  (exists? js/SharedArrayBuffer)))


;;;;;;;;;; Copying between buffers


(defn copy

  "Copies a buffer to another buffer."

  ([src-buffer]

   (let [n-byte (count src-buffer)]
     (copy (alloc n-byte)
           0
           src-buffer
           0
           n-byte)))


  ([dest-buffer src-buffer]

   (copy dest-buffer
         0
         src-buffer
         0
         (count src-buffer)))


  ([dest-buffer dest-offset src-buffer]

   (copy dest-buffer
         dest-offset
         src-buffer
         0
         (count src-buffer)))


  ([dest-buffer dest-offset src-buffer src-offset]

   (copy dest-buffer
         dest-offset
         src-buffer
         src-offset
         (- (count src-buffer)
            src-offset)))


  ([dest-buffer dest-offset src-buffer src-offset n-byte]

   #?(:clj  (System/arraycopy ^bytes src-buffer
                              src-offset
                              ^bytes dest-buffer
                              dest-offset
                              n-byte)
      :cljs (.set (js/Uint8Array. dest-buffer)
                  (js/Uint8Array. src-buffer
                                  src-offset
                                  n-byte)
                  dest-offset))
   dest-buffer))


;;;;;;;;;; Making it easier to work with buffers and testing them


#?(:cljs (extend-type js/ArrayBuffer

  ICounted

    (-count [this]
      (.-byteLength this))


  ISeqable

    (-seq [this]
      (array-seq (js/Int8Array. this)))


  binf.protocol/IGrow

    (grow [this n-additional-byte]
      (let [buffer-new (js/ArrayBuffer. (+ (.-byteLength this)
                                           n-additional-byte))]
          (copy buffer-new
                this)
          buffer-new))))



#?(:cljs (when (exists? js/SharedArrayBuffer) (extend-type js/SharedArrayBuffer

  ICounted

    (-count [this]
      (.-byteLength this))


  ISeqable

    (-seq [this]
      (array-seq (js/Int8Array. this)))


  binf.protocol/IGrow

   (grow [this n-additional-byte]
      (let [buffer-new (js/SharedArrayBuffer. (+ (.-byteLength this)
                                                 n-additional-byte))]
          (copy buffer-new
                this)
          buffer-new)))))
