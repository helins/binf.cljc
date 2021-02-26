;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns user

  "For daydreaming at the REPL."

  (:require [clojure.reflect]
            [clojure.pprint]
            [kaocha.repl]
            [helins.binf       :as binf]
            [helins.binf.dev]
            [helins.binf.int64 :as binf.int64]))


(set! *warn-on-reflection*
      true)


;;;;;;;;;;


(comment


  (kaocha.repl/run :jvm)


  (unsigned-bit-shift-right (unchecked-long -4)
                            1)

  )
