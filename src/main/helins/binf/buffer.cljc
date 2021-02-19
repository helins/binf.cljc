(ns helins.binf.buffer

  ""

  {:author "Adam Helinski"})


;;;;;;;;;; Creating new buffers


(defn alloc

  "Allocates a new buffer having `n-byte` bytes.
  
   In Clojurescript, corresponds to a JS `ArrayBuffer`.

   In Clojure on the JVM, corresponds to a plain byte array.
  
   In order to do anything interesting with this library, it needs to be wrapped in a [[helins.binf/view]]."

  [n-byte]

  #?(:clj  (byte-array n-byte)
     :cljs (js/ArrayBuffer. n-byte)))



#?(:cljs (defn alloc-shared

  "Akin to [[alloc]], allocates a JS `SharedArrayBuffer`."

  [n-byte]

  (js/SharedArrayBuffer. n-byte)))


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


#?(:cljs

(extend-type js/ArrayBuffer

  ICounted

    (-count [this]
      (.-byteLength this))


  ISeqable

    (-seq [this]
      (array-seq (js/Int8Array. this)))))



#?(:cljs

(extend-type js/SharedArrayBuffer

  ICounted

    (-count [this]
      (.-byteLength this))


  ISeqable

    (-seq [this]
      (array-seq (js/Int8Array. this)))))
