;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns helins.binf.leb128

  ""

  {:author "Adam Helins"}

  (:require [helins.binf.int      :as binf.int]
            [helins.binf.int64    :as binf.int64]
            [helins.binf.protocol :as binf.protocol])
  (:refer-clojure :rename {bit-shift-left           <<
                           bit-shift-right          >>
                           unsigned-bit-shift-right >>>}))


;;;;;;;;;; Miscellaneous


(defn n-byte-max

  ""

  [n-bit]

  (long (Math/ceil (double (/ n-bit
                              7)))))



(defn n-byte-i32

  ""

  [i32]

  (loop [i32-2  i32
         n-byte 1]
    (let [b8    (bit-and i32-2
                         0x7f)
          i32-3 (>> i32-2
                    7)]
      (if (or (and (zero? i32-3)
                   (zero? (bit-and b8
                                   0x40)))
              (and (= i32-3
                      -1)
                   (not (zero? (bit-and b8
                                        0x40)))))
        n-byte
        (recur i32-3
               (inc n-byte))))))



(defn n-byte-u32

  ""

  [u32]

  (loop [n-byte 1
         u32-2  u32]
    (let [u32-3 (>>> u32-2
                     7)]
      (if (zero? u32-3)
        n-byte
        (recur (inc n-byte)
               u32-3)))))



(defn n-byte-i64

  ""

  [i64]

  (loop [i64-2  i64
         n-byte 1]
    (let [b8    (bit-and i64-2
                         (binf.int64/i* 0x7f))
          i64-3 (>> i64-2
                    (binf.int64/i* 7))]
      (if (or (and (= (binf.int64/i* 0)
                      i64-3)
                   (= (binf.int64/i* 0)
                      (bit-and b8
                               (binf.int64/i* 0x40))))
              (and (= i64-3
                      (binf.int64/i* -1))
                   (not= (binf.int64/i* 0)
                         (bit-and b8
                                  (binf.int64/i* 0x40)))))
        n-byte
        (recur i64-3
               (inc n-byte))))))



(defn n-byte-u64

  ""

  [u64]

  (loop [n-byte 1
         u64-2  u64]
    (let [u64-3 (binf.int64/u>> u64-2
                                (binf.int64/u* 7))]
      (if (= (binf.int64/u* 0)
             u64-3)
        n-byte
        (recur (inc n-byte)
               u64-3)))))


;;;;;;;;;; i32


(defn rr-i32

  ""


  ([view]

   (rr-i32 view
           32))


  ([view n-bit]

   (loop [i32   0
          shift 0]
     (let [b8      (binf.protocol/rr-u8 view)
           i32-2   (bit-or i32
                           (<< (bit-and b8
                                        0x7f)
                               shift))
           shift-2 (+ shift
                      7)]
       (if (zero? (bit-and b8
                           0x80))
         (if (and (< shift-2
                     n-bit)
                  (not (zero? (bit-and b8
                                       0x40))))
           (binf.int/i32 (bit-or i32-2
                                 (<< -1
                                     shift-2)))
           (binf.int/i32 i32-2))
         (recur i32-2
                shift-2))))))



(defn wr-i32

  ""

  [view i32]

  (loop [i32-2 i32]
    (let [b8    (bit-and i32-2
                         0x7f)
          i32-3 (>> i32-2
                    7)]
      (if (or (and (zero? i32-3)
                   (zero? (bit-and b8
                                   0x40)))
              (and (= i32-3
                      -1)
                   (not (zero? (bit-and b8
                                        0x40)))))
        (do
          (binf.protocol/wr-b8 view
                               b8)
          view)
        (do
          (binf.protocol/wr-b8 view
                               (bit-or b8
                                       0x80))
          (recur i32-3))))))


;;;;;;;;;; u32


(defn rr-u32

  ""

  [view]

  (loop [u32   0
         shift 0]
    (let [b8    (binf.protocol/rr-u8 view)
          u32-2 (bit-or u32
                        (<< (bit-and b8
                                     0x7f)
                            shift))]
      (if (zero? (bit-and b8
                          0x80))
        #?(:clj  u32-2
           :cljs (binf.int/u32 u32-2))
        (recur u32-2
               (+ shift
                  7))))))



(defn wr-u32

  ""

  [view u32]

  (loop [u32-2 u32]
    (let [b8    (bit-and u32-2
                         0x7f)
          u32-3 (>>> u32-2
                     7)]
      (if (zero? u32-3)
        (do
          (binf.protocol/wr-b8 view
                               b8)
          view)
        (do
          (binf.protocol/wr-b8 view
                               (bit-or b8
                                       0x80))
          (recur u32-3))))))


;;;;;;;;;; u64


(defn rr-u64

  ""

  [view]

  (loop [u64   (binf.int64/u* 0)
         shift (binf.int64/u* 0)]
    (let [b8    (binf.protocol/rr-u8 view)
          u64-2 (bit-or u64
                        (<< (binf.int64/u* (bit-and b8
                                                    0x7f))
                            shift))]
      (if (zero? (bit-and b8
                          0x80))
        u64-2
        (recur u64-2
               (+ shift
                  (binf.int64/u* 7)))))))



(defn wr-u64

  ""

  [view u64]

  (loop [u64-2 u64]
    (let [b8    (bit-and u64-2
                         (binf.int64/u* 0x7f))
          u64-3 (binf.int64/u>> u64-2
                                (binf.int64/u* 7))]
      (if (= (binf.int64/u* 0)
             u64-3)
        (do
          (binf.protocol/wr-b8 view
                               (binf.int64/u8 b8))
          view)
        (do
          (binf.protocol/wr-b8 view
                               (binf.int64/u8 (bit-or b8
                                                      (binf.int64/u* 0x80))))
          (recur u64-3))))))


;;;;;;;;;; i64


(defn rr-i64

  ""


  ([view]

   (rr-i64 view
           64))


  ([view n-bit]

   (loop [i64   (binf.int64/u* 0)
          shift (binf.int64/u* 0)]
     (let [b8      (binf.protocol/rr-u8 view)
           i64-2   (bit-or i64
                           (<< (binf.int64/u* (bit-and b8
                                                       0x7f))
                               shift))
           shift-2 (+ shift
                      (binf.int64/u* 7))]
       (if (zero? (bit-and b8
                           0x80))
         (binf.int64/i* (if (and (< shift-2
                                    (binf.int64/u* n-bit))
                                 (not (zero? (bit-and b8
                                                      0x40))))
                          (bit-or i64-2
                                  (<< (binf.int64/i* -1)
                                      shift-2))
                          i64-2))
         (recur i64-2
                shift-2))))))



(defn wr-i64

  ""

  [view i64]

  (loop [i64-2 i64]
    (let [b8    (bit-and i64-2
                         (binf.int64/i* 0x7f))
          i64-3 (>> i64-2
                    (binf.int64/i* 7))]
      (if (or (and (= (binf.int64/i* 0)
                      i64-3)
                   (= (binf.int64/i* 0)
                      (bit-and b8
                               (binf.int64/i* 0x40))))
              (and (= i64-3
                      (binf.int64/i* -1))
                   (not= (binf.int64/i* 0)
                         (bit-and b8
                                  (binf.int64/i* 0x40)))))
        (do
          (binf.protocol/wr-b8 view
                               (binf.int64/u8 b8))
          view)
        (do
          (binf.protocol/wr-b8 view
                               (bit-or (binf.int64/u8 b8)
                                       0x80))
          (recur i64-3))))))
