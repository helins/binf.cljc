(ns helins.binf.protocol.impl

  ""

  {:author "Adam Helinski"
   :no-doc true}

  (:require [helins.binf.buffer   :as binf.buffer]
            [helins.binf.protocol :as binf.protocol]
            [helins.binf.string   :as binf.string]))


;;;;;;;;;;


(deftype View [^js/DataView dataview
               ^:mutable    little-endian?
               ^:mutable    -position]

  ICounted


    (-count [_]
      (.-byteLength dataview))


  binf.protocol/IAbsoluteReader

    (ra-buffer [this position n-byte]
      (binf.protocol/ra-buffer this
                               position
                               n-byte
                               (binf.buffer/alloc n-byte)
                               0))

    (ra-buffer [this position n-byte buffer]
      (binf.protocol/ra-buffer this
                               position
                               n-byte
                               buffer
                               0))

    (ra-buffer [this position n-byte buffer offset]
      (binf.buffer/copy buffer
                        offset
                        (binf.protocol/to-buffer this)
                        (+ (.-byteOffset dataview)
                           position)
                        n-byte))

    (ra-u8 [_ position]
      (.getUint8 dataview
                 position
                 little-endian?))

    (ra-i8 [_ position]
      (.getInt8 dataview
                position
                little-endian?))

    (ra-u16 [_ position]
      (.getUint16 dataview
                  position
                  little-endian?))

    (ra-i16 [_ position]
      (.getInt16 dataview
                 position
                 little-endian?))

    (ra-u32 [_ position]
      (.getUint32 dataview
                  position
                  little-endian?))

    (ra-i32 [_ position]
      (.getInt32 dataview
                 position
                 little-endian?))

    (ra-i64 [_ position]
      (.getBigInt64 dataview
                    position
                    little-endian?))

    (ra-f32 [_ position]
      (.getFloat32 dataview
                   position
                   little-endian?))

    (ra-f64 [_ position]
      (.getFloat64 dataview
                   position
                   little-endian?))

    (ra-string [this position n-byte]
      (binf.protocol/ra-string this
                               nil
                               position
                               n-byte))

    (ra-string [_this decoder position n-byte]
      (.decode (or decoder 
                   binf.string/decoder-utf-8)
               (js/Uint8Array. (.-buffer dataview)
                               (+ (.-byteOffset dataview)
                                  position)
                               n-byte)))


  binf.protocol/IAbsoluteWriter

    (wa-buffer [this position buffer]
      (binf.protocol/wa-buffer this
                               position
                               buffer
                               0
                               (count buffer)))

    (wa-buffer [this position buffer offset]
      (binf.protocol/wa-buffer this
                               position
                               buffer
                               offset
                               (- (count buffer)
                                  offset)))

    (wa-buffer [this position buffer offset n-byte]
      (binf.buffer/copy (binf.protocol/to-buffer this)
                        (+ (.-byteOffset dataview)
                           position)
                        buffer
                        offset
                        n-byte)
      this)

    (wa-b8 [this position integer]
       (.setUint8 dataview
                  position
                  integer
                  little-endian?)
        this)

    (wa-b16 [this position integer]
      (.setUint16 dataview
                  position
                  integer
                  little-endian?)
      this)

    (wa-b32 [this position integer]
      (.setUint32 dataview
                  position
                  integer
                  little-endian?)
      this)

    (wa-b64 [this position integer]
      (.setBigInt64 dataview
                    position
                    integer
                    little-endian?)
      this)

    (wa-f32 [this position floating]
      (.setFloat32 dataview
                   position
                   floating
                   little-endian?)
      this)

    (wa-f64 [this position floating]
      (.setFloat64 dataview
                   position
                   floating
                   little-endian?)
      this)

    (wa-string [_this position string]
      (let [res         (.encodeInto binf.string/encoder-utf-8
                                     string
                                     (js/Uint8Array. (.-buffer dataview)
                                                     (+ (.-byteOffset dataview)
                                                        position)
                                                     (- (.-byteLength dataview)
                                                        position)))
            read-UTF-16 (.-read res)]
        [(= (.-length string)
            read-UTF-16)
         (.-written res)
         read-UTF-16]))


  binf.protocol/IEndianess


    (endianess [_]
      (if little-endian?
        :little-endian
        :big-endian))

    (endianess [this new-endianess]
      (set! little-endian?
            (case new-endianess
              :big-endian    false
              :little-endian true))
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
      (let [b (binf.protocol/ra-buffer this
                                       (binf.protocol/position this)
                                       n-byte
                                       buffer
                                       offset)]
        (binf.protocol/skip this
                            n-byte)
        b))

    (rr-u8 [this]
      (let [ret (binf.protocol/ra-u8 this
                                     -position)]
        (set! -position
              (inc -position))
        ret))

    (rr-i8 [this]
      (let [ret (binf.protocol/ra-i8 this
                                     -position)]
        (set! -position
              (inc -position))
        ret))

    (rr-u16 [this]
      (let [ret (binf.protocol/ra-u16 this
                                      -position)]
        (set! -position
              (+ -position
                 2))
        ret))

    (rr-i16 [this]
      (let [ret (binf.protocol/ra-i16 this
                                      -position)]
        (set! -position
              (+ -position
                 2))
        ret))

    (rr-u32 [this]
      (let [ret (binf.protocol/ra-u32 this
                                      -position)]
        (set! -position
              (+ -position
                 4))
        ret))

    (rr-i32 [this]
      (let [ret (binf.protocol/ra-i32 this
                                      -position)]
        (set! -position
              (+ -position
                 4))
        ret))

    (rr-i64 [this]
      (let [ret (binf.protocol/ra-i64 this
                                      -position)]
        (set! -position
              (+ -position
                 8))
        ret))

    (rr-f32 [this]
      (let [ret (binf.protocol/ra-f32 this
                                      -position)]
        (set! -position
              (+ -position
                 8))
        ret))

    (rr-f64 [this]
      (let [ret (binf.protocol/ra-f64 this
                                      -position)]
        (set! -position
              (+ -position
                 8))
        ret))

    (rr-string [this n-byte]
      (binf.protocol/rr-string this
                               nil
                               n-byte))

    (rr-string [this decoder n-byte]
      (let [string (binf.protocol/ra-string this
                                            decoder
                                            -position
                                            n-byte)]
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
      (binf.protocol/wa-buffer this
                               (binf.protocol/position this)
                               buffer
                               offset
                               n-byte)
      (binf.protocol/skip this
                          n-byte)
      this)

    (wr-b8 [this integer]
      (binf.protocol/wa-b8 this
                           -position
                           integer)
      (set! -position
            (inc -position))
      this)

    (wr-b16 [this integer]
      (binf.protocol/wa-b16 this
                            -position
                            integer)
      (set! -position
            (+ -position
               2))
      this)

    (wr-b32 [this integer]
      (binf.protocol/wa-b32 this
                            -position
                            integer)
      (set! -position
            (+ -position
               4))
      this)

    (wr-b64 [this integer]
      (binf.protocol/wa-b64 this
                            -position
                            integer)
      (set! -position
            (+ -position
               8))
      this)

    (wr-f32 [this floating]
      (binf.protocol/wa-f32 this
                            -position
                            floating)
      (set! -position
            (+ -position
               8))
      this)

    (wr-f64 [this floating]
      (binf.protocol/wa-f64 this
                            -position
                            floating)
      (set! -position
            (+ -position
               8))
      this)

    (wr-string [this string]
      (let [res (binf.protocol/wa-string this
                                         -position
                                         string)]
        (binf.protocol/skip this
                            (res 1))
        res))


  binf.protocol/IView

    (offset [_]
      (.-byteOffset dataview))

    (position [_]
      -position)

    (seek [this position]
      (set! -position
            position)
      this)

    (skip [this n-byte]
      (set! -position
            (+ -position
               n-byte))
      this)

    (to-buffer [_]
      (.-buffer dataview))


  binf.protocol/IViewBuilder

    (view [this]
      this)

    (view [_this offset]
      (View. (js/DataView. (.-buffer dataview)
                           (+ (.-byteOffset dataview)
                              offset))
             little-endian?
             0))

    (view [_this offset size]
      (View. (js/DataView. (.-buffer dataview)
                           (+ (.-byteOffset dataview)
                              offset)
                           size)
             little-endian?
             0)))


;;;;;;;;;;


(defn- -buffer->view

  ;;


  ([buffer]

   (View. (js/DataView. buffer)
          false
          0))


  ([buffer offset]

   (View. (js/DataView. buffer
                        offset)
          false
          0))


  ([buffer offset n-byte]

   (View. (js/DataView. buffer
                        offset
                        n-byte)
          false
          0)))



(extend-protocol binf.protocol/IViewBuilder


  js/ArrayBuffer

    (view

      ([this]
       (-buffer->view this))

      ([this offset]
       (-buffer->view this
                      offset))

      ([this offset n-byte]
       (-buffer->view this
                      offset
                      n-byte)))


    js/SharedArrayBuffer

      (view

        ([this]
         (-buffer->view this))

        ([this offset]
         (-buffer->view this
                        offset))

        ([this offset n-byte]
         (-buffer->view this
                        offset
                        n-byte))))
