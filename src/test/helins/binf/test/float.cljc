;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns helins.binf.test.float

  {:author "Adam Helins"}

  (:require [clojure.test      :as t]
            [helins.binf.float :as binf.float]))


;;;;;;;;;;


#?(:clj (t/deftest f32

  ; JS does not have real floats, imprecision arise when they get converted automatically to f64.
  ; Other than that, the implementation is technically correct.

  (t/is (= (float 42.42)
           (binf.float/from-b32 (binf.float/b32 42.42)))
        "f32")))



(t/deftest f64

  (t/is (= 42.42
           (binf.float/from-b64 (binf.float/b64 42.42)))
        "f64"))
