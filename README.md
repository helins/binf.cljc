# BinF stands for "Binary Formats"

[![Clojars
Project](https://img.shields.io/clojars/v/io.helins/binf.svg)](https://clojars.org/io.helins/binf)

[![cljdoc badge](https://cljdoc.org/badge/io.helins/binf)](https://cljdoc.org/d/io.helins/binf)

Clojure(script) library for handling any kind of binary format, protocol ; both
in-memory and during IO ; and helping interacting with native libraries and WebAssembly modules.

An authentic Swiss army knife providing:

- Reading, writing, and copying binary data
- Via protocols which enhance host classes (`js/DataView` in JS, `ByteBuffer` on the
    JVM, ...)
- Coercions between primitive types
- Cross-platform handling of 64-bit integers
- Excellent support for IO and even memory-mapped files on the JVM
- Extra utilities such as Base64 encoding/decoding, LEB128, ...
- Defining C-like composite types (structs, unions, ...) as EDN


Table of content:

- [Rationale](#rationale)
- [Examples](#examples)
- [Usage](#usage)
    - [Buffers and views](#buffers_and_views)
    - [Binary data and operations](#binary_data)
    - [Creating a view from a buffer](#view_from_buffer)
    - [Creating a view over a memory-mapped-file (JVM)](#mmap)
    - [Creating a view from a view](#view_from_view)
    - [Working with dynamically-sized data](#dynamic_data)
    - [Working with 64-bit integers](#int64)
    - [Extra utilities](#extra)
    - [Interacting with native libraries and WebAssembly](#native)
- [Running tests](#tests)
- [Development](#develop)


## Rationale <a name="rationale">

Clojure libraries for handling binary data are typically limited and not very
well maintained. BinF is the only library providing a seamless experience
between Clojure and Clojurescript for pretty much any use case with an extensive
set of tools built with low-level performance in mind. While in beta, it has
already been used in production and for involving projects such as a WebAssembly decompiler/compiler.

## Examples <a name="examples">

All examples from the "[Usage](#usage)" section as well as more complete ones
are in the [./src/example/helins/binf](../main/src/example/helins/binf) directory.
They are well-described and meant to be tried out at the REPL.

Also, the [helins.binf.dev](../main/src/dev/helins/binf/dev.cljc) namespace
requires all namespaces of this library (quite a few) and can be used for
REPLing around.

Cloning this repo is a fast way of trying things out. See the
"[Development](#develop)" section.

## Usage <a name="usage">

This is an overview.

After getting a sense of the library, it is best to try out full examples and
explore the [full API](https://cljdoc.org/d/io.helins/binf) which describes more
namespaces.

Let us require the main namespaces used in this document:

```clojure
(require '[helins.binf        :as binf]
         '[helins.binf.buffer :as binf.buffer])
```

### Buffers and views <a name="buffers_and_views">

BinF is highly versatile because it leverages what the host offers, following the
Clojure mindset. The following main concepts must be understood.

A view is an object encompassing a raw chunk of memory and offering utilities for
manipulating it: reading and/or writing binary data. Such a chunk of memory
could be a byte array or a file. It does not really matter since views abstract
those chunks.

More precisely, a view is anything that implement at least some of the protocols
defined in the `helins.binf.protocol` namespace. Only rarely will the user
implement anything since BinF already enhances common classes.

On the JVM, those protocols are implemented for the ubiquitous
[ByteBuffer](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/nio/ByteBuffer.html)
which is used pretty much everywhere. In JS, they enhance the just-as-ubiquitous
[DataView](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/DataView).

By enhancing these host classes, code can be reused for many contexts: handling
memory, handling a file, a socket, ...

Finally, by definition, a buffer is an opaque byte array which can be manipulated only
via a view. It represents the lowest-level of directly accessible memory a host
can provide. On the JVM, a buffer is a plain old byte array. In JS, it is an [ArrayBuffer](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/ArrayBuffer)
or optionally a
[SharedArrayBuffer](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/SharedArrayBuffer).

Many host utilities expect buffers hence it is important to define a coherent
story between buffers and views.

### Binary data and operations <a name="binary_data">

Types and related operations follow a predictable naming convention.

The following table summarizes primitive binary types and their names:


| Type | Description |
|---|---|
| buffer | Byte array |
| f32 | 32-bit float |
| f64 | 64-bit float |
| i8 | Signed 8-bit integer |
| i16 | Signed 16-bit integer |
| i32 | Signed 32-bit integer |
| i64 | Signed 64-bit integer |
| string | String (UTF-8 by default) |
| u8 | Unsigned 8-bit integer |
| u16 | Unsigned 16-bit integer |
| u32 | Unsigned 32-bit integer |
| u64 | Unsigned 64-bit integer |

Reading and writing revolve around these types and happen at a specific position
in a view. In **absolute operations**, that position is provided by the user
explicitly. In **relative operations**, views use an internal position they maintain
themselves.

It is much more common to use relative operations since it is more
common to read or write things in a sequence. For instance, writing a 32-bit
integer will then advance that internal position by 4 bytes.

When writing integers, sign do not matter. For instance, instead of specifying
`i32` or `u32`, `b32` is used since only the bit pattern matters.

These operations are gathered in the core [helins.binf](https://cljdoc.org/d/io.helins/binf/0.0.0-beta0/api/helins.binf)
namespace. Some examples showing the naming convention are:

| Operation | Description |
|---|---|
| wa-b32 | Write a 32-bit integer at an absolute position |
| rr-i64 | Read a signed 64-bit integer from the current relative position |
| wr-buffer | Copy the given buffer to the current relative position |
| ra-string | Read a string from an absolute position |

The first letter denotes `r`eading or `w`riting, the second letter denotes
`a`bsolute or `r`elative.

It is best to follow that naming convention when writing business logic.

For instance, writing and reading a `YYYY/mm/dd` date "relatively":

```clojure
(defn wr-date
  [view year month day]
  (-> view
      (binf/wr-b16 year)
      (binf/wr-b8 month)
      (binf/wr-b8 day)))


(defn rr-date
  [view]
  [(binf/rr-u16 view)
   (binf/rr-u8 view)
   (binf/rr-u8 view)])
```

### Creating a view from a buffer <a name="view_from_buffer">

Complete example in the [helins.binf.example](../main/src/example/helins/binf/example.cljc)
namespace.


```clojure
;; Allocating a buffer of 1024 bytes
;;
(def my-buffer
     (binf.buffer/alloc 1024))

;; Wrapping the buffer in view
;;
(def my-view
     (binf/view my-buffer))

;; The buffer can always be extracted from its view
;;
(binf/backing-buffer my-view)
```

Using our date functions defined in the previous section:

```clojure
;; From the current position (0 for a new view)
;;
(let [position-date (binf/position my-view)]
  (-> my-view
      (wr-date 2021
               3
               16)
      (binf/seek position-date)
      rr-date))

;; => [2021 3 16]

```

### Creating a view over a memory-mapped file (JVM) <a name="mmap">

Complete example in the [helins.binf.example.mmap-file](../main/src/example/helins/binf/example/mmap_file.clj)
namespace.

On the JVM, BinF protocols already extends the popular `ByteBuffer` used
extensively by many utilities, amongst them IO ones (about anything in
`java.nio`).

One notable mention is the child class
[MappedByteBuffer](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/nio/MappedByteBuffer.html),
a special type of `ByteBuffer` which memory-maps a file. This technique usually
results in fast and efficient IO for larger file while being easy to follow.

Our date functions used in the previous section be applied to
such a memory-mapped file without any change.

There are a few ways for obtaining a `MappedByteBuffer`, here is one example:


```clojure
(import 'java.io.RandomAccessFile
        'java.nio.channels.FileChannel$MapMode)

(with-open [file (RandomAccessFile. "/tmp/binf-example.dat"
                                    "rw")]
  (let [view (-> file
                 .getChannel
                 (.map FileChannel$MapMode/READ_WRITE
                       ;; From byte 0 in the file
                       0
                       ;; A size in bytes, we know a date is 4 bytes
                       4))]
    (-> view
        ;; Writing date
        (wr-date 2021
                 3
                 16)
        ;; Ensuring changes are persisted on disk
        .force
        ;; Reading it back from the start of the file
        (binf/seek 0)
        rr-date)))
```

### Creating a view from a view <a name="view_from_view">

It is often useful to create "sub-views" of a view. Akin to wrapping a buffer, a
view can wrap a view:

```clojure
;; An offset of a 100 bytes with a window of 200 bytes
;;
(def sub-view
     (binf/view my-view
                100
                200))

;; The position of that sub-view starts transparently at 0
;;
(= 0
   (binf/position sub-view))

;; Contains 200 bytes indeed
;;
(= 200
   (binf/limit sub-view))
```

### Working with dynamically-sized data <a name="dynamic_data">

While reading data in a sequence is easy, writing can sometimes be a bit tricky
since one has to decide how much memory to allocate.

Sometimes, the lenght of the data is known in advance and writing is straightforward.

Sometimes, size can be estimated and one can pessimistically allocate more than needed
to cover all cases.

Sometimes, size is unknown but easy to compute. A first pass throught the data
computes the total number of bytes, a second pass actually writes it without
fearing of overflowing and having to check defensively if there is enough space.

And sometimes, size is not trivial to compute or impossible. In one pass, the user
must check defensively if there is enough memory for the next bit of data (eg. a date)
and then write that bit.

Anyway, when space is lacking, the user can grow a view, meaning copying in one
go the content of a view to a new bigger one:

```clojure
;; Asking for a view which contains 256 additional bytes.
;; Current position is preserved.
;;
(def my-view-2
     (binf/grow my-view
                256)
```

### Working with 64-bit integers <a name="int64">

Working with 64-bit integers is tricky since the JVM does not have unsigned ones
and JS engines do not even really have 64-bit integers at all. The
`helins.binf.int64` namespace provide utilities for working with them in a
cross-platform fashion.

It is not the most beautiful experience one will encounter in the course of a lifetime
but it works and does the job pretty efficiently.

### Extra utilities <a name="extra">

Other namespaces provides utilities such as Base64 encoding/decoding, LEB128
encoding/decoding, ...

It is best to [navigate through the
API](https://cljdoc.org/d/io.helins/binf).


### Interacting with native libraries and WebAssembly <a name="native">

Complete example in the [helins.binf.example.cabi](../main/src/example/helins/binf/example/cabi.cljc)
namespace.

Clojure is expanding, reaching new fronts through GraalVM, WebAssembly, new ways
of calling native code.

Although the C language does not have a defined
[ABI](https://en.wikipedia.org/wiki/Application_binary_interface), many tools
and languages understand a C-like ABI. For instance, the Rust programming
language allows for defining structures which follow the same rules as C
structures. This is because such rules are often well-defined, straightforward,
and there is a need for different languages and tools to understand each other
(eg. a shared native library).

The `helins.binf.cabi` namespace provides utilities for following those rules,
for instance when defining structures (eg. order of data members, specific aligment
of members depending on size, ...)

Those definitions can be reused for different architectures and ultimately
end up being plain old EDN, meaning they can be used in many different ways,
especially in combination with the view utilities seen before.

For instance, on the JVM, `DirectByteBuffer` which already extends view
protocols is often used in JNI for calling native code. In JS, [WebAssembly
memories](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/WebAssembly/Memory)
are buffers which can be wrapped in views. This provides exciting
possibilities.

Here is an example of defining a C structure for our date. Let us supposed it is
meant to be used with WebAssembly which is (as of today) 32-bit:

```clojure
(require '[helins.binf.cabi :as binf.cabi])


;; This information map defines a 32-bit modern architecture where words
;; are 4 bytes
;;
(def env32
     (binf.cabi/env 4))

(=  env32

    {:binf.cabi/align          4
     :binf.cabi.pointer/n-byte 4})


;; Defining a function computing our C date structure
;;
(def fn-struct-date
     (binf.cabi/struct :MyDate
                       [[:year  binf.cabi/u16]
                        [:month binf.cabi/u8]
                        [:day   binf.cabi/u8]]))


;; Computing our C date structure as EDN for a WebAssembly environment
;;
(= (fn-struct-date env32)

   {:binf.cabi/align          2
    :binf.cabi/n-byte         4
    :binf.cabi/type           :struct
    :binf.cabi.struct/layout  [:year
                               :month
                               :day]
    :binf.cabi.struct/member+ {:day   {:binf.cabi/align  1
                                       :binf.cabi/n-byte 1
                                       :binf.cabi/offset 3
                                       :binf.cabi/type   :u8}
                               :month {:binf.cabi/align  1
                                       :binf.cabi/n-byte 1
                                       :binf.cabi/offset 2
                                       :binf.cabi/type  :u8}
                               :year {:binf.cabi/align  2
                                      :binf.cabi/n-byte 2
                                      :binf.cabi/offset 0 
                                      :binf.cabi/type   :u16}}
    :binf.cabi.struct/type    :MyDate})
```

This date structure, in a 32-bit WebAssembly, is 4 bytes, aligns on a multiple
of 2 bytes. It is a `:struct` called `:MyDate` and all data members are clearly
layed out with their memory offsets computed.

A more challenging example would not be so easy to compute by hand.

## Running tests <a name="tests">

On the JVM, using [Kaocha](https://github.com/lambdaisland/kaocha):

```bash
$ ./bin/test/jvm/run
$ ./bin/test/jvm/watch
```
On NodeJS, using [Kaocha-CLJS](https://github.com/lambdaisland/kaocha-cljs):

```bash
$ ./bin/test/node/run
$ ./bin/test/node/watch
```

In the browser, using [Chui](https://github.com/lambdaisland/chui):
```
$ ./bin/test/browser/compile
# Then open ./resources/chui/index.html

# For testing an advanced build
$ ./bin/test/browser/advanced
```

## Development <a name="develop">

Starting in Clojure JVM mode, mentioning an additional deps alias (here, a local
setup of NREPL):
```bash
$ ./bin/dev/clojure :nrepl
```

Starting in CLJS mode using Shadow-CLJS:
```bash
$ ./bin/dev/cljs
# Then open ./resources/public/index.html
```


## License

Copyright © 2020 Adam Helinski and Contributors

Licensed under the term of the Mozilla Public License 2.0, see LICENSE.
