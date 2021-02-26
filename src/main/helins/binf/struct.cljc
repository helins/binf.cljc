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


;;;;;;;;;;


(def i8

  ""

  {:binf.struct/align  1
   :binf.struct/n-byte 1
   :binf.struct/type   'i8})



(def i16

  ""

  {:binf.struct/align  2
   :binf.struct/n-byte 2
   :binf.struct/type   'i16})



(def i32

  ""

  {:binf.struct/align  4
   :binf.struct/n-byte 4
   :binf.struct/type   'i32})



(def i64

  ""

  {:binf.struct/align  8
   :binf.struct/n-byte 8
   :binf.struct/type   'i64})



(def u8

  ""

  {:binf.struct/align  1
   :binf.struct/n-byte 1
   :binf.struct/type   'u8})



(def u16

  ""

  {:binf.struct/align  2
   :binf.struct/n-byte 2
   :binf.struct/type   'u16})



(def u32

  ""

  {:binf.struct/align  4
   :binf.struct/n-byte 4
   :binf.struct/type   'u32})



(def u64

  ""

  {:binf.struct/align  8
   :binf.struct/n-byte 8
   :binf.struct/type   'u64})



(def f32

  ""

  {:binf.struct/align  4
   :binf.struct/n-byte 4
   :binf.struct/type   'f32})



(def f64

  ""

  {:binf.struct/align  8
   :binf.struct/n-byte 8
   :binf.struct/type   'f64})


;;;;;;;;;;


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
                               (member :binf.struct/align))
            member-offset (+ offset
                             (rem offset
                                  member-align))]
        (recur (max align
                    member-align)
               (+ member-offset
                  (member :binf.struct/n-byte))
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
