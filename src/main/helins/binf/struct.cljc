;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns helins.binf.struct

  "Base64 encoding and decoding."

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [struct]))


;;;;;;;;;;


(def max-align-b32

  ""

  4)



(def max-align-b64

  ""

  8)



(defn c

  ""

  [max-align member+]

  (loop [align     1
         offset    0
         offset+   []
         member-2+ member+]
    (if (seq member-2+)
      (let [member        (first member-2+)
            member-align  (min max-align
                               member)
            member-offset (+ offset
                             (rem offset
                                  member-align))]
        (recur (max align
                    member-align)
               (+ member-offset
                  member)
               (conj offset+
                     member-offset)
               (rest member-2+)))
      {:align   align
       :offset+ offset+
       :n-byte  (let [mismatch (rem offset
                                    align)]
                  (if (zero? mismatch)
                    offset
                    (+ offset
                       (- align
                          mismatch))))})))
