(ns helins.binf.dev

  "For daydreaming in the repl."

  (:require [helins.binf :as binf]))


;;;;;;;;;;


(comment

  (.grow js/mem
         1)

  (.-byteLength (.-buffer js/mem))

  (set! js/mem
        (js/WebAssembly.Memory. #js {"initial" 1}))


  (set! js/v
        (binf/view (.-buffer js/mem)))


  )
