(ns user

  "For daydreaming at the REPL."

  (:require [kaocha.repl]
            [helins.binf.dev]))


(set! *warn-on-reflection*
      true)


;;;;;;;;;;


(comment


  (kaocha.repl/run :jvm)

  )
