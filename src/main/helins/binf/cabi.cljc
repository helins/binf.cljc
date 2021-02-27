;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns helins.binf.cabi

  "Base64 encoding and decoding."

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [array
                            struct]))


(declare force-env)


;;;;;;;;;;


(def sz-word16

  ""

  2)



(def sz-word32

  ""

  4)



(def sz-word64

  ""

  8)


;;;;;;;;;;


(defn env

  ""

  [sz-word]

  {:binf.cabi/align          sz-word
   :binf.cabi.pointer/n-byte sz-word})


;;;;;;;;;;


(defn force-align

  ""

  [f align]

  (fn merge-align [env-given]
    (f (assoc env-given
              :binf.cabi/align
              align))))



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



(defn ptr

  ""

  [type]

  (fn def-ptr [env]
    (assoc (primitive 'ptr
                      (env :binf.cabi.pointer/n-byte)
                      env)
           :binf.cabi.pointer/target
           (type env))))


;;;;;;;;;;


(defn array

  ""

  [element n-element]

  (fn def-array [env]
    (let [{:as             element-2
           :binf.cabi/keys [align
                            n-byte]} (element env)]
      {:binf.cabi/align           align
       :binf.cabi/n-byte          (* n-element
                                     n-byte)
       :binf.cabi/type            'array
       :binf.cabi.array/element   element-2
       :binf.cabi.array/n-element n-element})))


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

  (fn def-struct [env]
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
         :binf.cabi/type           'struct
         :binf.cabi.struct/layout  layout
         :binf.cabi.struct/member+ name->member
         :binf.cabi.struct/type    type}))))



(defn union

  ""

  [type member+]

  (fn def-union [env]
    (loop [align        1
           member-2+    member+
           n-byte       0
           name->member {}]
      (if (seq member-2+)
        (let [[member-name
               f-member]   (first member-2+)
              member       (f-member env)]
          (recur (max align
                      (member :binf.cabi/align))
                 (rest member-2+)
                 (max n-byte
                      (member :binf.cabi/n-byte))
                 (assoc name->member
                        member-name
                        member)))
        {:binf.cabi/align         align
         :binf.cabi/n-byte        n-byte
         :binf.cabi/type          'union
         :binf.cabi.union/member+ name->member
         :binf.cabi.union/type    type}))))
