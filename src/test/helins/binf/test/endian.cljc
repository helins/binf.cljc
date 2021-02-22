(ns helins.binf.test.endian

  ""

  {:author "Adam Helinski"}

  (:require [clojure.test       :as t]
            [helins.binf.int64  :as binf.int64]
            [helins.binf.endian :as binf.endian]))


;;;;;;;;;;


(t/deftest main

  (t/is (= 0x01234
           (binf.endian/b16 0x3412))
        "16-bit")
  
  (t/is (= 0x11223344
           (binf.endian/b32 0x44332211))
        "32-bit")
  (t/is (= (binf.int64/u* 0x1122334455667788)
           (binf.endian/b64 (binf.int64/u* 0x8877665544332211)))
        "64-bit"))
