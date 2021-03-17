;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns helins.binf.example

  "Basic example of creating a view over a buffer and writing a date."

  (:require [helins.binf        :as binf]
            [helins.binf.buffer :as binf.buffer]))


;;;;;;;;;; Functions for writing a date and reading it back


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


;;;;;;;;;; Allocating a buffer and creating a view


(def my-buffer

  "A buffer of 1024 bytes. Size depends on use case."

  (binf.buffer/alloc 1024))



(def my-view

  "One view created over our buffer.
  
   Several views can be created over one buffer, whole or parts of it."

   (binf/view my-buffer))


;;;;;;;;;; Eval interactively - Writing and reading a date


(comment


  ;; From the current position (0 for a new view), write date and read it back after coming
  ;; back to that position
  ;;
  (= [2021 3 16]

     (let [position-date (binf/position my-view)]
       (-> my-view
           (wr-date 2021
                    3
                    16)
           (binf/seek position-date)
           rr-date))))


;;;;;;;;;; Eval interactively - Buffers, subviews, and growing


(comment


  ;; When a view wraps a buffer, it can always be extracted.
  ;;
  (= my-buffer

     (binf/backing-buffer my-view))


  ;; Creating a subview of our view.
  ;; They both work on the same buffer but independantly.
  ;;
  ;; An offset of a 100 bytes with a window of 200 bytes.
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
  
  
  ;; Creating a new view which copies the original and add 256 additional bytes.
  ;; Position is preserved and copied as well.
  ;;
  (def my-bigger-view
       (binf/grow my-view
                  256))

  (= (+ 1024
        256)

     (binf/limit my-bigger-view)))
