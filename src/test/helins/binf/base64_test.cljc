(ns helins.binf.base64-test

  {:author "Adam Helins"}

  (:require [clojure.test       :as t]
            [helins.binf        :as binf]
            [helins.binf.base64 :as binf.base64])
  (:refer-clojure :rename {bit-shift-right >>}))


;;;;;;;;;;


(t/deftest base64

  (let [buffer (binf/buffer 64)
        view   (binf/view buffer)
        #?@(:cljs [buffer-shared (binf/buffer-shared 64)
                   view-shared   (binf/view buffer-shared)])]
    (dotimes [i 64]
      (binf/wr-b8 view
                  i)
      #?(:cljs (binf/wr-b8 view-shared
                           i)))
    (t/is (= (seq buffer)
             (seq (-> buffer
                      binf.base64/encode
                      binf.base64/decode
                      binf/to-buffer))
             #?(:cljs (seq (-> buffer-shared
                               binf.base64/encode
                               (binf.base64/decode binf/buffer-shared)
                               binf/to-buffer))))
          "Without offset nor lenght")
    (t/is (= (drop 5
                   (seq buffer))
             (seq (-> buffer
                      (binf.base64/encode 5)
                      binf.base64/decode
                      binf/to-buffer))
             #?(:cljs (seq (-> buffer-shared
                               (binf.base64/encode 5)
                               (binf.base64/decode binf/buffer-shared)
                               binf/to-buffer))))
          "With offset without length")
    (t/is (= (->> (seq buffer)
                  (drop 5)
                  (take 20))
             (seq (-> buffer
                      (binf.base64/encode 5
                                          20)
                      binf.base64/decode
                      binf/to-buffer))
             #?(:cljs (seq (-> buffer-shared
                               (binf.base64/encode 5
                                                   20)
                               (binf.base64/decode binf/buffer-shared)
                               binf/to-buffer))))
          "With offset and length")))
