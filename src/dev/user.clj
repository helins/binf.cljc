;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns user

  "For daydreaming at the REPL."

  (:require [clojure.reflect]
            [clojure.pprint]
            [kaocha.repl]
            [helins.binf       :as binf]
            [helins.binf.dev]))


(set! *warn-on-reflection*
      true)


;;;;;;;;;;


(comment


  (kaocha.repl/run :jvm)


  (->> (binf/view-native 42)
       clojure.reflect/reflect
       :members
       (filter #(contains? (:flags %)
                           :private))
       clojure.pprint/pprint)


  )
