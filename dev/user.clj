(ns user

  "For daydreaming in the repl."

  (:require [helins.binf      :as binf]
            [helins.binf-test :as binf-test])
  (:import (java.nio ByteBuffer
                     ByteOrder)))


;;;;;;;;;;


(require '[nrepl.server])  (defonce server (nrepl.server/start-server :port 4000))

(set! *warn-on-reflection*
      true)


(comment



  )
