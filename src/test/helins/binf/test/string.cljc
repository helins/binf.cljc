;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns helins.binf.test.string

  "Testing strings utilities"

  {:author "Adam Helinski"}

  (:require [clojure.test.check.generators :as tc.gen]
            [clojure.test.check.properties :as tc.prop]
            [helins.binf.string            :as binf.string]
            [helins.mprop                  :as mprop]))


;;;;;;;;;;


(mprop/deftest gen

  (tc.prop/for-all [string tc.gen/string]
    (= string
       (-> string
           binf.string/encode
           binf.string/decode))))
