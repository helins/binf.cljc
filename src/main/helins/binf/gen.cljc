;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns helins.binf.gen

  ""

  {:author "Adam Helinski"}

  (:require [clojure.test.check.generators :as tc.gen]
            [helins.binf                   :as binf]
            [helins.binf.buffer            :as binf.buffer]))


;;;;;;;;;; Integers <= 32-bits


(def i8
     (tc.gen/choose -128
                    127))


(def i16
     (tc.gen/choose -32768
                    32768))


(def i32
     (tc.gen/choose -2147483648
                    2147483647))


(def u8
     (tc.gen/choose 0
                    255))


(def u16
     (tc.gen/choose 0
                    65535))


(def u32
     (tc.gen/choose 0
                    4294967295))


;;;;;;;;;; Integers 64-bits


(def i64
     #?(:clj  tc.gen/large-integer
        :cljs (tc.gen/fmap (fn [[a b]]
                             (js/BigInt.asIntN 64
                                               (str a
                                                    b)))
                           (tc.gen/tuple u32
                                         u32))))

(def u64
     #?(:clj  tc.gen/large-integer
        :cljs (tc.gen/fmap (fn [[a b]]
                             (js/BigInt.asUintN 64
                                                (str a
                                                     b)))
                           (tc.gen/tuple u32
                                         u32))))



;;;;;;;;;; Buffers


(def view
     (tc.gen/fmap (fn [u8+]
                    (let [view (binf/view (binf.buffer/alloc (count u8+)))]
                      (doseq [u8 u8+]
                        (binf/wr-b8 view
                                    u8))
                      (binf/seek view
                                 0)))
                  (tc.gen/vector u8)))



(def buffer
     (tc.gen/fmap binf/backing-buffer
                  view))
