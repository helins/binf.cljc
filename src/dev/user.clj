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
