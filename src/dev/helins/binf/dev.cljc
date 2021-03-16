;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns helins.binf.dev

  "For daydreaming in the repl."

  (:require [clojure.pprint]
            [helins.binf                :as binf]
            [helins.binf.base64         :as binf.base64]
            [helins.binf.buffer         :as binf.buffer]
			[helins.binf.cabi			:as binf.cabi]
            [helins.binf.endian         :as binf.endian]
            [helins.binf.float          :as binf.float]
            [helins.binf.gen            :as binf.gen]
            [helins.binf.int            :as binf.int]
            [helins.binf.int64          :as binf.int64]
            [helins.binf.leb128         :as binf.leb128]
            #?(:clj [helins.binf.native :as binf.native])
            [helins.binf.protocol       :as binf.protocol]
            [helins.binf.protocol.impl  :as binf.protocol.impl]
            [helins.binf.string         :as binf.string]
            [helins.binf.test           :as binf.test]
            [helins.binf.test.base64    :as binf.test.base64]
            [helins.binf.test.cabi      :as binf.test.cabi])
  #?(:clj (:import java.io.RandomAccessFile
                   java.nio.channels.FileChannel$MapMode)))




;;;;;;;;;;


(defn wr-date

  [view year month day]

  (-> view
      (binf/wr-b16 year)
      (binf/wr-b8 month)
      (binf/wr-b8 day)))



(defn rr-date

  [view]

  [(binf/rr-u16 view)
   (binf/rr-u8 view)
   (binf/rr-u8 view)])




(def my-buffer
     (binf.buffer/alloc 1024))

;; Wrapping the buffer in view
;;
(def my-view
     (binf/view my-buffer))

;; The buffer can always be extracted from its view
;;
(binf/backing-buffer my-view)


;; From the current position (0 for a new view)
;;
(let [position-date (binf/position my-view)]
  (-> my-view
      (wr-date 2021
               3
               16)
      (binf/seek position-date)
      rr-date))


;; => [2021 3 16]



;; An offset of a 100 bytes with a window of 200 bytes
;;
(def sub-view
     (binf/view my-view
                100
                200))

;; The position of that sub-view starts transparently at 0
;;
(= 0
   (binf/position sub-view))

(= 200
   (binf/limit sub-view))


(def my-view-2
     (binf/grow my-view
                (Math/ceil (* 1.5
                              (binf/limit my-view)))))


#?(:clj (comment


  
  (import 'java.io.RandomAccessFile
          'java.nio.channels.FileChannel$MapMode)

  (with-open [file (RandomAccessFile. "/tmp/binf-example.dat"
                                      "rw")]
    (let [view (-> file
                   .getChannel
                   (.map FileChannel$MapMode/READ_WRITE
                         ;; From byte 0 in the file
                         0
                         ;; A size in bytes, we know a date is 4 bytes
                         4))]
      (-> view
          ;; Writing date
          (wr-date 2021
                   3
                   16)
          ;; Ensuring changes are persisted on disk
          .force
          ;; Reading it back from the start of the file
          (binf/seek 0)
          rr-date)))





  (def view
       (-> (RandomAccessFile. "/tmp/binf-example.dat"
                              "rw")
           .getChannel
           (.map FileChannel$MapMode/READ_WRITE
                 0
                 1024)))
     
  (binf/seek view
             42)

  (def view-2
       (binf/grow view
                  10))





  (def env
       (binf.cabi/env 4))


  (def fn-struct-date
       (binf.cabi/struct :MyDates
                         [[:year  binf.cabi/u16]
                          [:month binf.cabi/u8]
                          [:date  binf.cabi/u8]]))

  (fn-struct-date env)



  (binf.cabi/struct :ComplexExample
                    [[:pointer_array (binf.cabi/array (binf.cabi/ptr binf.cabi/f64)
                                                      10)]
                     [:inner_struct  (binf.cabi/struct :InnerStruct
                                                       [[:a_byte  binf.cabi/u8]
                                                        [:a_union (binf.cabi/union :SomeUnion
                                                                                   [[:int    binf.cabi/i32]
                                                                                    [:double binf.cabi/f64]])]])]])



  ))
