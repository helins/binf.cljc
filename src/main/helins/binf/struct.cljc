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


(defn name-get

  ""

  [member]

  (get member
       :binf.struct/name))



(defn name-set

  ""
  
  [member name]

  (assoc member
         :binf.struct/name
         name))


;;;;;;;;;;


(defn i8

  ""

  [name]

  (name-set {:binf.struct/align  1
             :binf.struct/n-byte 1
             :binf.struct/type   'i8}
            name))



(defn i16

  ""

  [name]

  (name-set {:binf.struct/align  2
             :binf.struct/n-byte 2
             :binf.struct/type   'i16}
            name))



(defn i32

  ""

  [name]

  (name-set {:binf.struct/align  4
             :binf.struct/n-byte 4
             :binf.struct/type   'i32}
            name))



(defn i64

  ""

  [name]

  (name-set {:binf.struct/align  8
             :binf.struct/n-byte 8
             :binf.struct/type   'i64}
            name))



(defn u8

  ""

  [name]

  (name-set {:binf.struct/align  1
             :binf.struct/n-byte 1
             :binf.struct/type   'u8}
            name))



(defn u16

  ""

  [name]

  (name-set {:binf.struct/align  2
             :binf.struct/n-byte 2
             :binf.struct/type   'u16}
            name))



(defn u32

  ""

  [name]

  (name-set {:binf.struct/align  4
             :binf.struct/n-byte 4
             :binf.struct/type   'u32}
            name))



(defn u64

  ""

  [name]

  (name-set {:binf.struct/align  8
             :binf.struct/n-byte 8
             :binf.struct/type   'u64}
            name))



(defn f32

  ""

  [name]

  (name-set {:binf.struct/align  4
             :binf.struct/n-byte 4
             :binf.struct/type   'f32}
            name))



(defn f64

  ""

  [name]

  (name-set {:binf.struct/align  8
             :binf.struct/n-byte 8
             :binf.struct/type   'f64}
            name))


;;;;;;;;;;


(defn c

  ""

  [max-align member+]

  (loop [align        1
         layout       []
         member-2+    member+
         name->member {}
         offset       0]
    (if (seq member-2+)
      (let [member        (first member-2+)
            member-align  (min max-align
                               (member :binf.struct/align))
            member-offset (+ offset
                             (rem offset
                                  member-align))
            member-name   (name-get member)]
        (recur (max align
                    member-align)
               (conj layout
                     member-name)
               (rest member-2+)
               (assoc name->member
                      member-name
                      (assoc member
                             :binf.struct/offset
                             member-offset))
               (+ member-offset
                  (member :binf.struct/n-byte))))
      {:binf.struct/align        align
       :binf.struct/layout       layout
       :binf.struct/n-byte       (let [mismatch (rem offset
                                                     align)]
                                   (if (zero? mismatch)
                                     offset
                                     (+ offset
                                        (- align
                                           mismatch))))
       :binf.struct/name->member name->member})))
