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


(defn force-env

  ""

  [f env]

  (fn merge-env [env-given]
    (f (merge env-given
              env))))


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


(defn primitive

  ;;

  [type n-byte {:binf.cabi/keys [align]}]

  {:binf.cabi/align  (min align
                          n-byte)
   :binf.cabi/n-byte n-byte
   :binf.cabi/type   type})


;;;;;


(defn i8

  ""

  [_env]

  {:binf.cabi/align  1
   :binf.cabi/n-byte 1
   :binf.cabi/type   'i8})



(defn i16

  ""

  [env]

  (primitive 'i16
             2
             env))



(defn i32

  ""

  [env]

  (primitive 'i32
             4
             env))



(defn i64

  ""

  [env]

  (primitive 'i64
             8
             env))



(defn u8

  ""

  [_env]

  {:binf.cabi/align  1
   :binf.cabi/n-byte 1
   :binf.cabi/type   'u8})



(defn u16

  ""

  [env]

  (primitive 'u16
             2
             env))



(defn u32

  ""

  [env]

  (primitive 'u32
             4
             env))



(defn u64

  ""

  [env]

  (primitive 'u64
             8
             env))



(defn f32

  ""

  [env]

  (primitive 'f32
             4
             env))



(defn f64

  ""

  [env]

  (primitive 'f64
             8
             env))


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

  [type member+]

  (fn make-struct [env]
    (loop [align        1
           layout       []
           member-2+    member+
           name->member {}
           offset       0]
      (if (seq member-2+)
        (let [[member-name
               f-member]    (first member-2+)
              member        (f-member env)
              member-align  (member :binf.cabi/align)
              member-offset (aligned member-align
                                     offset)]
          (recur (max align
                      member-align)
                 (conj layout
                       member-name)
                 (rest member-2+)
                 (assoc name->member
                        member-name
                        (assoc member
                               :binf.cabi/offset
                               member-offset))
                 (+ member-offset
                    (member :binf.cabi/n-byte))))
        {:binf.cabi/align          align
         :binf.cabi/n-byte         (aligned align
                                              offset)
         :binf.cabi/type           type
         :binf.cabi.struct/layout  layout
         :binf.cabi.struct/member+ name->member}))))
