# BinF stands for "Binary Formats"

[![Clojars
Project](https://img.shields.io/clojars/v/helins/binf.cljc.svg)](https://clojars.org/helins-io/binf.cljc)

[![cljdoc badge](https://cljdoc.org/badge/helins/binf.cljc)](https://cljdoc.org/d/helins-io/binf.cljc)

Cross-platform library for handling any kind of binary format or protocol without any shenaningans.

Provides:

- Reading and writing primitive values to byte arrays: signed/unsigned integers,
    floats, and strings
- Easy copying
- Facilitate working with dynamically sized data
- Relative positioning (akin to Java ByteBuffers)
- Just functions, straightforward and flexible
- Primitive coercions
- Extra utilities such as Base64 encoding/decoding
- Compatible with the JVM, NodeJS, and the browser

## Rationale

As Clojure keeps on expanding, there are more and more places where having proper tooling for manipulating byte
arrays becomes increasingly interesting. Those are but a few examples:

- Custom and fine-tuned serialization
- Sharing data between web workers without copying
- Handling binary protocols, custom or well-known ones (eg. MIDI)
- Lower-level programming (eg. accessing native APIs)
- Talking to hardware

Being cross-platform is important. The story between Clojure and
Clojurescript is a beautiful one and when it comes to serialization, not every use case suits EDN or JSON. Furthermore, the browser - hence Clojurescript - has became quite a capable beast and now allows for somewhat lower-level forms of programming (eg. handling files, talking to MIDI devices, USB or bluetooth ones).

Prior work in this field is often constrained to the JVM and/or too opiniated,
resulting in poor flexibility.

## Usage

After reading the following overview, go explore the [full API](https://cljdoc.org/d/helins-io/binf.cljc) which really sheds light on what is possible.

### Buffers and Views

A `buffer` is a fixed-size collection of bytes. On the JVM, it is a simple byte array. In
Clojurescript, it is an
[ArrayBuffer](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/ArrayBuffer).

```clojure
(require '[helins/binf :as binf])

(def my-buffer
     (binf/buffer 1024))
```

However, most operations are performed via a `view`.

```clojure
(def my-view
     (binf/view my-buffer))
```

A `view` maintains a current position to which all "relative" operations refer.
For instance, reading a 32-bit integer will advance this position by 4 bytes.
A same set of operations is provided for acting upon an "absolute" position
provided by the user and leaving the relative position unchanged.

As a mnemonic, those operations are functions referring to a primivite value prefixed by 2 letters. The first one is `r`
(read) or `w` (write). The second is `r` (relative) or `a` (absolute). Primitives
naming is akin to the convention used by the Rust programming language. For
instance:

```clojure
;; Relative to the current position, write a byte and a 32-bit integer
;; (sign is irrelevant, only the bit pattern is important when writing)
;;
(-> view
    (binf/wr-b8 42)
    (binf/wr-b32 1000))


;; Relative position in bytes is updated
;;
(= (binf/position view)
   5)

;; At absolute positions, read our data as unsigned integers
;;
[(binf/ra-u8 view
             0)
 (binf/ra-u32 view
              1)]

;; = [42 1000]


;; We could have rewind our data and used relative positioning
;;
(binf/seek view
           0)

[(binf/rr-u8 view
             0)
 (binf/rr-u32 view
              1)]
```

### Growing views

Sometimes, when writing data, the end size is unknown and working with a
fixed-size buffer becomes tedious. A `growing view` intially wraps a given buffer
just like a regular view but when the end is reached, it transparently allocates
a bigger one under the hood.

```clojure
(def my-growing-view
     (binf/growing-view (binf/buffer 100)))


;; Yeah, a 100 bytes will not be enough
;;
(dotimes [i 4000]
  (binf/wr-b16 my-growing-view
               i))

;; No worries
;;
(= (count my-growing-buffer)
   8675)
```

This reallocation strategy is a simple one, yet it is quite effective on the
longer term. Behavior is configurable (by default, the new buffer is 1.5 the size of the previous one).


## License

Copyright © 2020 Adam Helinski

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
