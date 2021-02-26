;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns helins.binf.test.struct

  {:author "Adam Helins"}

  (:require [clojure.test       :as t]
            [helins.binf.struct :as binf.struct]))


;;;;;;;;;;


(t/deftest c-unnested

  (t/is (= {:align   8
            :offset+ [0
                      2
                      8]
            :n-byte  16}
           (binf.struct/c 8
                          [1
                           2
                           8])))

  (t/is (= {:align   4
            :offset+ [0
                      2
                      4
                      12]
            :n-byte  16}
           (binf.struct/c 4
                          [1
                           2
                           8
                           1])))
  )
