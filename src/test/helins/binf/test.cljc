;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns helins.binf.test

  "Testing core view utilities."

  {:author "Adam Helins"}

  (:require [clojure.test                    :as t]
            [clojure.test.check.clojure-test :as TC.ct]
            [clojure.test.check.generators   :as TC.gen]
            [clojure.test.check.properties   :as TC.prop]
            [helins.binf                     :as binf]
            [helins.binf.buffer              :as binf.buffer]
            [helins.binf.gen                 :as binf.gen]
            [helins.binf.int                 :as binf.int]
            [helins.binf.int64               :as binf.int64]
            #?(:clj [helins.binf.native      :as binf.native])
            [helins.binf.test.buffer         :as binf.test.buffer]
            [helins.binf.test.string         :as binf.test.string]))


#?(:clj (set! *warn-on-reflection*
              true))


;;;;;;;;;; Miscellaneous


(defn eq-float

  "Computes equality for floats where NaN is equal to itself."

  [x y]

  (if (Double/isNaN x)
    (Double/isNaN y)
    (= x
       y)))


;;;;;;;;;; Creating views


(def offset
     4)


(def size
     16)


(def size-2
     4)


(def view
     (-> (binf.buffer/alloc size)
         binf/view
         (binf/endian-set :little-endian)))


#?(:clj (def view-native
             (binf/endian-set (binf.native/view size)
                              :little-endian)))


#?(:cljs (def view-shared
              (-> (binf.buffer/alloc-shared size)
                  binf/view
                  (binf/endian-set :little-endian))))


(t/deftest offset-view
  (let [view (-> (binf.buffer/alloc 64)
                 (binf/view 32
                            16)
                 (binf/endian-set :little-endian))]
    (binf/wa-b8 view 0
                42)
    (t/is (= 42
             (binf/ra-u8 view 0)))))


(t/deftest buffer->view

  ;; Without offset nor size
  
  (t/is (= 0
           (binf/buffer-offset view)
           #?(:cljs (binf/buffer-offset view-shared))))
  (t/is (= 0
           (binf/position view)
           #?(:clj (binf/position view-native))
           #?(:cljs (binf/position view-shared))))
  (t/is (= size
           (binf/limit view)
           #?(:clj (binf/limit view-native))
           #?(:cljs (binf/limit view-shared))))
  (t/is (= size
           (binf/remaining view)
           #?(:clj (binf/remaining view-native))
           #?(:cljs (binf/remaining view-shared))))

  ;; With offset

  (let [v (binf/view (binf.buffer/alloc size)
                     offset)
        #?@(:cljs [v-shared (binf/view (binf.buffer/alloc-shared size)
                                       offset)])]
    (t/is (= offset
             (binf/buffer-offset v)
             #?(:cljs (binf/buffer-offset v-shared))))
    (t/is (= 0
             (binf/position v)
             #?(:cljs (binf/position v-shared))))
    (t/is (= (- size
                offset)
             (binf/limit v)
             #?(:cljs (binf/limit v-shared))))
    (t/is (= (- size
                offset)
             (binf/remaining v)
             #?(:cljs (binf/remaining v-shared)))))

  ;; With offset and size

  (let [v (binf/view (binf.buffer/alloc size)
                     offset
                     size-2)
        #?@(:cljs [v-shared (binf/view (binf.buffer/alloc-shared size)
                                       offset
                                       size-2)])]
    (t/is (= offset
             (binf/buffer-offset v)
             #?(:cljs (binf/buffer-offset v-shared))))
    (t/is (= 0
             (binf/position v)
             #?(:cljs (binf/position v-shared))))
    (t/is (= size-2
             (binf/limit v)
             #?(:cljs (binf/limit v-shared))))
    (t/is (= size-2
             (binf/remaining v)
             #?(:cljs (binf/remaining v-shared))))))



(t/deftest view->view

  ;; Without offset nor size
  
  (let [v (binf/view view)]
    (t/is (= :little-endian
             (binf/endian-get v))
          "Endianess is duplicated")
    (t/is (= 0
             (binf/buffer-offset v)))
    (t/is (= 0
             (binf/position v)))
    (t/is (= size
             (binf/limit v)))
    (t/is (= size
             (binf/remaining v))))

  ;; With offset

  (let [v (binf/view view
                     offset)
        #?@(:clj [v-native (binf/view view-native
                                      offset)])]
    (t/is (= :little-endian
             (binf/endian-get v))
          "Endianess is duplicated")
    #?(:clj (t/is (= :little-endian
                     (binf/endian-get v-native))
                  "Endianess is duplicated in native view"))
    (t/is (= offset
             (binf/buffer-offset v)))
    (t/is (= 0
             (binf/position v)
             #?(:clj (binf/position v-native))))
    (t/is (= (- size
                offset)
             (binf/limit v)
             #?(:clj (binf/limit v-native))))
    (t/is (= (- size
                offset)
             (binf/remaining v)
             #?(:clj (binf/remaining v-native)))))

  ;; With offset and size

  (let [v (binf/view view
                     offset
                     size-2)
        #?@(:clj [v-native (binf/view view-native
                                      offset
                                      size-2)])]
    (t/is (= :little-endian
             (binf/endian-get v))
          "Endianess is duplicated")
    #?(:clj (t/is (= :little-endian
                     (binf/endian-get v-native))
                  "Endianess is duplicated in native view"))
    (t/is (= offset
             (binf/buffer-offset v)))
    (t/is (= 0
             (binf/position v)
             #?(:clj (binf/position v-native))))
    (t/is (= size-2
             (binf/limit v)
             #?(:clj (binf/limit v-native))))
    (t/is (= size-2
             (binf/remaining v)
             #?(:clj (binf/remaining v-native))))))


;;;;;;;;; Numerical R/W


(def view-size
     1024)


(defn gen-write

  ""

  [src n-byte]

  (TC.gen/let [start (TC.gen/choose 0
                                    (- view-size
                                       n-byte))
               size  (TC.gen/choose n-byte
                                    (- view-size
                                       start))
               pos   (TC.gen/choose 0
                                    (- size
                                       n-byte))]
    [pos
     (binf/view src
                start
                size)]))



(defn prop-absolute

  ""


  ([src gen n-byte ra wa]

   (prop-absolute src
                  gen
                  n-byte
                  ra
                  wa
                  =))


  ([src gen n-byte ra wa eq]

   (TC.prop/for-all [x      gen
                     [pos
                      view] (gen-write src
                                       n-byte)]
     (eq x
         (-> view
             (wa pos
                 x)
             (ra pos))))))



(defn prop-relative

  ""


  ([src gen n-byte ra wa]

   (prop-relative src
                  gen
                  n-byte
                  ra
                  wa
                  =))


  ([src gen n-byte rr wr eq]

   (TC.prop/for-all [x      gen
                     [pos
                      view] (gen-write src
                                       n-byte)]
     (eq x
         (-> view
             (binf/seek pos)
             (wr x)
             (binf/seek (- (binf/position view)
                          n-byte))
             rr)))))


(def src
     (binf.buffer/alloc view-size))



(TC.ct/defspec rwa-i8

  (prop-absolute src
                 binf.gen/i8
                 1
                 binf/ra-i8
                 binf/wa-b8))


(TC.ct/defspec rwa-i16

  (prop-absolute src
                 binf.gen/i16
                 2
                 binf/ra-i16
                 binf/wa-b16))


(TC.ct/defspec rwa-i32

  (prop-absolute src
                 binf.gen/i32
                 4
                 binf/ra-i32
                 binf/wa-b32))


(TC.ct/defspec rwa-i64

  (prop-absolute src
                 binf.gen/i64
                 8
                 binf/ra-i64
                 binf/wa-b64))


(TC.ct/defspec rwa-u8

  (prop-absolute src
                 binf.gen/u8
                 1
                 binf/ra-u8
                 binf/wa-b8))


(TC.ct/defspec rwa-u16

  (prop-absolute src
                 binf.gen/u16
                 2
                 binf/ra-u16
                 binf/wa-b16))


(TC.ct/defspec rwa-u32

  (prop-absolute src
                 binf.gen/u32
                 4
                 binf/ra-u32
                 binf/wa-b32))


(TC.ct/defspec rwa-u64

  (prop-absolute src
                 binf.gen/u64
                 8
                 binf/ra-u64
                 binf/wa-b64))


(TC.ct/defspec rwr-i8

  (prop-relative src
                 binf.gen/i8
                 1
                 binf/rr-i8
                 binf/wr-b8))


(TC.ct/defspec rwr-i16

  (prop-relative src
                 binf.gen/i16
                 2
                 binf/rr-i16
                 binf/wr-b16))


(TC.ct/defspec rwr-i32

  (prop-relative src
                 binf.gen/i32
                 4
                 binf/rr-i32
                 binf/wr-b32))


(TC.ct/defspec rwr-i64

  (prop-relative src
                 binf.gen/i64
                 8
                 binf/rr-i64
                 binf/wr-b64))


(TC.ct/defspec rwa-f32

  (prop-absolute binf.gen/f32
                 4
                 binf/ra-f32
                 binf/wa-f32
                 eq-float))


(TC.ct/defspec rwa-f64

  (prop-absolute src
                 binf.gen/f64
                 8
                 binf/ra-f64
                 binf/wa-f64
                 eq-float))


(TC.ct/defspec rwr-f32

  (prop-relative src
                 binf.gen/f32
                 4
                 binf/rr-f32
                 binf/wr-f32
                 eq-float))


(TC.ct/defspec rwr-f64

  (prop-relative src
                 binf.gen/f64
                 8
                 binf/rr-f64
                 binf/wr-f64
                 eq-float))


(def src-2

  "On the JVM, represents a native view while in JS, represents a shared buffer.
  
   Both have nothing in common but below tests can be reused across platforms."
  
  #?(:clj  (binf.native/view view-size)
     :cljs (binf.buffer/alloc-shared view-size)))


(TC.ct/defspec rwa-i8-2

  (prop-absolute src-2
                 binf.gen/i8
                 1
                 binf/ra-i8
                 binf/wa-b8))


(TC.ct/defspec rwa-i16-2

  (prop-absolute src-2
                 binf.gen/i16
                 2
                 binf/ra-i16
                 binf/wa-b16))


(TC.ct/defspec rwa-i32-2

  (prop-absolute src-2
                 binf.gen/i32
                 4
                 binf/ra-i32
                 binf/wa-b32))


(TC.ct/defspec rwa-i64-2

  (prop-absolute src-2
                 binf.gen/i64
                 8
                 binf/ra-i64
                 binf/wa-b64))


(TC.ct/defspec rwa-u8-2

  (prop-absolute src-2
                 binf.gen/u8
                 1
                 binf/ra-u8
                 binf/wa-b8))


(TC.ct/defspec rwa-u16-2

  (prop-absolute src-2
                 binf.gen/u16
                 2
                 binf/ra-u16
                 binf/wa-b16))


(TC.ct/defspec rwa-u32-2

  (prop-absolute src-2
                 binf.gen/u32
                 4
                 binf/ra-u32
                 binf/wa-b32))


(TC.ct/defspec rwa-u64-2

  (prop-absolute src-2
                 binf.gen/u64
                 8
                 binf/ra-u64
                 binf/wa-b64))


(TC.ct/defspec rwr-i8-2

  (prop-relative src-2
                 binf.gen/i8
                 1
                 binf/rr-i8
                 binf/wr-b8))


(TC.ct/defspec rwr-i16-2

  (prop-relative src-2
                 binf.gen/i16
                 2
                 binf/rr-i16
                 binf/wr-b16))


(TC.ct/defspec rwr-i32-2

  (prop-relative src-2
                 binf.gen/i32
                 4
                 binf/rr-i32
                 binf/wr-b32))


(TC.ct/defspec rwr-i64-2

  (prop-relative src-2
                 binf.gen/i64
                 8
                 binf/rr-i64
                 binf/wr-b64))


(TC.ct/defspec rwa-f32-2

  (prop-absolute src-2
                 binf.gen/f32
                 4
                 binf/ra-f32
                 binf/wa-f32
                 eq-float))


(TC.ct/defspec rwa-f64-2

  (prop-absolute src-2
                 binf.gen/f64
                 8
                 binf/ra-f64
                 binf/wa-f64
                 eq-float))


(TC.ct/defspec rwr-f32-2

  (prop-relative src-2
                 binf.gen/f32
                 4
                 binf/rr-f32
                 binf/wr-f32
                 eq-float))


(TC.ct/defspec rwr-f64-2

  (prop-relative src-2
                 binf.gen/f64
                 8
                 binf/rr-f64
                 binf/wr-f64
                 eq-float))


;;;;;;;;;; Copying from/to buffers


(def copy-size
     10)



(defn- -rwa-buffer

  [view]

  (t/is (= (take 7
                 binf.test.buffer/copy-target)
           (seq (binf/ra-buffer (binf/wa-buffer view
                                                5
                                                (binf/backing-buffer (binf.test.buffer/make-view))
                                                2
                                                2)
                                0
                                7)))
        "Absolute writing")

  (t/is (= (take 5
                 (drop 2
                       binf.test.buffer/copy-target))
           (seq (binf/ra-buffer view
                                2
                                5)))
        "Absolute reading")

  (t/is (zero? (binf/position view))
        "Position is unchanged"))




(defn- -rwr-buffer

  [view]

  (binf/seek view
             5)

  (t/is (= (take 7
                 binf.test.buffer/copy-target)
           (seq (binf/ra-buffer (binf/wr-buffer view
                                                (binf/backing-buffer (binf.test.buffer/make-view))
                                                2
                                                2)
                                0
                                7)))
        "Relative writing")

  (t/is (= (binf/position view)
           7)
        "Writing is relative")

  (binf/seek view
             0)

  (t/is (= (take 7
                 binf.test.buffer/copy-target)
           (seq (binf/rr-buffer view
                                7)))
        "Relative reading")

  (t/is (= (binf/position view)
           7)
        "Reading is relative"))



(t/deftest rwa-buffer

  (-rwa-buffer (binf/view (binf.buffer/alloc copy-size))))



#?(:clj (t/deftest rwa-buffer-native

  (-rwa-buffer (binf.native/view copy-size))))



#?(:cljs (t/deftest rwa-buffer-shared

  (-rwa-buffer (binf/view (binf.buffer/alloc-shared copy-size)))))



(t/deftest rwr-buffer

  (-rwr-buffer (binf/view (binf.buffer/alloc copy-size))))



#?(:clj (t/deftest rwr-buffer-shared

  (-rwr-buffer (binf.native/view copy-size))))



#?(:cljs (t/deftest rwr-buffer-shared

  (-rwr-buffer (binf/view (binf.buffer/alloc-shared copy-size)))))


;;;;;;;;;; Encoding and decoding strings


(defn -string

  [string res]

  (t/is (first res)
        "Enough bytes for writing strings")

  (t/is (= (count string)
           (res 2))
        "Char count is accurate")

  (t/is (<= (res 2)
            (res 1))
        "Cannot write more chars than bytes"))



(defn- -a-string
  
  [f-view]

  (t/is (false? (first (binf/wa-string (binf/view (binf.buffer/alloc 10))
                                       0
                                       binf.test.string/string)))
        "Not enough bytes to write everything")
  (let [view (f-view)
        res  (binf/wa-string view
                             0
                             binf.test.string/string)]

    (-string binf.test.string/string
             res)

    (t/is (zero? (binf/position view))
          "Write was absolute")

    (t/is (= binf.test.string/string
             (binf/ra-string view
                             0
                             (res 1)))
          "Properly decoding encoded string")
    
    (t/is (zero? (binf/position view))
          "Read was absolute")))



(defn- -r-string

  [f-view]

  (t/is (false? (first (binf/wr-string (binf/view (binf.buffer/alloc 10))
                                       binf.test.string/string)))
        "Not enough bytes to write everything")
  (let [view (f-view)
        res  (binf/wr-string view
                             binf.test.string/string)]

    (-string binf.test.string/string
             res)

    (t/is (= (res 1)
             (binf/position view))
          "Write was relative")

    (binf/seek view
               0)

    (t/is (= binf.test.string/string
             (binf/rr-string view
                             (res 1)))
          "Properly decoding encoded string")
    
    (t/is (= (res 1)
             (binf/position view))
          "Read was relative")))



(t/deftest a-string

  (-a-string #(binf/view (binf.buffer/alloc 1024))))



#?(:clj (t/deftest a-string-native

  (-a-string #(binf.native/view 1024))))



#_(t/deftest r-string

  (-r-string #(binf/view (binf.buffer/alloc 1024))))



#?(:clj (t/deftest r-string-native

  (-r-string #(binf.native/view 1024))))


;;;;;;;;;; Reallocating views


(t/deftest grow

  (t/is (= [1 2 42 0 0 0]
           (seq (binf/backing-buffer (binf/grow (-> (binf.buffer/alloc 4)
                                                    binf/view
                                                    (binf/wr-b8 1)
                                                    (binf/wr-b8 2)
                                                    (binf/wr-b8 42))
                                                2)))
           #?(:cljs (seq (binf/backing-buffer (binf/grow (-> (binf.buffer/alloc-shared 4)
                                                             binf/view
                                                             (binf/wr-b8 1)
                                                             (binf/wr-b8 2)
                                                             (binf/wr-b8 42))
                                                         2))))))

  (let [view (-> (binf/view (binf.buffer/alloc 100))
                 (binf/seek 42))]
    (t/is (= 42
             (binf/position view)
             (-> view
                 (binf/grow 200)
                 binf/position))
          "Position is the same than in the original view"))

  (t/is (= :little-endian
           (-> (binf.buffer/alloc 42)
               binf/view
               (binf/endian-set :little-endian)
               (binf/grow 24)
               binf/endian-get))
        "Endianess is duplicated"))


;;;;;;;;;; Additional types / Boolean


(t/deftest bool

  (let [view (binf/view (binf.buffer/alloc 2))]

    (t/is (= true
             (-> view
                 (binf/wr-bool true)
                 (binf/seek 0)
                 binf/rr-bool))
          "Relative")

    (t/is (= true
             (-> view
                 (binf/wa-bool 1
                               true)
                 (binf/ra-bool 1)))
          "Absolute")))
