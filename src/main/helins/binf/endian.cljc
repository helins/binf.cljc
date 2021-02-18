(ns helins.binf.endian

  ""

  {:author "Adam Helinski"}
  #?(:clj (:import java.nio.ByteOrder)))


;;;;;;;;;;


#?(:cljs (def ^:private -d*js-order

  ;;

  (delay
    (let [b16 (js/Uint32Array. 1)
          b8  (js/Uint8Array. (.-buffer b16))]
      (aset b16
            0
            0xaa)
      (if (= (aget b8
                   0)
             0xaa)
        :little-endian
        :big-endian)))))



(defn order-host

  ""

  []

  #?(:clj  :big-endian
     :cljs @-d*js-order))



(defn order-native

  ""

  []

  #?(:clj  (ByteOrder/nativeOrder)
     :cljs @-d*js-order))
