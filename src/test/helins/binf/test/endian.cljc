;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns helins.binf.test.endian

  "Testing endianess utilities."

  {:author "Adam Helinski"}

  (:require [clojure.test                  :as T]
            [clojure.test.check.properties :as tc.prop]
            [helins.binf.int               :as binf.int]
            [helins.binf.int64             :as binf.int64]
            [helins.binf.endian            :as binf.endian]
            [helins.binf.gen               :as binf.gen]
            [helins.mprop                  :as mprop]))


;;;;;;;;;;


(T/deftest main

  (T/is (= 0x01234
           (binf.endian/b16 0x3412))
        "16-bit")
  
  (T/is (= 0x11223344
           (binf.endian/b32 0x44332211))
        "32-bit")
  (T/is (= (binf.int64/u* 0x1122334455667788)
           (binf.endian/b64 (binf.int64/u* 0x8877665544332211)))
        "64-bit"))


;;;;;;;;;; Generative


(mprop/deftest b16

  {:ratio-num 150}

  (tc.prop/for-all [u16 binf.gen/u16]
    (= u16
       (-> u16
           binf.endian/b16
           binf.endian/b16
           binf.int/u16))))



(mprop/deftest b32

  {:ratio-num 150}

  (tc.prop/for-all [u32 binf.gen/u32]
    (= u32
       (-> u32
           binf.endian/b32
           binf.endian/b32
           binf.int/u32))))



(mprop/deftest b64

  {:ratio-num 150}

  (tc.prop/for-all [u64 binf.gen/u64]
    (= u64
       (-> u64
           binf.endian/b64
           binf.endian/b64
           binf.int64/u*))))

