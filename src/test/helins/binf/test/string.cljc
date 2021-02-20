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
