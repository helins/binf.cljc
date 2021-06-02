;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns helins.binf.dev

  "For daydreaming at the REPL."

  (:require [clojure.pprint]
            [helins.binf                :as binf]
            [helins.binf.base64         :as binf.base64]
            [helins.binf.buffer         :as binf.buffer]
			[helins.binf.cabi			:as binf.cabi]
            [helins.binf.endian         :as binf.endian]
            [helins.binf.float          :as binf.float]
            [helins.binf.gen            :as binf.gen]
            [helins.binf.int            :as binf.int]
            [helins.binf.int64          :as binf.int64]
            [helins.binf.leb128         :as binf.leb128]
            #?(:clj [helins.binf.native :as binf.native])
            [helins.binf.protocol       :as binf.protocol]
            [helins.binf.protocol.impl  :as binf.protocol.impl]
            [helins.binf.string         :as binf.string]
            [helins.binf.test           :as binf.test]
            [helins.binf.test.base64    :as binf.test.base64]
            [helins.binf.test.cabi      :as binf.test.cabi])
  #?(:clj (:import java.io.RandomAccessFile
                   java.nio.channels.FileChannel$MapMode)))


;;;;;;;;;;


(comment

  
  (def v
       (-> (binf.buffer/alloc 64)
           binf/view))

  (binf/wa-f32 v
               0
               ##NaN)
  (binf/ra-f32 v
               0)

  )
