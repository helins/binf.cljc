;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns helins.binf.protocol.impl

  ;; JS implementation of BinF protocols.

  {:author "Adam Helinski"
   :no-doc true}

  (:require [goog.object]
            [helins.binf.buffer   :as binf.buffer]
            [helins.binf.protocol :as binf.protocol]
            [helins.binf.string   :as binf.string]))


;;;;;;;;;;


(def ^:private -k-little-endian?

  ;;

  (js/Symbol))



(def ^:private -k-position

  ;;

  (js/Symbol))



(defn- -little-endian?

  ;;

  [this]

  (goog.object/get this
                   -k-little-endian?))



(defn- -duplicate-endianess

  ;;

  [view view-from]

  (goog.object/set view
                   -k-little-endian?
                   (goog.object/get view-from
                                    -k-little-endian?))
  view)



(defn- -set-position

  ;;

  [view position]

  (goog.object/set view
                   -k-position
                   position)
  view)



(defn- -init-view

  ;;
  

  ([view]

   (-set-position view
                  0))


  ([view view-parent]

   (-> view
       -init-view
       (-duplicate-endianess view-parent))))



(extend-type js/DataView


  binf.protocol/IAbsoluteReader

    (ra-buffer [this position n-byte buffer offset]
      (binf.buffer/copy buffer
                        offset
                        (binf.protocol/backing-buffer this)
                        (+ (.-byteOffset this)
                           position)
                        n-byte))

    (ra-u8 [this position]
      (.getUint8 this
                 position
                 (-little-endian? this)))

    (ra-i8 [this position]
      (.getInt8 this
                position
                (-little-endian? this)))

    (ra-u16 [this position]
      (.getUint16 this
                  position
                  (-little-endian? this)))

    (ra-i16 [this position]
      (.getInt16 this
                 position
                 (-little-endian? this)))

    (ra-u32 [this position]
      (.getUint32 this
                  position
                  (-little-endian? this)))

    (ra-i32 [this position]
      (.getInt32 this
                 position
                 (-little-endian? this)))

    (ra-u64 [this position]
      (.getBigUint64 this
                     position
                     (-little-endian? this)))

    (ra-i64 [this position]
      (.getBigInt64 this
                    position
                    (-little-endian? this)))

    (ra-f32 [this position]
      (.getFloat32 this
                   position
                   (-little-endian? this)))

    (ra-f64 [this position]
      (.getFloat64 this
                   position
                   (-little-endian? this)))

    (ra-string [this decoder position n-byte]
      (.decode decoder 
               (js/Uint8Array. (.-buffer this)
                               (+ (.-byteOffset this)
                                  position)
                               n-byte)))


  binf.protocol/IAbsoluteWriter

    (wa-buffer [this position buffer offset n-byte]
      (binf.buffer/copy (binf.protocol/backing-buffer this)
                        (+ (.-byteOffset this)
                           position)
                        buffer
                        offset
                        n-byte)
      this)

    (wa-b8 [this position integer]
       (.setUint8 this
                  position
                  integer
                  (-little-endian? this))
        this)

    (wa-b16 [this position integer]
      (.setUint16 this
                  position
                  integer
                  (-little-endian? this))
      this)

    (wa-b32 [this position integer]
      (.setUint32 this
                  position
                  integer
                  (-little-endian? this))
      this)

    (wa-b64 [this position integer]
      (.setBigInt64 this
                    position
                    integer
                    (-little-endian? this))
      this)

    (wa-f32 [this position floating]
      (.setFloat32 this
                   position
                   floating
                   (-little-endian? this))
      this)

    (wa-f64 [this position floating]
      (.setFloat64 this
                   position
                   floating
                   (-little-endian? this))
      this)

    (wa-string [this position string]
      (let [res         (.encodeInto binf.string/encoder-utf-8
                                     string
                                     (js/Uint8Array. (.-buffer this)
                                                     (+ (.-byteOffset this)
                                                        position)
                                                     (- (.-byteLength this)
                                                        position)))
            read-UTF-16 (.-read res)]
        [(= (.-length string)
            read-UTF-16)
         (.-written res)
         read-UTF-16]))


  binf.protocol/IBackingBuffer

    (backing-buffer [this]
      (.-buffer this))

    (buffer-offset [this]
      (.-byteOffset this))


  binf.protocol/IEndianess


    (endian-get [this]
      (if (-little-endian? this)
        :little-endian
        :big-endian))

    (endian-set [this endianess]
      (goog.object/set this
                       -k-little-endian?
                       (case endianess
                         :big-endian    false
                         :little-endian true))
      this)


  binf.protocol/IGrow

    (grow [this n-additional-byte]
      (-> (js/DataView. (binf.protocol/grow (.-buffer this)
                                            n-additional-byte))
          (-set-position (binf.protocol/position this))
          (-duplicate-endianess this)))


  binf.protocol/IRelativeReader

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
      (let [position (goog.object/get this
                                      -k-position)
            ret      (binf.protocol/ra-u8 this
                                          position)]
        (goog.object/set this
                         -k-position
                         (inc position))
        ret))

    (rr-i8 [this]
      (let [position (goog.object/get this
                                      -k-position)
            ret      (binf.protocol/ra-i8 this
                                          position)]
        (goog.object/set this
                         -k-position
                         (inc position))
        ret))

    (rr-u16 [this]
      (let [position (goog.object/get this
                                      -k-position)
            ret      (binf.protocol/ra-u16 this
                                           position)]

        (goog.object/set this
                         -k-position
                         (+ position
                            2))
        ret))

    (rr-i16 [this]
      (let [position (goog.object/get this
                                      -k-position)
            ret      (binf.protocol/ra-i16 this
                                           position)]
        
        (goog.object/set this
                         -k-position
                         (+ position
                            2))
        ret))

    (rr-u32 [this]
      (let [position (goog.object/get this
                                      -k-position)
            ret      (binf.protocol/ra-u32 this
                                           position)]
        (goog.object/set this
                         -k-position
                         (+ position
                            4))
        ret))

    (rr-i32 [this]
      (let [position (goog.object/get this
                                      -k-position)
            ret      (binf.protocol/ra-i32 this
                                           position)]
        (goog.object/set this
                         -k-position
                         (+ position
                            4))
        ret))

    (rr-u64 [this]
      (let [position (goog.object/get this
                                      -k-position)
            ret      (binf.protocol/ra-u64 this
                                           position)]
        (goog.object/set this
                         -k-position
                         (+ position
                            8))
        ret))

    (rr-i64 [this]
      (let [position (goog.object/get this
                                      -k-position)
            ret      (binf.protocol/ra-i64 this
                                           position)]
        (goog.object/set this
                         -k-position
                         (+ position
                            8))
        ret))

    (rr-f32 [this]
      (let [position (goog.object/get this
                                      -k-position)
            ret      (binf.protocol/ra-f32 this
                                           position)]
        (goog.object/set this
                         -k-position
                         (+ position
                            4))
        ret))

    (rr-f64 [this]
      (let [position (goog.object/get this
                                      -k-position)
            ret      (binf.protocol/ra-f64 this
                                           position)]
        (goog.object/set this
                         -k-position
                         (+ position
                            8))
        ret))

    (rr-string [this decoder n-byte]
      (let [string (binf.protocol/ra-string this
                                            decoder
                                            (goog.object/get this
                                                             -k-position)
                                            n-byte)]
        (binf.protocol/skip this
                            n-byte)
        string))


  binf.protocol/IRelativeWriter
    
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
      (let [position (goog.object/get this
                                      -k-position)]
         (binf.protocol/wa-b8 this
                              position
                              integer)
         (goog.object/set this
                          -k-position
                          (inc position)))
      this)

    (wr-b16 [this integer]
      (let [position (goog.object/get this
                                      -k-position)]
        (binf.protocol/wa-b16 this
                              position
                              integer)
        (goog.object/set this
                         -k-position
                         (+ position
                            2)))
      this)

    (wr-b32 [this integer]
      (let [position (goog.object/get this
                                      -k-position)]
        (binf.protocol/wa-b32 this
                              position
                              integer)
        (goog.object/set this
                         -k-position
                         (+ position
                            4)))
      this)

    (wr-b64 [this integer]
      (let [position (goog.object/get this
                                      -k-position)]
        (binf.protocol/wa-b64 this
                              position
                              integer)
        (goog.object/set this
                         -k-position
                         (+ position
                            8)))
      this)

    (wr-f32 [this floating]
      (let [position (goog.object/get this
                                      -k-position)]
        (binf.protocol/wa-f32 this
                              position
                              floating)
        (goog.object/set this
                         -k-position
                         (+ position
                            4)))
      this)

    (wr-f64 [this floating]
      (let [position (goog.object/get this
                                      -k-position)]
        (binf.protocol/wa-f64 this
                              position
                              floating)
        (goog.object/set this
                         -k-position
                         (+ position
                            8)))
      this)

    (wr-string [this string]
      (let [res (binf.protocol/wa-string this
                                         (goog.object/get this
                                                          -k-position)
                                         string)]
        (binf.protocol/skip this
                            (res 1))
        res))


  binf.protocol/IPosition

    (limit [this]
      (.-byteLength this))

    (position [this]
      (goog.object/get this
                       -k-position))

    (seek [this position]
      (goog.object/set this
                       -k-position
                       position)
      this)

    (skip [this n-byte]
      (goog.object/set this
                       -k-position
                       (+ (goog.object/get this
                                           -k-position)
                          n-byte))
      this)


  binf.protocol/IViewable

    (view
      
      ([this]
       (-init-view (js/DataView. (.-buffer this)
                                 (.-byteOffset this)
                                 (.-byteLength this))
                   this))

      ([this offset]
       (-init-view (js/DataView. (.-buffer this)
                                 (+ (.-byteOffset this)
                                    offset)
                                 (- (.-byteLength this)
                                    offset))
                   this))

      ([this offset n-byte]
       (-init-view (js/DataView. (.-buffer this)
                                 (+ (.-byteOffset this)
                                    offset)
                                 n-byte)
                   this))))


;;;;;;;;;;


(defn- -buffer->view

  ;;


  ([buffer]

   (-init-view (js/DataView. buffer)))


  ([buffer offset]

   (-init-view (js/DataView. buffer
                             offset)))


  ([buffer offset n-byte]

   (-init-view (js/DataView. buffer
                             offset
                             n-byte))))



(extend-protocol binf.protocol/IViewable


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
                      n-byte))))



(when (exists? js/SharedArrayBuffer)

  (extend-protocol binf.protocol/IViewable

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
                        n-byte)))))
