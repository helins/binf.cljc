;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns helins.binf.test.string

  ""

  {:author "Adam Helinski"}

  (:require [clojure.test       :as t]
            [helins.binf.string :as binf.string]))


;;;;;;;;;;


(def string
     "²é&\"'(§è!çà)-aertyuiopqsdfhgklmwcvbnùµ,;:=")



(t/deftest text

  (t/is (= string
           (-> string
               binf.string/encode
               binf.string/decode))))
