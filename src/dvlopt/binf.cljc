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
