(ns helins.binf.test.native

  ""

  {:author "Adam Helinski"}

  (:require [clojure.test       :as t]
            [helins.binf.native :as binf.native]))


;;;;;;;;;;


(def n-byte
     64)



(def ptr
     (binf.native/alloc n-byte))

;;;;;


(t/deftest free

  (t/is (nil? (binf.native/free (binf.native/alloc 4)))))



(t/deftest ptr<->view

  (t/is (= ptr
           (-> ptr
               (binf.native/ptr->view n-byte)
               binf.native/view->ptr))))



(t/deftest realloc

  (t/is (not (zero? (binf.native/realloc (binf.native/alloc 4)
                                         8)))))



(t/deftest rw

  (t/is (= -42
           (do
             (binf.native/w-b8 ptr
                               -42)
             (binf.native/r-i8 ptr))
           (do
             (binf.native/w-b16 ptr
                                -42)
             (binf.native/r-i16 ptr))
           (do
             (binf.native/w-b32 ptr
                                -42)
             (binf.native/r-i32 ptr))
           (do
             (binf.native/w-b64 ptr
                                -42)
             (binf.native/r-b64 ptr)))
        "Signed")

  (t/is (= 42
           (do
             (binf.native/w-b8 ptr
                               42)
             (binf.native/r-u8 ptr))
           (do
             (binf.native/w-b16 ptr
                                42)
             (binf.native/r-u16 ptr))
           (do
             (binf.native/w-b32 ptr
                                42)
             (binf.native/r-u32 ptr))
           (do
             (binf.native/w-b64 ptr
                                42)
             (binf.native/r-b64 ptr)))
        "Unsigned")

  (t/is (= (float 42.24)
           (do
             (binf.native/w-f32 ptr
                                42.24)
             (binf.native/r-f32 ptr)))
        "f32")

  (t/is (= 42.24
           (do
             (binf.native/w-f64 ptr
                                42.24)
             (binf.native/r-f64 ptr)))
        "f64")

  (t/is (= 0xffffffff
           (do
             (binf.native/w-ptr ptr
                                0xffffffff)
             (binf.native/r-ptr ptr)))
        "ptr"))
