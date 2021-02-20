(ns helins.binf.test.string

  ""

  {:author "Adam Helinski"}

  (:require [clojure.test       :as t]
            [helins.binf.string :as binf.string]))


;;;;;;;;;;


(def string
     "²é&\"'(§è!çà)-aertyuiopqsdfhgklmwcvbnùµ,;:=")



(t/deftest text

  (t/is (= string
           (-> string
               binf.string/encode
               binf.string/decode))))



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
