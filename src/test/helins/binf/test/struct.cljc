;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns helins.binf.test.struct

  {:author "Adam Helins"}

  (:require [clojure.test       :as t]
            [helins.binf.struct :as binf.struct]))


;;;;;;;;;;


(t/deftest unnested

  (t/is (= {:align   8
            :offset+ [0
                      2
                      8]
            :n-byte  16}
           (binf.struct/c 8
                          [binf.struct/u8
                           binf.struct/i16
                           binf.struct/f64])))

  (t/is (= {:align   4
            :offset+ [0
                      2
                      4
                      12]
            :n-byte  16}
           (binf.struct/c 4
                          [binf.struct/i8
                           binf.struct/u16
                           binf.struct/i64
                           binf.struct/u8])))
  )
