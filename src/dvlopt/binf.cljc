(ns dvlopt.binf

  ""

  {:author "Adam Helinski"})


;;;;;;;;; Aliases for bitwise operations


(def ^{:arglists '([x n])}
  
  <<

  ""

  bit-shift-left)



(def ^{:arglists '([x n])}
 
  >>

  ""

  bit-shift-right)



(def ^{:arglists '([x n])}
 
  >>>

  ""

  unsigned-bit-shift-right)



(def ^{:arglists '([x y]
                   [x y & more])}
      
  &

  ""

  bit-and)



(def ^{:arglists '([x y]
                   [x y & more])}

  |

  ""

  bit-or)


(def ^{:arglist '([x y]
                  [x y & more])}

  x|

  ""

  bit-xor)


(def ^{:arglists '([x])}

  !

  bit-not)


;;;;;;;;;;


(defn u8

  ""

  [i8]

  (& 0xff
     i8))



(defn i8

  ""

  [u8]

  (unchecked-byte u8))



(defn u16

  ""

  [i8-1 i8-2]

  (& 0xffff
     (| (<< i8-1
            8)
        i8-2)))



(defn i16

  ""

  [i8-1 i8-2]

  (unchecked-short (u16 i8-1
                        i8-2)))



(defn u32

  ""

  [i8-1 i8-2 i8-3 i8-4]

  (& 0xffffffff
     (| (<< i8-1
            24)
        (<< i8-2
            16)
        (<< i8-3
            8)
        i8-4)))



(defn i32

  ""

  [i8-1 i8-2 i8-3 i8-4]

  (unchecked-int (u32 i8-1
                      i8-2
                      i8-3
                      i8-4)))



(defn i64

  ""

  [i8-1 i8-2 i8-3 i8-4 i8-5 i8-6 i8-7 i8-8]

  (| (<< i8-1
         56)
     (<< i8-2
         48)
     (<< i8-3
         40)
     (<< i8-4
         32)
     (<< i8-5
         24)
     (<< i8-6
         16)
     (<< i8-7
         8)
     i8-8))



(defn f32

  ""

  [i8-1 i8-2 i8-3 i8-4]

  (Float/intBitsToFloat (i32 i8-1
                             i8-2
                             i8-3
                             i8-4)))



(defn f64

  ""

  [i8-1 i8-2 i8-3 i8-4 i8-5 i8-6 i8-7 i8-8]

  (Double/longBitsToDouble (i64 i8-1
                                i8-2
                                i8-3
                                i8-4
                                i8-5
                                i8-6
                                i8-7
                                i8-8)))
