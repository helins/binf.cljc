;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns helins.binf.dev

  "For daydreaming in the repl."

  (:require [helins.binf                :as binf]
            [helins.binf.base64         :as binf.base64]
            [helins.binf.buffer         :as binf.buffer]
			[helins.binf.cabi			:as binf.cabi]
            [helins.binf.endian         :as binf.endian]
            [helins.binf.float          :as binf.float]
            [helins.binf.int            :as binf.int]
            [helins.binf.int64          :as binf.int64]
            [helins.binf.leb128         :as binf.leb128]
            #?(:clj [helins.binf.native :as binf.native])
            [helins.binf.protocol       :as binf.protocol]
            [helins.binf.protocol.impl  :as binf.protocol.impl]
            [helins.binf.string         :as binf.string]
            [helins.binf.test           :as binf.test]
            [helins.binf.test.base64    :as binf.test.base64]
            [helins.binf.test.cabi      :as binf.test.cabi]))


;;;;;;;;;;


(comment


 ; (.grow js/mem
 ;        1)

 ; (.-byteLength (.-buffer js/mem))

 ; (set! js/mem
 ;       (js/WebAssembly.Memory. #js {"initial" 1}))


  (set! js/v
        (binf/view (.-buffer js/mem)))



  (def v
       (binf/view (binf.buffer/alloc 128)))


  (binf.leb128/rr-i32 (-> (binf/view (binf.buffer/alloc 10))
                          (binf/wa-b8 0
                                      0x7F
                                      #_(binf.int/u8 0x7F)))
                      #_33)


  (def v
       (binf/view (binf.buffer/alloc 42)))

  (-> v
      (binf/wr-b32 42)
      binf/view
      binf/position)


  )
