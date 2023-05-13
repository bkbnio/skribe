# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.2.4] - 05/13/2023

### Changed

- Do not use type alias for Uuid

## [0.2.3] - 05/11/2023

### Changed 

- Misc bug fixes

## [0.2.2] - 05/11/2023

### Changed

- Account for ArraySchema when generating response types

## [0.2.1] - 05/11/2023

### Added

- SerialName annotations when property has been renamed

## [0.2.0] - 05/11/2023

### Added

- Type hinting for possible API responses

### Changed

- Turned into a pretty much ground up rewrite to leverage the official OpenAPI parser instead of Spekt

## [0.1.3] - 05/07/2023

### Added

- Pipeline to publish codegen artifacts to Sonatype

## [0.1.2] - 05/07/2023

### Added

- Playground module for easy manual testing

### Changed

- Codegen module now lives in Skribe instead of Spekt
- `spektGenerate` can now clean (wipe) output directory ahead of code write if directed.

## [0.1.1] - 05/05/2023

### Added

- Now supports custom package for generated code

## [0.1.0] - 05/05/2023

### Added

- Initial plugin implementation

## [0.0.1] - 05/04/2023

- Started my project using [sourdough](https://github.com/bkbnio/sourdough-kt) ❤️
- Setup initial Gradle plugins with library and root configurations
