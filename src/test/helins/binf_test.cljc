(ns helins.binf-test

  {:author "Adam Helins"}

  (:require [clojure.test :as t]
            [helins.binf  :as binf])
  (:refer-clojure :rename {bit-shift-right >>}))


;;;;;;;;;;


(t/deftest buffer*
  
  (t/is (= (range -3
                  4)
           (seq (binf/buffer* -3 -2 -1 0 1 2 3)))))


;;;;;;;;;; Primitive conversions


(t/deftest uints

  (t/are [n fi fu]
         (let [value (dec (binf/integer (Math/pow 2
                                                  n)))]
           (t/is (= value
                    (-> value
                        fu
                        fi
                        fu
                        fi
                        fu))))
    8  binf/i8  binf/u8
    16 binf/i16 binf/u16
    32 binf/i32 binf/u32))



(t/deftest i64

  (let [value (binf/integer (Math/pow 2
                                      7))]
    (t/is (= value
             (binf/i64 (binf/u8 (>> value
                                    56))
                       (binf/u8 (>> value
                                    48))
                       (binf/u8 (>> value
                                    40))
                       (binf/u8 (>> value
                                    32))
                       (binf/u8 (>> value
                                    24))
                       (binf/u8 (>> value
                                    16))
                       (binf/u8 (>> value
                                    8))
                       (binf/u8 value))))))


#?(:clj (t/deftest f32

  ; JS does not have real floats, imprecision arise when they get converted automatically to f64.
  ; Other than that, the implementation is technically correct.

  (t/is (= (float 42.42)
           (binf/f32 (binf/bits-f32 42.42)))
        "f32")))


(t/deftest f64

  (t/is (= 42.42
           (binf/f64 (binf/bits-f64 42.42)))
        "f64"))


;;;;;;;;;; Views, primitive values


(def offset
     4)


(def size
     16)


(def size-2
     4)


(def view
     (binf/view (binf/buffer size)))


#?(:cljs (def view-shared
              (binf/view (binf/buffer-shared size))))


(t/deftest buffer->view

  ;; Without offset nor size
  
  (t/is (= 0
           (binf/offset view)
           #?(:cljs (binf/offset view-shared))))
  (t/is (= 0
           (binf/position view)
           #?(:cljs (binf/position view-shared))))
  (t/is (= size
           (count view)
           #?(:cljs (count view-shared))))
  (t/is (= size
           (binf/remaining view)
           #?(:cljs (binf/remaining view-shared))))

  ;; With offset

  (let [v (binf/view (binf/buffer size)
                     offset)
        #?@(:cljs [v-shared (binf/view (binf/buffer-shared size)
                                       offset)])]
    (t/is (= offset
             (binf/offset v)
             #?(:cljs (binf/offset v-shared))))
    (t/is (= 0
             (binf/position v)
             #?(:cljs (binf/position v-shared))))
    (t/is (= (- size
                offset)
             (count v)
             #?(:cljs (count v-shared))))
    (t/is (= (- size
                offset)
             (binf/remaining v)
             #?(:cljs (binf/remaining v-shared)))))

  ;; With offset and size

  (let [v (binf/view (binf/buffer size)
                     offset
                     size-2)
        #?@(:cljs [v-shared (binf/view (binf/buffer-shared size)
                                       offset
                                       size-2)])]
    (t/is (= offset
             (binf/offset v)
             #?(:cljs (binf/offset v-shared))))
    (t/is (= 0
             (binf/position v)
             #?(:cljs (binf/position v-shared))))
    (t/is (= size-2
             (count v)
             #?(:cljs (count v-shared))))
    (t/is (= size-2
             (binf/remaining v)
             #?(:cljs (binf/remaining v-shared))))))



(t/deftest view->view

  ;; Without offset nor size
  
  (let [v (binf/view view)]
    (t/is (= 0
             (binf/offset v)))
    (t/is (= 0
             (binf/position v)))
    (t/is (= size
             (count v)))
    (t/is (= size
             (binf/remaining v))))

  ;; With offset

  (let [v (binf/view view
                     offset)]
    (t/is (= offset
             (binf/offset v)))
    (t/is (= 0
             (binf/position v)))
    (t/is (= (- size
                offset)
             (count v)))
    (t/is (= (- size
                offset)
             (binf/remaining v))))

  ;; With offset and size

  (let [v (binf/view view
                     offset
                     size-2)]
    (t/is (= offset
             (binf/offset v)))
    (t/is (= 0
             (binf/position v)))
    (t/is (= size-2
             (count v)))
    (t/is (= size-2
             (binf/remaining v)))))



(defn view-8

  []
  
  (binf/view (binf/buffer 8)))



#?(:cljs (defn view-8-shared

  []

  (binf/view (binf/buffer-shared 8))))



(defn grow-buffer

  [n-byte-old]

  (+ n-byte-old
     (if (< (rand)
            0.5)
       3
       4)))



(defn gview

  []

  (binf/growing-view (binf/buffer 1)
                     grow-buffer))



; #?(:cljs (defn gview-shared
; 
;   []
; 
;   (binf/growing-view (binf/buffer-shared 1)
;                      grow-buffer)))



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


    binf/wa-b8  binf/ra-u8  binf/wr-b8  binf/rr-u8  (binf/integer (dec (Math/pow 2 8)))
    binf/wa-b8  binf/ra-i8  binf/wr-b8  binf/rr-i8  -1
    binf/wa-b16 binf/ra-u16 binf/wr-b16 binf/rr-u16 (binf/integer (dec (Math/pow 2 16)))
    binf/wa-b16 binf/ra-i16 binf/wr-b16 binf/rr-i16 -1
    binf/wa-b32 binf/ra-u32 binf/wr-b32 binf/rr-u32 (binf/integer (dec (Math/pow 2 32)))
    binf/wa-b32 binf/ra-i32 binf/wr-b32 binf/rr-i32 -1))



(defn- -view-i64

  [f-view]

  (let [x #?(:clj  Long/MAX_VALUE
             :cljs (js/BigInt js/Number.MAX_SAFE_INTEGER))]
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



(t/deftest gview-uints

  (-view-uints gview))



(t/deftest view-i64

  (-view-i64 view-8))



#?(:cljs (t/deftest view-i64-shared

  (-view-i64 view-8-shared)))



(t/deftest gview-i64

  (-view-i64 gview))



#?(:clj (t/deftest view-f32

  (-view-f32 view-8)))



#?(:clj (t/deftest gview-f32

  (-view-f32 gview)))



(t/deftest view-f64

  (-view-f64 view-8))



#?(:cljs (t/deftest view-f64-shared

  (-view-f64 view-8-shared)))



(t/deftest gview-f64

  (-view-f64 gview))


;;;;;;;;;; Copying


(defn cp-view

  []

  (let [view (binf/view (binf/buffer 5))]
    (dotimes [_ 5]
      (binf/wr-b8 view
                  1))
    view))


(def cp-target
     (concat (repeat 5
                     0)
             (repeat 2
                     1)
             (repeat 3
                     0)))


(t/deftest copy-buffer

  (t/is (= (seq (binf/to-buffer (cp-view)))
           (seq (binf/copy-buffer (binf/to-buffer (cp-view)))))
        "Cloning")

  (t/is (= (concat (repeat 5
                           0)
                   (repeat 5
                           1))
           (seq (binf/copy-buffer (binf/buffer 10)
                                  5
                                  (binf/to-buffer (cp-view)))))
        "Without offset nor length")

  (t/is (= (concat (repeat 5
                           0)
                   (repeat 3
                           1)
                   (repeat 2
                           0))
           (seq (binf/copy-buffer (binf/buffer 10)
                                  5
                                  (binf/to-buffer (cp-view))
                                  2)))
        "With offset")


  (t/is (= cp-target
           (seq (binf/copy-buffer (binf/buffer 10)
                                  5
                                  (binf/to-buffer (cp-view))
                                  2
                                  2)))
        "With offset and length"))



(defn- -rwa-buffer

  [view]

  (t/is (= (take 7
                 cp-target)
           (take 7
                 (seq (binf/to-buffer (binf/wa-buffer view
                                                      5
                                                      (binf/to-buffer (cp-view))
                                                      2
                                                      2)))))
        "Absolute writing")

  (t/is (= (take 5
                 (drop 2
                       cp-target))
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
                 cp-target)
           (take 7
                 (seq (binf/to-buffer (binf/wr-buffer view
                                                      (binf/to-buffer (cp-view))
                                                      2
                                                      2)))))
        "Relative writing")

  (t/is (= (binf/position view)
           7)
        "Writing is relative")

  (binf/seek view
             0)

  (t/is (= (take 7
                 cp-target)
           (seq (binf/rr-buffer view
                                7)))
        "Relative reading")

  (t/is (= (binf/position view)
           7)
        "Reading is relative"))



(t/deftest rwa-buffer

  (-rwa-buffer (binf/view (binf/buffer 10))))



(t/deftest rwr-buffer

  (-rwr-buffer (binf/view (binf/buffer 10))))



(t/deftest grwa-buffer

  (-rwa-buffer (binf/growing-view (binf/buffer 2))))



(t/deftest grwr-buffer

  (-rwr-buffer (binf/growing-view (binf/buffer 2))))


;;;;;;;;;; Encoding and decoding text


(def string
     "²é&\"'(§è!çà)-aertyuiopqsdfhgklmwcvbnùµ,;:=")



(t/deftest text

  (t/is (= string
           (-> string
               binf/text-encode
               binf/text-decode))))



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
                             string)]

    (-string string
             res)

    (t/is (zero? (binf/position view))
          "Write was absolute")

    (t/is (= string
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
                             string)]

    (-string string
              res)

    (t/is (= (res 1)
             (binf/position view))
          "Write was relative")

    (binf/seek view
               0)

    (t/is (= string
             (binf/rr-string view
                             (res 1)))
          "Properly decoding encoded string")
    
    (t/is (= (res 1)
             (binf/position view))
          "Read was relative")))




(t/deftest a-string
  
  (t/is (false? (first (binf/wa-string (binf/view (binf/buffer 10))
                                       0
                                       string)))
        "Not enough bytes to write everything")

  (-a-string #(binf/view (binf/buffer 1024))))




(t/deftest ga-string

  (-a-string gview))




(t/deftest r-string

  (t/is (false? (first (binf/wr-string (binf/view (binf/buffer 10))
                                       string)))
        "Not enough bytes to write everything")

  (-r-string #(binf/view (binf/buffer 1024))))



(t/deftest gr-string

  (-r-string gview))


;;;;;;;;;; Growing views


(t/deftest gseek

  (let [gv (binf/growing-view (binf/buffer 10))]
    (binf/seek gv
               1000)
    (t/is (= 1000
             (binf/position gv)))))


;;;;;;;;;; Base64 utilities


(t/deftest base64

  (let [buffer (binf/buffer 64)
        view   (binf/view buffer)]
    (dotimes [i 64]
      (binf/wr-b8 view
                  i))
    (t/is (= (seq buffer)
             (seq (-> buffer
                      binf/base64-encode
                      binf/base64-decode)))
          "Without offset nor lenght")
    (t/is (= (drop 5
                   (seq buffer))
             (seq (-> buffer
                      (binf/base64-encode 5)
                      binf/base64-decode)))
          "With offset without length")
    (t/is (= (->> (seq buffer)
                  (drop 5)
                  (take 20))
             (seq (-> buffer
                      (binf/base64-encode 5
                                          20)
                      binf/base64-decode)))
          "With offset and length")))
