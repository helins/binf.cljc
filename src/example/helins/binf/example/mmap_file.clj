;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns helins.binf.example.mmap-file

  "Using BinF for writing and reading to a memory-mapped file on the JVM.
  
   BinF protocols are implemented for ByteBuffer, parent of MappedByteBuffer."

  ;; We shall reuse our functions for writing and reading a date.
  ;;
  (:require [helins.binf         :as binf]
            [helins.binf.example :as binf.example])
  (:import java.io.RandomAccessFile
           java.nio.channels.FileChannel$MapMode))


;;;;;;;;;; Eval interactively


(comment

  ;; Writing a date to '/tmp/binf-example.dat' and reading it back.
  ;;
  (= [2021 3 16]

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
             (binf.example/wr-date 2021
                                   3
                                   16)
             ;; Ensuring changes are persisted on disk
             .force
             ;; Reading it back from the start of the file
             (binf/seek 0)
             binf.example/rr-date)))))
