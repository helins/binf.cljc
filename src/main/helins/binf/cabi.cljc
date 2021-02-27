;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns helins.binf.cabi

  "Base64 encoding and decoding."

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [array
                            struct]))


;;;;;;;;;;


(def word-32

  ""

  4)



(def word-64

  ""

  8)


;;;;;;;;;;


(defn name-get

  ""

  [member]

  (get member
       :binf.cabi/name))



(defn name-set

  ""
  
  [member name]

  (assoc member
         :binf.cabi/name
         name))


;;;;;;;;;;


(defn i8

  ""

  [name]

  (name-set {:binf.cabi/align  1
             :binf.cabi/n-byte 1
             :binf.cabi/type   'i8}
            name))



(defn i16

  ""

  [name]

  (name-set {:binf.cabi/align  2
             :binf.cabi/n-byte 2
             :binf.cabi/type   'i16}
            name))



(defn i32

  ""

  [name]

  (name-set {:binf.cabi/align  4
             :binf.cabi/n-byte 4
             :binf.cabi/type   'i32}
            name))



(defn i64

  ""

  [name]

  (name-set {:binf.cabi/align  8
             :binf.cabi/n-byte 8
             :binf.cabi/type   'i64}
            name))



(defn u8

  ""

  [name]

  (name-set {:binf.cabi/align  1
             :binf.cabi/n-byte 1
             :binf.cabi/type   'u8}
            name))



(defn u16

  ""

  [name]

  (name-set {:binf.cabi/align  2
             :binf.cabi/n-byte 2
             :binf.cabi/type   'u16}
            name))



(defn u32

  ""

  [name]

  (name-set {:binf.cabi/align  4
             :binf.cabi/n-byte 4
             :binf.cabi/type   'u32}
            name))



(defn u64

  ""

  [name]

  (name-set {:binf.cabi/align  8
             :binf.cabi/n-byte 8
             :binf.cabi/type   'u64}
            name))



(defn f32

  ""

  [name]

  (name-set {:binf.cabi/align  4
             :binf.cabi/n-byte 4
             :binf.cabi/type   'f32}
            name))



(defn f64

  ""

  [name]

  (name-set {:binf.cabi/align  8
             :binf.cabi/n-byte 8
             :binf.cabi/type   'f64}
            name))


;;;;;;;;;;


(defn array

  ""

  [member n]

  (-> member
      (update :binf.cabi/n-byte
              #(* n
                  %))
      (update :binf.cabi/type
              #(vector %
                       n))))

;;;;;;;;;;


(defn aligned

  ""

  [align offset]

  (let [mismatch (rem offset
                      align)]
    (if (zero? mismatch)
      offset
      (+ offset
         (- align
            mismatch)))))



(defn struct

  ""

  ([type max-align member+]

   (loop [align        1
          layout       []
          member-2+    member+
          name->member {}
          offset       0]
     (if (seq member-2+)
       (let [member        (first member-2+)
             member-align  (min max-align
                                (member :binf.cabi/align))
             member-offset (aligned member-align
                                    offset)
             member-name   (name-get member)]
         (recur (max align
                     member-align)
                (conj layout
                      member-name)
                (rest member-2+)
                (assoc name->member
                       member-name
                       (assoc member
                              :binf.cabi/align  member-align
                              :binf.cabi/offset member-offset))
                (+ member-offset
                   (member :binf.cabi/n-byte))))
       {:binf.cabi/align        align
        :binf.cabi/layout       layout
        :binf.cabi/n-byte       (aligned align
                                           offset)
        :binf.cabi/name->member name->member
        :binf.cabi/type         type})))


  ([type max-align name member+]

   (name-set (struct type
                     max-align
                     member+)
             name)))
