# Changelog

All notable changes to this project will be documented in this file.



## [Unreleased]

### Added

### Changed

- In JS, coercion to f32 became a real operation instead of a no-op

### Fixed



## [1.1.0-beta0] - 2021-04-03

### Changed

- In JS, coercion to f32 became a real operation instead of a no-op



## [1.0.0-beta1] - 2021-03-18

### Fixed

- Cljdoc analysis



## [1.0.0-beta0] - 2021-03-17

### Added

- C-like ABI utilities, creating C types in EDN
- Creating views over native memory (JVM, `DirectByteBuffer`)
- Endianess detection and swapping
- LEB128 encoding/decoding
- Handling native pointers
- More 64-bit integer utilities
- R/W booleans

### Changed

- Complete API reorganization, core namespace split into specific ones
- Remove custom types and implement protocols directly on host classes
    - `ByteBuffer` on the JVM
    - `DataView` in JS
- Relicense under MPL 2.0

### Fixed

- Existing 64-bit integer utilities which were poorly implemented

### Removed

- Growing views in favor of a single `grow` function



## [0.0.0-beta0] - 2020-11-04

### Added

- Base64 encoding/decoding
- Proper README

### Changed

- Renamed `copy` to `copy-buffer`
- Renamed group id from 'dvlopt' to 'helins'



## [0.0.0-alpha4] - 2020-08-15

### Fixed

- Seeking a view with non-zero offset (JVM)



## [0.0.0-alpha3] - 2020-08-11

### Added

- Buffer copying
- Better R/W of buffers to view
- Docstrings
- Predicate testing if a view can grow

### Fixed

- Absolute positioning when view has an offset (JVM)

### Removed

- Aliases to bitwise operations



## [0.0.0-alpha2] - 2020-06-17

### Fixed

- Ensure views are always big endian both on the JVM and in JS



## [0.0.0-alpha1] - 2020-05-30

### Added

- Macro for creating a buffer



## [0.0.0-alpha0] - 2020-05-13

### Added

- First API iteration
    - Aliases for some bitwise operations
    - Coercions between primitives
    - Concept of growing views, views that can reallocate more data
    - R/W primitives, strings, buffers



[Unreleased]: https://github.com/helins/binf.cljc/compare/1.0.0-beta1...HEAD
[1.0.0-beta1]: https://github.com/helins/binf.cljc/compare/1.0.0-beta0...1.0.0-beta1
[1.0.0-beta0]: https://github.com/helins/binf.cljc/compare/0.0.0-beta0...1.0.0-beta0
[0.0.0-beta0]: https://github.com/helins/binf.cljc/compare/0.0.0-alpha4...0.0.0-beta0
[0.0.0-alpha4]: https://github.com/helins/binf.cljc/compare/0.0.0-alpha3...0.0.0-alpha4
[0.0.0-alpha3]: https://github.com/helins/binf.cljc/compare/0.0.0-alpha2...0.0.0-alpha3
[0.0.0-alpha2]: https://github.com/helins/binf.cljc/compare/0.0.0-alpha1...0.0.0-alpha2
[0.0.0-alpha1]: https://github.com/helins/binf.cljc/compare/0.0.0-alpha0...0.0.0-alpha1
[0.0.0-alpha0]: https://github.com/helins/binf.cljc/tree/0.0.0-alpha0
