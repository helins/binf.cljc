;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns helins.binf.cabi

  "Interaction with environments that conforms to a C-like ABI.

   This namespace is mainly about defining C structures in order to pass them or to understand
   them when interacting with native functions. It could be when using JNI on the JVM or when
   calling WebAssembly functions in Clojurescript, for instance.

   This opens the road to interacting with languages like C++ and Rust since they allow for
   defining such C structures and commonly do so in the context of libraries.

   BinF already provides utilities for handling arbitrary data in native memory or in WebAssembly memory.
   For example, on the JVM, `DirectByteBuffer` implements view protocols and is commonly used with JNI.
   Some WebAssembly runtimes such as `Wasmer` use them to represent the memory of a WebAssembly module. In
   JS, WebAssembly memories are buffers from which a view can be built.

   What is missing is knowing how to use a C-like ABI which relies on following some rules. For instance,
   the members of a structure are aligned on specific memory addresses for performance reasons. This namespace
   provides utilities for defining such composite data structures as EDN and computing everything there is to compute
   for following those ABI rules.

   3 definitions are needed for understanding these utilities:

     - An `env` is a map containing information needed for computing those rules such as the alignment being used.
       See [[env]] for creating one.

     - A description map describes either a primitive type (eg. [[u32]]) or a composite data structure (eg. [[struct]]).

     - A description function takes an `env` and produces a description map.

   A description map is plain data containing eveything that is needed for handling this type. For instance,
   a description of a structure contains computed memory offsets of all its data fields.

   This namespace contains ready description functions for primitive types (such as [[f32]]) as well as functions
   for creating description functions for composite ones (such as [[array]] or [[struct]]).
  
   See [[struct]] for a small example and a full one in the `helins.binf.example.cabi` namespace from the repository.
   Source code also clearly shows what is being outputed. While daunting at first, this namespace is actually quite simple."

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [array
                            struct]))


(declare force-env)


;;;;;;;;;;


(defn env

  "Creates a minimal `env` map which describes the used alignment and the size of a pointer. Those
   informations are necessary for computing about any C type.
  
   This function leverages the fact that in modern architectures, alignment matches the size of pointers
   (eg. on 64-bit machine, alignment is 8 bytes and a pointer is 8 bytes as well)."

  [n-byte-word]

  {:binf.cabi/align          n-byte-word
   :binf.cabi.pointer/n-byte n-byte-word})


;;;;;;;;;;


(defn force-align

  "Sometimes, different alignments are used at the same time. For instance, an inner structure might have
   a different alignment than its outer structure. This function alters a description function in order to
   force a particular alignment.

   ```clojure
   ;; Forcing 4 byte alignment on the inner struct

   (struct :OuterStruct
           [:a f32]
           [:b (force-align (struct :InnerStruct
                                    [:inner-a i64])
                            4)])
   ```"

  [f align]

  (fn merge-align [env-given]
    (f (assoc env-given
              :binf.cabi/align
              align))))



(defn force-env

  "Akin to [[force-align]] but forces the given `env` over the one being used rather than merely the alignment."

  [f env]

  (fn merge-env [env-given]
    (f (merge env-given
              env))))


;;;;;;;;;;


(defn name-get

  "Given a description map, returns its name.

   As such, only members of a [[struct]] or a [[union]] have a name."

  [description-map]

  (get description-map
       :binf.cabi/name))



(defn name-set

  "Assoc'es a new name to a description map.

   See [[name-get]]."
  
  [member name]

  (assoc member
         :binf.cabi/name
         name))


;;;;;;;;;;


(defn aligned

  "Re-align `offset` given `align` (an alignment in bytes).

   Used by utilities in this namespace.

  
   ```clojure
   (aligned 8
            13)

   ;; 13 is re-aligned to 16, a multiple of 8
   ```"

  [align offset]

  (let [mismatch (rem offset
                      align)]
    (if (zero? mismatch)
      offset
      (+ offset
         (- align
            mismatch)))))


;;;;;;;;;;


(defn- -primitive

  ;; Helper for defining a primitive type.

  [type n-byte {:binf.cabi/keys [align]}]

  {:binf.cabi/align  (min align
                          n-byte)
   :binf.cabi/n-byte n-byte
   :binf.cabi/type   type})


;;;;;


(defn bool

  "Given an `env`, returns a map describing a boolean."

  [_env]

  {:binf.cabi/align  1
   :binf.cabi/n-byte 1
   :binf.cabi/type   :bool})



(defn i8

  "Given an `env`, returns a map describing a signed 8-bit integer."

  [_env]

  {:binf.cabi/align  1
   :binf.cabi/n-byte 1
   :binf.cabi/type   :i8})



(defn i16

  "Given an `env`, returns a map describing a signed 16-bit integer."

  [env]

  (-primitive :i16
              2
              env))



(defn i32

  "Given an `env`, returns a map describing a signed 32-bit integer."

  [env]

  (-primitive :i32
              4
              env))



(defn i64

  "Given an `env`, returns a map describing a signed 64-bit integer."

  [env]

  (-primitive :i64
              8
              env))



(defn u8

  "Given an `env`, returns a map describing an unsigned 8-bit integer."

  [_env]

  {:binf.cabi/align  1
   :binf.cabi/n-byte 1
   :binf.cabi/type   :u8})



(defn u16

  "Given an `env`, returns a map describing an unsigned 16-bit integer."

  [env]

  (-primitive :u16
              2
              env))



(defn u32

  "Given an `env`, returns a map describing an unsigned 32-bit integer."

  [env]

  (-primitive :u32
              4
              env))



(defn u64

  "Given an `env`, returns a map describing an unsigned 64-bit integer."

  [env]

  (-primitive :u64
              8
              env))



(defn f32

  "Given an `env`, returns a map describing a 32-bit floating value."

  [env]

  (-primitive :f32
              4
              env))



(defn f64

  "Given an `env`, returns a map describing a 64-bit floating value."

  [env]

  (-primitive :f64
              8
              env))



(defn ptr

  "Given a description function for a type, returns a function taking an `env` and returning
   a map describing a pointer for that type."

  [type]

  (fn def-ptr [env]
    (assoc (-primitive :ptr
                       (env :binf.cabi.pointer/n-byte)
                       env)
           :binf.cabi.pointer/target
           (type env))))


;;;;;;;;;;


(defn array

  "Given a description function for a type and a number of elements, returns a description function
   for an array."

  [description-fn n-element]

  (fn def-array [env]
    (let [{:as             element-2
           :binf.cabi/keys [align
                            n-byte]} (description-fn env)]
      {:binf.cabi/align           align
       :binf.cabi/n-byte          (* n-element
                                     n-byte)
       :binf.cabi/type            :array
       :binf.cabi.array/element   element-2
       :binf.cabi.array/n-element n-element})))


;;;;;;;;;;


(defn enum

  "Given a type name and a sequence of constants, returns a description function
   for an enum named as such and composed of those constants.

   A constant is either an identifier (string or keyword) or a vector `[identifier value]`.
   It looks exactly like defining an enum in C in that regard.

   Attention, enums are brittle from an ABI point of view and should be discouraged. For
   instance, the Google Fuchsia projects even explicitely bans them.
  
   ```clojure
   (def make-enum
        (enum :MyEnum
              [:a
               :b
               [:c 42]
               :d]))
   ```"

  [type constant+]

  (fn def-struct [env]
    (loop [constant-2+ constant+
           max-value   0
           tag->value  {}
           value       0]
      (if (seq constant-2+)
        (let [constant (first constant-2+)
              [constant-tag
               constant-value
               value-next]    (if (vector? constant)
                                (let [[constant-tag
                                       constant-value] constant]
                                  [constant-tag
                                   constant-value
                                   (inc constant-value)])
                                [constant
                                 value
                                 (inc value)])]
          (recur (rest constant-2+)
                 (max constant-value
                      max-value)
                 (assoc tag->value
                        constant-tag
                        constant-value)
                 value-next))
        {:binf.cabi/align          (min 4
                                        (env :binf.cabi/align))
         :binf.cabi/n-byte         4
         :binf.cabi/type           :enum
         :binf.cabi.enum/constant+ tag->value
         :binf.cabi.enum/type      type}))))



(defn struct

  "Given a type name and a sequence of members, returns a description function for a structure
   named as such and composed of those members.
  
   A member is a vector `[member-name description-function]`.

   The following example creates a structure containing a byte and an array of doubles. The resulting
   description function is then being passed an environment map were words are 8 bytes (ie. 64-bit machine).

   That description is plain data and contains computed offsets of all members.

   ```clojure
   (def make-struct
        (struct :MyStruct
                [[:a u8]
                 [:b (array f64
                            10)]]))

   (def my-struct-map
        (make-struct (env 8)))
   ```"

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
         :binf.cabi/type           :struct
         :binf.cabi.struct/layout  layout
         :binf.cabi.struct/member+ name->member
         :binf.cabi.struct/type    type}))))



(defn union

  "Given a type name and a sequence of members, returns a description function for
   a union named as such and composed of those members.
  
   A member is a vector `[member-name description-function]`.
  
   Behaves similarly to [[struct]]."

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
         :binf.cabi/type          :union
         :binf.cabi.union/member+ name->member
         :binf.cabi.union/type    type}))))
