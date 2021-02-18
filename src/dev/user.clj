(ns user

  "For daydreaming at the REPL."

  (:require [clojure.edn]
            [clojure.pprint]
            [helins.binf :as binf])
  (:import java.nio.ByteBuffer))


;;;;;;;;;;


(comment


  (-> (quote
        (module
          (type $t0 (func (result f64)))
          (type $t1 (func (param i32) (result f64)))
          (import "env" "rand" (func $rand (type $t0)))
          (func $add_one (type $t1) (param $p0 i32) (result f64)
            (f64.add
              (call $rand)
              (f64.convert_i32_s
                (local.get $p0))))
          (table $T0 1 1 funcref)
          (memory $memory 16)
          (global $g0 (mut i32) (i32.const 4654616516515115165151561616161651 #_1048576))
          (global $__data_end i32 (i32.const 1048576))
          (global $__heap_base i32 (i32.const 1048576))
          (export "memory" (memory 0))
          (export "add_one" (func $add_one))
          (export "__data_end" (global 1))
          (export "__heap_base" (global 2))))
      str
      clojure.edn/read-string
      clojure.pprint/pprint)



  )
