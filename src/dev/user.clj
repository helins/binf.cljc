(ns user

  "For daydreaming at the REPL."

  (:require [kaocha.repl]))


(set! *warn-on-reflection*
      true)


;;;;;;;;;;


(comment


  (kaocha.repl/run :jvm)

  )
