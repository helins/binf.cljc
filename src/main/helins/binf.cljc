(ns helins.binf

  "Uninhibited library for handling any kind binary format or protocol.
  
   See README for an overview."

  {:author "Adam Helins"}

  (:require [clojure.core         :as clj]
            [helins.binf.buffer   :as binf.buffer]
            [helins.binf.protocol :as binf.protocol])
  #?(:cljs (:require-macros [helins.binf]))
  #?(:clj (:import clojure.lang.Counted
                   (java.nio ByteBuffer
                             ByteOrder
                             CharBuffer)
                   (java.nio.charset Charset
                                     CoderResult
                                     StandardCharsets)))
  (:refer-clojure :rename {bit-shift-left           <<
                           bit-shift-right          >>
                           unsigned-bit-shift-right >>>}))




#?(:cljs (def ^:private -text-decoder-utf-8

  ;;

  (js/TextEncoder.)))



#?(:cljs (def ^:private -text-encoder

  ;;

  (js/TextEncoder.)))


;;;;;;;;;; Primitive type sizes


(def sz-b8

  "Number of bytes in an 8-bit integer."

  1)



(def sz-b16

  "Number of bytes in a 16-bit integer."

  2)



(def sz-b32

  "Number of bytes in a 32-bit integer."

  4)



(def sz-b64

  "Number of bytes in a 64-bit integer."

  8)



(def sz-f32

  "Number of bytes in a 32-bit float."

  4)



(def sz-f64

  "Number of bytes in a 64-bit float."

  8)


;;;;;;;;;; Types and protocol extensions

(comment

;; Copying in CLJS is almost exactly the same as in CLJ




(defn remaining

  "Returns the number of bytes remaining until the end of the view."

  [view]

  (- (count view)
     (position view)))

)









;;;;;


;#?(:cljs (defn view->data-view
;
;  ""
;
;  [view]
;
;  ))


;;;;;;;;;; Creating primitives from bytes


#?(:cljs (def ^:no-doc -view-cast (binf.protocol/view (binf.buffer/alloc 8))))
