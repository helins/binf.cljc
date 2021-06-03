;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns helins.binf.test.float

  {:author "Adam Helins"}

  (:require [clojure.test.check.clojure-test :as TC.ct]
            [clojure.test.check.properties   :as TC.prop]
            [helins.binf.float               :as binf.float]
            [helins.binf.gen                 :as binf.gen]))


;;;;;;;;;;


(TC.ct/defspec f32

  (TC.prop/for-all [x binf.gen/f32]
    (binf.float/= x
                  (binf.float/from-b32 (binf.float/b32 x)))))



(TC.ct/defspec f64

  (TC.prop/for-all [x binf.gen/f64]
    (binf.float/= x
                  (binf.float/from-b64 (binf.float/b64 x)))))



(TC.ct/defspec f32<->f64

  (TC.prop/for-all [x binf.gen/f32]
    (binf.float/= x
                  (-> x
                      binf.float/f64
                      binf.float/f32))))
