;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns helins.binf.test.base64

  {:author "Adam Helins"}

  (:require [clojure.test                    :as t]
            [clojure.test.check.clojure-test :as TC.ct]
            [clojure.test.check.generators   :as TC.gen]
            [clojure.test.check.properties   :as TC.prop]
            [helins.binf                     :as binf]
            [helins.binf.base64              :as binf.base64]
            [helins.binf.buffer              :as binf.buffer]
            [helins.binf.gen                 :as binf.gen]))


;;;;;;;;;; Helpers


(defn to-seq

  "Reads the given `view` as a buffer relatively until its end and returns it as a sequence."

  [view]

  (seq (binf/rr-buffer view
                       (binf/limit view))))


;;;;;;;;;; Tests


(TC.ct/defspec main

  (TC.prop/for-all [[buffer
                     offset
                     n-byte] (TC.gen/let [buffer (binf.gen/buffer)
                                          offset (TC.gen/choose 0
                                                                (binf/limit buffer))
                                          n-byte (TC.gen/choose 0
                                                                (- (binf/limit buffer)
                                                                   offset))]
                               [buffer
                                offset
                                n-byte])]
    (and (= (seq buffer)
            (to-seq (-> buffer
                        binf.base64/encode
                        binf.base64/decode)))
         (= (seq (drop offset
                       buffer))
            (to-seq (-> buffer
                        (binf.base64/encode offset)
                        binf.base64/decode)))
         (= (seq (->> buffer
                      (drop offset)
                      (take n-byte)))
            (to-seq (-> buffer
                        (binf.base64/encode offset
                                            n-byte)
                        binf.base64/decode))))))
