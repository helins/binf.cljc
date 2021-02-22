(ns helins.binf.test

  {:author "Adam Helins"}

  (:require [clojure.test            :as t]
            [helins.binf             :as binf]
            [helins.binf.buffer      :as binf.buffer]
            [helins.binf.int         :as binf.int]
            [helins.binf.int64       :as binf.int64]
            [helins.binf.test.buffer :as binf.test.buffer]
            [helins.binf.test.string :as binf.test.string]))


#?(:clj (set! *warn-on-reflection*
              true))


;;;;;;;;;; Creating views


(def offset
     4)


(def size
     16)


(def size-2
     4)


(def view
     (binf/view (binf.buffer/alloc size)))



#?(:cljs (def view-shared
              (binf/view (binf.buffer/alloc-shared size))))



(t/deftest buffer->view

  ;; Without offset nor size
  
  (t/is (= 0
           (binf/buffer-offset view)
           #?(:cljs (binf/buffer-offset view-shared))))
  (t/is (= 0
           (binf/position view)
           #?(:cljs (binf/position view-shared))))
  (t/is (= size
           (binf/limit view)
           #?(:cljs (binf/limit view-shared))))
  (t/is (= size
           (binf/remaining view)
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
                     offset)]
    (t/is (= offset
             (binf/buffer-offset v)))
    (t/is (= 0
             (binf/position v)))
    (t/is (= (- size
                offset)
             (binf/limit v)))
    (t/is (= (- size
                offset)
             (binf/remaining v))))

  ;; With offset and size

  (let [v (binf/view view
                     offset
                     size-2)]
    (t/is (= offset
             (binf/buffer-offset v)))
    (t/is (= 0
             (binf/position v)))
    (t/is (= size-2
             (binf/limit v)))
    (t/is (= size-2
             (binf/remaining v)))))


;;;;;;;;; Numerical R/W


(defn view-8

  []
  
  (binf/view (binf.buffer/alloc 8)))



#?(:cljs (defn view-8-shared

  []

  (binf/view (binf.buffer/alloc-shared 8))))



(defn- -view-uints

  [f-view]

  (t/are [wa ra wr rr value]
         (and (t/is (= value
                       (-> (f-view)
                           (wa 0
                               value)
                           (ra 0)))
                    "Absolute uint")
              (t/is (= value
                       (-> (f-view)
                           (wr value)
                           (binf/seek 0)
                           rr))
                    "Relative uint"))


    binf/wa-b8  binf/ra-u8  binf/wr-b8  binf/rr-u8  (binf.int/from-float (dec (Math/pow 2 8)))
    binf/wa-b8  binf/ra-i8  binf/wr-b8  binf/rr-i8  -1
    binf/wa-b16 binf/ra-u16 binf/wr-b16 binf/rr-u16 (binf.int/from-float (dec (Math/pow 2 16)))
    binf/wa-b16 binf/ra-i16 binf/wr-b16 binf/rr-i16 -1
    binf/wa-b32 binf/ra-u32 binf/wr-b32 binf/rr-u32 (binf.int/from-float (dec (Math/pow 2 32)))
    binf/wa-b32 binf/ra-i32 binf/wr-b32 binf/rr-i32 -1))



(defn- -view-i64

  [f-view]

  (let [x (binf.int64/i* -9223372036854775808)]
    (and (t/is (= x
                 (-> (f-view)
                     (binf/wa-b64 0
                                  x)
                     (binf/ra-i64 0)))
               "Absolute i64")
         (t/is (= x
                  (-> (f-view)
                      (binf/wr-b64 x)
                      (binf/seek 0)
                      (binf/rr-i64)))
               "Relative i64"))))



#?(:clj (defn- -view-f32

  [f-view]

  (let [x (float 42.42)]
    (and (t/is (= x
                  (-> (f-view)
                      (binf/wa-f32 0
                                   x)
                      (binf/ra-f32 0)))
               "Absolute f32")
         (t/is (= x
                  (-> (f-view)
                      (binf/wr-f32 x)
                      (binf/seek 0)
                      binf/rr-f32))
               "Relative f32")))))



(defn- -view-f64

  [f-view]

  (let [x 42.42]
    (and (t/is (= x
                  (-> (f-view)
                      (binf/wa-f64 0
                                   x)
                      (binf/ra-f64 0)))
               "Absolute f64")
         (t/is (= x
                  (-> (f-view)
                      (binf/wr-f64 x)
                      (binf/seek 0)
                      binf/rr-f64))
               "Relative f64"))))



(t/deftest view-uints

  (-view-uints view-8))



#?(:cljs (t/deftest view-uints-shared

  (-view-uints view-8)))



(t/deftest view-i64

  (-view-i64 view-8))



#?(:cljs (t/deftest view-i64-shared

  (-view-i64 view-8-shared)))



#?(:clj (t/deftest view-f32

  (-view-f32 view-8)))



(t/deftest view-f64

  (-view-f64 view-8))



#?(:cljs (t/deftest view-f64-shared

  (-view-f64 view-8-shared)))


;;;;;;;;;; Copying from/to buffers


(defn- -rwa-buffer

  [view]

  (t/is (= (take 7
                 binf.test.buffer/copy-target)
           (take 7
                 (seq (binf/backing-buffer (binf/wa-buffer view
                                                           5
                                                           (binf/backing-buffer (binf.test.buffer/make-view))
                                                           2
                                                           2)))))
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
           (take 7
                 (seq (binf/backing-buffer (binf/wr-buffer view
                                                           (binf/backing-buffer (binf.test.buffer/make-view))
                                                           2
                                                           2)))))
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

  (-rwa-buffer (binf/view (binf.buffer/alloc 10))))



#?(:cljs (t/deftest rwa-buffer-shared

  (-rwa-buffer (binf/view (binf.buffer/alloc-shared 10)))))



(t/deftest rwr-buffer

  (-rwr-buffer (binf/view (binf.buffer/alloc 10))))



#?(:cljs (t/deftest rwr-buffer-shared

  (-rwr-buffer (binf/view (binf.buffer/alloc-shared 10)))))


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
  
  (t/is (false? (first (binf/wa-string (binf/view (binf.buffer/alloc 10))
                                       0
                                       binf.test.string/string)))
        "Not enough bytes to write everything")

  (-a-string #(binf/view (binf.buffer/alloc 1024))))



(t/deftest r-string

  (t/is (false? (first (binf/wr-string (binf/view (binf.buffer/alloc 10))
                                       binf.test.string/string)))
        "Not enough bytes to write everything")

  (-r-string #(binf/view (binf.buffer/alloc 1024))))


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
                                                         2)))))))
