;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


 (ns helins.binf.protocol.impl

   ""

   {:author "Adam Helinski"
    :no-doc true}

   (:require [helins.binf.int      :as binf.int]
             [helins.binf.protocol :as binf.protocol]
             [helins.binf.string   :as binf.string])
   (:import (java.nio ByteBuffer
                      ByteOrder
                      CharBuffer)
            (java.nio.charset Charset
                              CoderResult)))


;;;;;;;;;; Implenting protocols


(extend-type ByteBuffer


  binf.protocol/-IByteBuffer

    (-array-index [this position]
      (+ (.arrayOffset this)
         position))


  binf.protocol/IAbsoluteReader

    (ra-buffer [this position n-byte buffer offset]
      (let [position-cache (.position this)]
        (.position this
                   position)
        (.get this
              ^bytes buffer
              offset
              n-byte)
        (.position this
                   position-cache))
      buffer)

    (ra-u8 [this position]
      (binf.int/u8 (.get this
                         (int (binf.protocol/-array-index this
                                                          position)))))

    (ra-i8 [this position]
      (.get this
            (int (binf.protocol/-array-index this
                                             position))))

    (ra-u16 [this position]
      (binf.int/u16 (.getShort this
                               (binf.protocol/-array-index this
                                                           position))))

    (ra-i16 [this position]
      (.getShort this
                 (binf.protocol/-array-index this
                                             position)))


    (ra-u32 [this position]
      (binf.int/u32 (.getInt this
                             (binf.protocol/-array-index this
                                                         position))))

    (ra-i32 [this position]
      (.getInt this
               (binf.protocol/-array-index this
                                           position)))

    (ra-u64 [this position]
      (binf.protocol/ra-i64 this
                            position))

    (ra-i64 [this position]
      (.getLong this
                (binf.protocol/-array-index this
                                            position)))

    (ra-f32 [this position]
      (.getFloat this
                 (binf.protocol/-array-index this
                                             position)))

    (ra-f64 [this position]
      (.getDouble this
                  (binf.protocol/-array-index this
                                              position)))

    (ra-string [this decoder position n-byte]
      (.toString (.decode ^Charset decoder
                          (-> this
                              .duplicate
                              (.position position)
                              (.limit (+ position
                                         n-byte))))))


  binf.protocol/IAbsoluteWriter

    (wa-buffer [this position buffer offset n-byte]
      (let [position-cache (.position this)]
        (.position this
                   position)
        (.put this
              ^bytes buffer
              offset
              n-byte)
        (.position this
                   position-cache))
      this)

    (wa-b8 [this position integer]
      (.put this
            (binf.protocol/-array-index this
                                        position)
            (unchecked-byte integer))
      this)

    (wa-b16 [this position integer]
      (.putShort this
                 (binf.protocol/-array-index this
                                             position)
                 (unchecked-short integer))
      this)

    (wa-b32 [this position integer]
      (.putInt this
               (binf.protocol/-array-index this
                                           position)
               (unchecked-int integer))
      this)

    (wa-b64 [this position integer]
      (.putLong this
                (binf.protocol/-array-index this
                                            position)
                integer)
      this)

    (wa-f32 [this position floating]
      (.putFloat this
                 (binf.protocol/-array-index this
                                             position)
                 floating)
      this)

    (wa-f64 [this position floating]
      (.putDouble this
                  (binf.protocol/-array-index this
                                              position)
                  floating)
      this)

    (wa-string [this position string]
      (let [saved-position (.position this)]
        (.position this
                   (binf.protocol/-array-index this
                                               position))
        (let [res (binf.protocol/wr-string this
                                           string)]
          (.position this
                     saved-position)
          res)))
    

  binf.protocol/IBackingBuffer

    (backing-buffer [this]
      (.array this))

    (buffer-offset [this]
      (.arrayOffset this))


  binf.protocol/IEndianess


    (endian-get [this]
      (condp =
             (.order this)
        ByteOrder/BIG_ENDIAN    :big-endian
        ByteOrder/LITTLE_ENDIAN :little-endian))

    (endian-set [this endianess]
      (.order this
              (case endianess
                :big-endian    ByteOrder/BIG_ENDIAN
                :little-endian ByteOrder/LITTLE_ENDIAN))
      this)


  binf.protocol/IGrow

    (grow [this n-additional-byte]
      (.position this
                 0)
      (let [bb-new (ByteBuffer/allocate (+ (binf.protocol/limit this)
                                           n-additional-byte))]
        (.put bb-new
              this)
        bb-new))


  binf.protocol/IRelativeReader

    (rr-buffer [this n-byte buffer offset]
      (.get this
            ^bytes buffer
            offset
            n-byte)
      buffer)

    (rr-u8 [this]
      (binf.int/u8 (.get this)))

    (rr-i8 [this]
      (.get this))

    (rr-u16 [this]
      (binf.int/u16 (.getShort this)))

    (rr-i16 [this]
      (.getShort this))

    (rr-u32 [this]
      (binf.int/u32 (.getInt this)))

    (rr-i32 [this]
      (.getInt this))

    (rr-u64 [this]
      (.getLong this))

    (rr-i64 [this]
      (.getLong this))

    (rr-f32 [this]
      (.getFloat this))

    (rr-f64 [this]
      (.getDouble this))

    (rr-string [this decoder n-byte]
      (let [string (.toString (.decode ^Charset decoder
                                       (-> this
                                           .duplicate
                                           (.limit (+ (.position this)
                                                        n-byte)))))]
        (binf.protocol/skip this
                            n-byte)
        string))


  binf.protocol/IRelativeWriter

    (wr-buffer [this buffer offset n-byte]
      (.put this
            ^bytes buffer
            offset
            n-byte)
      this)

    (wr-b8 [this integer]
      (.put this
            (unchecked-byte integer))
      this)

    (wr-b16 [this integer]
      (.putShort this
                 (unchecked-short integer))
      this)

    (wr-b32 [this integer]
      (.putInt this
               (unchecked-int integer))
      this)

    (wr-b64 [this integer]
      (.putLong this
                integer)
      this)

    (wr-f32 [this floating]
      (.putFloat this
                 floating)
      this)

    (wr-f64 [this floating]
      (.putDouble this
                  floating)
      this)

    (wr-string [this string]
      (let [encoder     (.newEncoder binf.string/encoder-utf-8)
            char-buffer (if (instance? CharBuffer
                                       string)
                          string
                          (CharBuffer/wrap ^String string))
            position-bb (.position this)
            position-cb (.position ^CharBuffer char-buffer)
            res         (.encode encoder
                                 char-buffer 
                                 this
                                 true)
            n-byte      (- (.position this)
                           position-bb)
            n-chars     (- (.position ^CharBuffer char-buffer)
                           position-cb)]
        (condp =
               res
          CoderResult/UNDERFLOW [true n-byte n-chars]
          CoderResult/OVERFLOW  [false n-byte n-chars char-buffer]
          (throw (ex-info (str "Unable to write string: "
                               string)
                          {:binf/error  :string-encoding
                           :binf/string string})))))

        ;; It seems the encoder does not need to be flushed when writing UTF-8
        ;;
        ; (condp =
        ;        (.flush encoder
        ;                this)
        ;   CoderResult/UNDERFLOW (- (.position this)
        ;                            offset)
        ;   CoderResult/OVERFLOW  (throw (ex-info "Not enough bytes to flush string encoder"
        ;                                         {::error  :insufficient-output
        ;                                          ::string string}))
        ;   (throw (ex-info "Unable to flush string encoder"
        ;                   {::error  :string-encoding
        ;                    ::string string}))))


  binf.protocol/IPosition

    (limit [this]
      (.limit this))

    (position [this]
      (.position this))

    (seek [this position]
      (.position this
                 (binf.protocol/-array-index this
                                             position))
      this)

    (skip [this n-byte]
      (.position this
                 (+ (.position this)
                    n-byte))
      this)


  binf.protocol/IViewable


    (view
      
      ([this]
       (doto (.duplicate this)
         (.order (.order this))))

      ([this offset]
       (doto (-> this
                 .duplicate
                 (.position offset)
                 .slice)
         (.order (.order this))))

      ([this offset n-byte]
        (-> this
            (binf.protocol/view offset)
            (.limit n-byte)))))


;;;;;;;;;; Creating views from byte arrays


(extend-type (Class/forName "[B")

  binf.protocol/IPosition

    (limit [this]
      (count this))


  binf.protocol/IViewable

    (view
     
      ([this]
       (ByteBuffer/wrap this))

      ([this offset]
       (-> (ByteBuffer/wrap this)
           (.position offset)
           .slice))

      ([this offset n-byte]
       (-> (ByteBuffer/wrap this
                            offset
                            n-byte)
           .slice))))
