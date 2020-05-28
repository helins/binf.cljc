(ns dvlopt.binf-test

  {:author "Adam Helinski"}

  (:require [clojure.test :as t]
            [dvlopt.binf  :as binf]))




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



(t/deftest ^:no-node i64

  ; Fails on Node because it has no concept of BigInt as the browser (where ints are actually doubles < 64 bits).

  (let [value (binf/integer (Math/pow 2
                                      7))]
    (t/is (= value
             (binf/i64 (binf/u8 (binf/>> value
                                         56))
                       (binf/u8 (binf/>> value
                                         48))
                       (binf/u8 (binf/>> value
                                         40))
                       (binf/u8 (binf/>> value
                                         32))
                       (binf/u8 (binf/>> value
                                         24))
                       (binf/u8 (binf/>> value
                                         16))
                       (binf/u8 (binf/>> value
                                         8))
                       (binf/u8 value))))))


#?(:clj

(t/deftest f32

  ; JS does not have real floats, imprecision arise when they get converted automatically to f64.
  ; Other than that, the implementation is technically correct.

  (t/is (= (float 42.42)
           (binf/f32 (binf/bits-f32 42.42)))
        "f32")))


(t/deftest ^:no-node f64

  ; Fails on Node, cf `i64`.

  (t/is (= 42.42
           (binf/f64 (binf/bits-f64 42.42)))
        "f32"))


;;;;;;;;;; Views


(def offset
     4)


(def size
     16)


(def size-2
     4)


(def view
     (binf/view (binf/buffer size)))


(t/deftest buffer->view

  ;; Without offset nor size
  
  (t/is (= 0
           (binf/offset view)))
  (t/is (= 0
           (binf/position view)))
  (t/is (= size
           (count view)))
  (t/is (= size
           (binf/remaining view)))

  ;; With offset

  (let [v (binf/view (binf/buffer size)
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

  (let [v (binf/view (binf/buffer size)
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



(t/deftest view-uints

  (t/are [wa ra wr rr value]
         (and (t/is (= value
                       (-> (view-8)
                           (wa 0
                               value)
                           (ra 0)))
                    "Absolute")
              (t/is (= value
                       (-> (view-8)
                           (wr value)
                           (binf/seek 0)
                           rr))
                    "Relative"))



    binf/wa-8  binf/ra-u8  binf/wr-8  binf/rr-u8  (binf/integer (dec (Math/pow 2 8)))
    binf/wa-8  binf/ra-i8  binf/wr-8  binf/rr-i8  -1
    binf/wa-16 binf/ra-u16 binf/wr-16 binf/rr-u16 (binf/integer (dec (Math/pow 2 16)))
    binf/wa-16 binf/ra-i16 binf/wr-16 binf/rr-i16 -1
    binf/wa-32 binf/ra-u32 binf/wr-32 binf/rr-u32 (binf/integer (dec (Math/pow 2 32)))
    binf/wa-32 binf/ra-i32 binf/wr-32 binf/rr-i32 -1))



(t/deftest ^:no-node view-i64

  ; Node, Cf. [[i64]]

  (let [x #?(:clj  Long/MAX_VALUE
             :cljs (js/BigInt js/Number.MAX_SAFE_INTEGER))]
    (t/is (= x
             (-> (view-8)
                 (binf/wa-64 0
                             x)
                 (binf/ra-i64 0))))))



#?(:clj

(t/deftest view-f32

  (let [x (float 42.42)]
    (t/is (= x
             (-> (view-8)
                 (binf/wa-f32 0
                              x)
                 (binf/ra-f32 0)))))))



(t/deftest view-f64

  (let [x 42.42]
    (t/is (= x
             (-> (view-8)
                 (binf/wa-f64 0
                              x)
                 (binf/ra-f64 0))))))


;;;;;;;;;; Copying


(defn cp-view

  []

  (let [view (binf/view (binf/buffer 5))]
    (dotimes [_ 5]
      (binf/wr-8 view
                 1))
    view))


(def cp-target
     (concat (repeat 5
                     0)
             (repeat 2
                     1)
             (repeat 3
                     0)))


(t/deftest copy

  (t/is (= (concat (repeat 5
                           0)
                   (repeat 5
                           1))
           (seq (binf/copy (binf/buffer 10)
                           5
                           (binf/to-buffer (cp-view)))))
        "Without offset nor length")

  (t/is (= (concat (repeat 5
                           0)
                   (repeat 3
                           1)
                   (repeat 2
                           0))
           (seq (binf/copy (binf/buffer 10)
                           5
                           (binf/to-buffer (cp-view))
                           2)))
        "With offset")


  (t/is (= cp-target
           (seq (binf/copy (binf/buffer 10)
                           5
                           (binf/to-buffer (cp-view))
                           2
                           2)))
        "With offset and length"))



(t/deftest acopy

  (let [view (binf/view (binf/buffer 10))]
    (t/is (= cp-target
             (seq (binf/to-buffer (binf/acopy view
                                              5
                                              (binf/to-buffer (cp-view))
                                              2
                                              2))))
          "Absolute copying to view")
    (t/is (zero? (binf/position view))
          "Copy is absolute")))



(t/deftest rcopy

  (let [view (binf/view (binf/buffer 10))]
    (binf/skip view
               5)
    (t/is (= cp-target
             (seq (binf/to-buffer (binf/rcopy view
                                              (binf/to-buffer (cp-view))
                                              2
                                              2))))
          "Relative copying to view")
    (t/is (= (binf/position view)
             7)
          "Copy is relative")))


;;;;;;;;;; Encoding and decoding text


(def string
     "²é&\"'(§è!çà)-aertyuiopqsdfhgklmwcvbnùµ,;:=")


(t/deftest text

  (t/is (= string
           (-> string
               binf/text-encode
               binf/text-decode))))


(t/deftest a-string
  
  (t/is (false? (first (binf/wa-string (binf/view (binf/buffer 10))
                                       0
                                       string)))
        "Not enough bytes to write everything")

  (let [view (binf/view (binf/buffer 1024))
        res  (binf/wa-string view
                             0
                             string)]
    (t/is (first res)
          "Enough bytes for writing strings")

    (t/is (= (count string)
             (res 2))
          "Char count is accurate")

    (t/is (zero? (binf/position view))
          "Write was absolute")

    (t/is (= string
             (binf/ra-string view
                             0
                             (res 1)))
          "Properly decoding encoded string")
    
    (t/is (zero? (binf/position view))
          "Read was absolute")))


(t/deftest r-string

  (t/is (false? (first (binf/wr-string (binf/view (binf/buffer 10))
                                       string)))
        "Not enough bytes to write everything")

  (let [view (binf/view (binf/buffer 1024))
        res  (binf/wr-string view
                             string)]
    (t/is (first res)
          "Enough bytes for writing strings")

    (t/is (= (count string)
             (res 2))
          "Char count is accurate")

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
