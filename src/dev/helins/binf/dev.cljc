(ns helins.binf.dev

  "For daydreaming in the repl."

  (:require [helins.binf        :as binf]
            [helins.binf.base64 :as binf.base64]
            [helins.binf.int    :as binf.int]
            [helins.binf.int64  :as binf.int64]))


;;;;;;;;;;


(comment


  (.grow js/mem
         1)

  (.-byteLength (.-buffer js/mem))

  (set! js/mem
        (js/WebAssembly.Memory. #js {"initial" 1}))


  (set! js/v
        (binf/view (.-buffer js/mem)))



  
  (def v
       (binf/view (binf/buffer-shared 32)))


  (binf/wa-b16 v
               0
               4)

  (binf/ra-u16 v
               0)


  (binf/ra-buffer v
                  0
                  10)

  (binf/wa-buffer v
                  2
                  (binf/to-buffer v)
                  0
                  4)

  (binf/wa-string v
                  0
                  "coucou")







 ; (-> (js/WebAssembly.instantiateStreaming (js/fetch "lib.wasm")
 ;                                          #js {"env" #js {"rand" js/Math.random}})
 ;     (.then (fn [o]
 ;              (println :ok)
 ;              (def ex
 ;                   (-> o
 ;                       .-instance
 ;                       .-exports)))))


 ; (.add_one ex
 ;           42)



  
  (binf.int64/i8 (js/BigInt -1))
  (binf.int64/u8 -1)




  (defn -sym

    [sym]

    (symbol (str "$"
                 sym)))


  (defn param

    [arg]

    (list* 'param
           arg))


  (defn i32

    ([]

     ['i32])

    ([sym]

     [(-sym sym)
      'i32]))



  (defn func

    [sym param+ body]

    `(~'func ~(-sym sym)
             ~@(map param
                    param+)
             ~@body))

       
  (func 'my-func
        [(i32 'foo)
         (i32 'bar)]
        nil)



  (defn i32-add

    [a b]

    (list 'i32.add
          a
          b))


  )
