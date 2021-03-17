;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns helins.binf.example.cabi

  "Examples of defining C structures as EDN data and using this information
   to read and write a view."

  (:require [clojure.pprint]
            [helins.binf                :as binf]
			[helins.binf.cabi			:as binf.cabi]
            #?(:clj [helins.binf.native :as binf.native])))


;;;;;;;;;; Preparing things


(def env32

  "An environment map which describes how to compute composite types.
  
   Here, we specify a word size of 4 bytes because we supposedly target WebAssembly
   which is 32-bit."

   (binf.cabi/env 4))



(def fn-struct-date

  "We create a function which given an environment map, returns EDN describing
   a C structure for a date as used in the `helins.binf.example` namespace.
  
   The `helins.binf.example` examples write and read dates in a \"packed format\".
   
   C structure are represented diffently. Space is added in order to make accessing
   data more efficient. That is why we need to compute a few things."

   (binf.cabi/struct :MyDate
                     [[:year  binf.cabi/u16]
                      [:month binf.cabi/u8]
                      [:day   binf.cabi/u8]]))

(def struct-date
  
  "Now we use our `fn-struct-date` with `env32` in order to compute an EDN representation
   for our struct which is suitable for a 32-bit environment."

  (fn-struct-date env32))



(def view

  "On the JVM, our view is actually a `DirectByteBuffer` which we could use, for instance,
   with JNI.

   In JS, we wrap a buffer which is WebAssembly memory. In a real application we would then
   instantiate a WebAssembly module using that memory object."

  #?(:clj  (binf.native/view 1024)
     :cljs (-> (js/WebAssembly.Memory. #js {"initial" 1})
               .-buffer
               binf/view)))


;;;;;;;;;; Creating functions for writing and reading a date using information from our struct.
;;;;;;;;;;
;;;;;;;;;; Depending on the use-case, helpers should be written.
;;;;;;;;;; Here, we show the whole work.


(let [member+      (get struct-date
                        :binf.cabi.struct/member+)
      offset-year  (get-in member+
                           [:year
                            :binf.cabi/offset])
      offset-month (get-in member+
                           [:month
                            :binf.cabi/offset])
      offset-day   (get-in member+
                           [:day
                            :binf.cabi/offset])]


  (defn ra-date

    "Reads a date from an absolute position."

    [view position]

    [(binf/ra-u16 view
                  (+ position
                     offset-year))
     (binf/ra-u8 view
                 (+ position
                    offset-month))
     (binf/ra-u8 view
                 (+ position
                    offset-day))])


  (defn wa-date

    "Writes a date at an absolute position."

    [view position year month day]

    (-> view
        (binf/wa-b16 (+ position
                        offset-year)
                     year)
        (binf/wa-b8 (+ position
                       offset-month)
                    month)
        (binf/wa-b8 (+ position
                       offset-day)
                    day))))


;;;;;;;;;; Eval interactively - Writing and reading a date


(comment

  ;; Having an idea of how things look.

  (= env32

     {:binf.cabi/align          4
      :binf.cabi.pointer/n-byte 4})


  (fn? fn-struct-date)


  (= struct-date

     {:binf.cabi/align          2
      :binf.cabi/n-byte         4
      :binf.cabi/type           :struct
      :binf.cabi.struct/layout  [:year
                                 :month
                                 :day]
      :binf.cabi.struct/member+ {:day   {:binf.cabi/align  1
                                         :binf.cabi/n-byte 1
                                         :binf.cabi/offset 3
                                         :binf.cabi/type   :u8}
                                 :month {:binf.cabi/align  1
                                         :binf.cabi/n-byte 1
                                         :binf.cabi/offset 2
                                         :binf.cabi/type  :u8}
                                 :year {:binf.cabi/align  2
                                        :binf.cabi/n-byte 2
                                        :binf.cabi/offset 0 
                                        :binf.cabi/type   :u16}}
      :binf.cabi.struct/type    :MyDate})


  ;; Writing and reading a date at position 0 in our view

  (= [2021 3 16]

     (-> view
         (wa-date 0
                  2021
                  3
                  16)
         (ra-date 0))))


;;;;;;;;;; Eval interactively - A more complex C struct


(comment

  (let [fn-complex-struct (binf.cabi/struct :ComplexExample
                                            [[:pointer_array (binf.cabi/array (binf.cabi/ptr binf.cabi/f64)
                                                                              10)]
                                             [:inner_struct  (binf.cabi/struct :InnerStruct
                                                                               [[:a_byte  binf.cabi/u8]
                                                                                [:a_union (binf.cabi/union :SomeUnion
                                                                                                           [[:int    binf.cabi/i32]
                                                                                                            [:double binf.cabi/f64]])]])]])]
    ;; Using our 32-bit environment map, let us print the EDN description of this struct.
    ;;
    (-> (fn-complex-struct env32)
        clojure.pprint/pprint)))
