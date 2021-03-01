;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns helins.binf.test.leb128

  {:author "Adam Helins"}

  (:require [clojure.test       :as t]
            [helins.binf        :as binf]
            [helins.binf.buffer :as binf.buffer]
            [helins.binf.int64  :as binf.int64]
            [helins.binf.leb128 :as binf.leb128]))


;;;;;;;;;; int32


(t/deftest u32

  (let [v (binf/view (binf.buffer/alloc 32))]

    (t/is (= 0
             (-> v
                 (binf/seek 0)
                 (binf.leb128/wr-u32 0)
                 (binf/seek 0)
                 (binf.leb128/rr-u32))))

    (t/is (= 4294967295
             (-> v
                 (binf/seek 0)
                 (binf.leb128/wr-u32 4294967295)
                 (binf/seek 0)
                 (binf.leb128/rr-u32))))))



(t/deftest i32

  (let [v (binf/view (binf.buffer/alloc 32))]

    (t/is (= 0
             (-> v
                 (binf/seek 0)
                 (binf.leb128/wr-i32 0)
                 (binf/seek 0)
                 (binf.leb128/rr-i32))))

    (t/is (= 2147483647
             (-> v
                 (binf/seek 0)
                 (binf.leb128/wr-i32 2147483647)
                 (binf/seek 0)
                 (binf.leb128/rr-i32))))

    (t/is (= -2147483648
             (-> v
                 (binf/seek 0)
                 (binf.leb128/wr-i32 -2147483648)
                 (binf/seek 0)
                 (binf.leb128/rr-i32))))

    (t/is (= -42
             (-> v
                 (binf/seek 0)
                 (binf.leb128/wr-i32 -42)
                 (binf/seek 0)
                 (binf.leb128/rr-i32))))))


;;;;;;;;;; int64


(t/deftest u64

  (let [v (binf/view (binf.buffer/alloc 32))]

    (t/is (= (binf.int64/u* 0)
             (-> v
                 (binf/seek 0)
                 (binf.leb128/wr-u64 (binf.int64/u* 0))
                 (binf/seek 0)
                 (binf.leb128/rr-u64))))

    (t/is (= (binf.int64/u* 18446744073709551615)
             (-> v
                 (binf/seek 0)
                 (binf.leb128/wr-u64 (binf.int64/u* 18446744073709551615))
                 (binf/seek 0)
                 (binf.leb128/rr-u64))))))



(t/deftest i64

  (let [v (binf/view (binf.buffer/alloc 32))]

    (t/is (= (binf.int64/i* 0)
             (-> v
                 (binf/seek 0)
                 (binf.leb128/wr-i64 (binf.int64/i* 0))
                 (binf/seek 0)
                 (binf.leb128/rr-i64))))

    (t/is (= (binf.int64/i* 9223372036854775807)
             (-> v
                 (binf/seek 0)
                 (binf.leb128/wr-i64 (binf.int64/i* 9223372036854775807))
                 (binf/seek 0)
                 (binf.leb128/rr-i64))))

    (t/is (= (binf.int64/i* -9223372036854775808)
             (-> v
                 (binf/seek 0)
                 (binf.leb128/wr-i64 (binf.int64/i* -9223372036854775808))
                 (binf/seek 0)
                 (binf.leb128/rr-i64))))))
