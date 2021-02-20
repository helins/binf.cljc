 (ns helins.binf.protocol.impl

   ""

   {:author "Adam Helinski"
    :no-doc true}

   (:require [helins.binf.buffer   :as binf.buffer]
             [helins.binf.int      :as binf.int]
             [helins.binf.protocol :as binf.protocol]
             [helins.binf.string   :as binf.string])
   (:import (java.nio ByteBuffer
                      ByteOrder
                      CharBuffer)
            (java.nio.charset CoderResult)))


;;;;;;;;;; Implenting protocols


(deftype View [^ByteBuffer byte-buffer
                           -offset]


  clojure.lang.Counted

    (count [_]
      (- (.limit byte-buffer)
         -offset))


  binf.protocol/IAbsoluteReader

    (ra-buffer [this position n-byte buffer offset]
      (binf.buffer/copy buffer
                        offset
                        (binf.protocol/to-buffer this)
                        (+ -offset
                           position)
                        n-byte))

    (ra-u8 [_ position]
      (binf.int/u8 (.get byte-buffer
                         (int (+ -offset
                                  position)))))

    (ra-i8 [_ position]
      (.get byte-buffer
            (int (+ -offset
                    position))))


    (ra-u16 [_ position]
      (binf.int/u16 (.getShort byte-buffer
                               (+ -offset
                                  position))))

    (ra-i16 [_ position]
      (.getShort byte-buffer
                 (+ -offset
                    position)))


    (ra-u32 [_ position]
      (binf.int/u32 (.getInt byte-buffer
                             (+ -offset
                                position))))

    (ra-i32 [_ position]
      (.getInt byte-buffer
               (+ -offset
                  position)))

    (ra-i64 [_ position]
      (.getLong byte-buffer
                (+ -offset
                   position)))

    (ra-f32 [_ position]
      (.getFloat byte-buffer
                 (+ -offset
                    position)))

    (ra-f64 [_ position]
      (.getDouble byte-buffer
                  (+ -offset
                     position)))

    (ra-string [_this decoder position n-byte]
      (String. (.array byte-buffer)
               (int (+ -offset
                       position))
               ^long n-byte
               (or decoder
                   binf.string/decoder-utf-8)))


  binf.protocol/IAbsoluteWriter

    (wa-buffer [this position buffer offset n-byte]
      (binf.buffer/copy (binf.protocol/to-buffer this)
                        (+ -offset
                           position)
                        buffer
                        offset
                        n-byte)
      this)

    (wa-b8 [this position integer]
      (.put byte-buffer
            (+ -offset
               position)
            (unchecked-byte integer))
      this)

    (wa-b16 [this position integer]
      (.putShort byte-buffer
                 (+ -offset
                    position)
                 (unchecked-short integer))
      this)

    (wa-b32 [this position integer]
      (.putInt byte-buffer
               (+ -offset
                  position)
               (unchecked-int integer))
      this)

    (wa-b64 [this position integer]
      (.putLong byte-buffer
                (+ -offset
                   position)
                integer)
      this)

    (wa-f32 [this position floating]
      (.putFloat byte-buffer
                 (+ -offset
                    position)
                 floating)
      this)

    (wa-f64 [this position floating]
      (.putDouble byte-buffer
                  (+ -offset
                     position)
                  floating)
      this)

    (wa-string [this position string]
      (let [saved-position (.position byte-buffer)]
        (.position byte-buffer
                   (+ -offset
                      position))
        (let [res (binf.protocol/wr-string this
                                           string)]
          (.position byte-buffer
                     saved-position)
          res)))
    

  binf.protocol/IEndianess


    (endian-get [_this]
      (condp =
             (.order byte-buffer)
        ByteOrder/BIG_ENDIAN    :big-endian
        ByteOrder/LITTLE_ENDIAN :little-endian))

    (endian-set [this endianess]
      (.order byte-buffer
              (case endianess
                :big-endian    ByteOrder/BIG_ENDIAN
                :little-endian ByteOrder/LITTLE_ENDIAN))
      this)


  binf.protocol/IRelativeReader

    
    (rr-buffer [this n-byte]
      (binf.protocol/rr-buffer this
                               n-byte
                               (binf.buffer/alloc n-byte)
                               0))

    (rr-buffer [this n-byte buffer]
      (binf.protocol/rr-buffer this
                               n-byte
                               buffer
                               0))

    (rr-buffer [this n-byte buffer offset]
      (let [b (binf.buffer/copy buffer
                                offset
                                (binf.protocol/to-buffer this)
                                (.position byte-buffer)
                                n-byte)]
        (binf.protocol/skip this
              n-byte)
        b))

    (rr-u8 [_]
      (binf.int/u8 (.get byte-buffer)))

    (rr-i8 [_]
      (.get byte-buffer))

    (rr-u16 [_]
      (binf.int/u16 (.getShort byte-buffer)))

    (rr-i16 [_]
      (.getShort byte-buffer))

    (rr-u32 [_]
      (binf.int/u32 (.getInt byte-buffer)))

    (rr-i32 [_]
      (.getInt byte-buffer))

    (rr-i64 [_]
      (.getLong byte-buffer))

    (rr-f32 [_]
      (.getFloat byte-buffer))

    (rr-f64 [_]
      (.getDouble byte-buffer))

    (rr-string [this n-byte]
      (binf.protocol/rr-string this
                               nil
                               n-byte))

    (rr-string [this decoder n-byte]
      (let [string (String. (.array byte-buffer)
                            (.position byte-buffer)
                            ^long n-byte
                            (or decoder
                                binf.string/encoder-utf-8))]
        (binf.protocol/skip this
                            n-byte)
        string))


  binf.protocol/IRelativeWriter


    (wr-buffer [this buffer]
      (binf.protocol/wr-buffer this
                               buffer
                               0
                               (count buffer)))

    (wr-buffer [this buffer offset]
      (binf.protocol/wr-buffer this
                               buffer
                               offset
                               (- (count buffer)
                                  offset)))

    (wr-buffer [this buffer offset n-byte]
      (binf.buffer/copy (binf.protocol/to-buffer this)
                        (.position byte-buffer)
                        buffer
                        offset
                        n-byte)
      (binf.protocol/skip this
                          n-byte)
      this)

    (wr-b8 [this integer]
      (.put byte-buffer
            (unchecked-byte integer))
      this)

    (wr-b16 [this integer]
      (.putShort byte-buffer
                 (unchecked-short integer))
      this)

    (wr-b32 [this integer]
      (.putInt byte-buffer
               (unchecked-int integer))
      this)

    (wr-b64 [this integer]
      (.putLong byte-buffer
                integer)
      this)

    (wr-f32 [this floating]
      (.putFloat byte-buffer
                 floating)
      this)

    (wr-f64 [this floating]
      (.putDouble byte-buffer
                  floating)
      this)

    (wr-string [_this string]
      (let [encoder     (.newEncoder binf.string/encoder-utf-8)
            char-buffer (if (instance? CharBuffer
                                       string)
                          string
                          (CharBuffer/wrap ^String string))
            position-bb (.position byte-buffer)
            position-cb (.position ^CharBuffer char-buffer)
            res         (.encode encoder
                                 char-buffer 
                                 byte-buffer
                                 true)
            n-byte      (- (.position byte-buffer)
                           position-bb)
            n-chars     (- (.position ^CharBuffer char-buffer)
                           position-cb)]
        (condp =
               res
          CoderResult/UNDERFLOW [true n-byte n-chars]
          CoderResult/OVERFLOW  [false n-byte n-chars char-buffer]
          (throw (ex-info (str "Unable to write string: "
                               string)
                          {::error  :string-encoding
                           ::string string})))))

        ;; It seems the encoder does not need to be flushed when writing UTF-8
        ;;
        ; (condp =
        ;        (.flush encoder
        ;                byte-buffer)
        ;   CoderResult/UNDERFLOW (- (.position byte-buffer)
        ;                            offset)
        ;   CoderResult/OVERFLOW  (throw (ex-info "Not enough bytes to flush string encoder"
        ;                                         {::error  :insufficient-output
        ;                                          ::string string}))
        ;   (throw (ex-info "Unable to flush string encoder"
        ;                   {::error  :string-encoding
        ;                    ::string string}))))


  binf.protocol/IView

    (offset [_]
      -offset)

    (position [_]
      (- (.position byte-buffer)
         -offset))

    (seek [this position]
      (.position byte-buffer
                 (+ -offset
                    position))
      this)

    (skip [this n-byte]
      (.position byte-buffer
                 (+ (.position byte-buffer)
                    n-byte))
      this)

    (to-buffer [_]
      (.array byte-buffer))


  binf.protocol/IViewable


    (view [this]
      this)

    (view [_ offset]
      (binf.protocol/view (.array byte-buffer)
                          (+ -offset
                             offset)))

    (view [_ offset size]
      (binf.protocol/view (.array byte-buffer)
                          (+ -offset
                             offset)
                          size)))


;;;;;;;;;; Creating views from objects


(extend-protocol binf.protocol/IViewable

  (Class/forName "[B")

    (view
     
      ([this]
       (View. (ByteBuffer/wrap this)
              0))

      ([this offset]
       (View. (doto (ByteBuffer/wrap this)
                (.position offset))
              offset))

      ([this offset size]
       (View. (doto (ByteBuffer/wrap this)
                (.position offset)
                (.limit (+ offset
                           size)))
              offset))))
