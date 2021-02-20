(ns helins.binf

  "Uninhibited library for handling any kind binary format or protocol.
  
   See README for an overview."

  {:author "Adam Helins"}

  (:require #?(:cljs [helins.binf.buffer  :as binf.buffer])
            [helins.binf.protocol         :as binf.protocol]
            [helins.binf.protocol.impl]))


(declare remaining)


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


;;;;;;;;;; Helper functions


(defn garanteed?

  ""

  [view n-byte]

  (>= (remaining view)
      n-byte))



(defn remaining

  "Returns the number of bytes remaining until the end of the view."

  [view]

  (- (count view)
     (binf.protocol/position view)))


;;;;;


;#?(:cljs (defn view->data-view
;
;  ""
;
;  [view]
;
;  ))


;;;;;;;;;; Used by other namespaces for casting primitive types in CLJS


#?(:cljs (def ^:no-doc -view-cast (binf.protocol/view (binf.buffer/alloc 8))))
