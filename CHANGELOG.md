# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.12.0] - October 3rd, 2022

### Added

- Library Multiplatform returns... kinda

## [0.11.0] - October 3rd, 2022

### Added

- Gitbook integration

### Changed

- Dropped MPP for the moment

## [0.10.0] - September 29th, 2022

### Changed
- Bump to Kotlin 1.7.20
- Dropped Dokka Support

## [0.9.2] - August 26th, 2022
### Changed
- Bumped Kover version

## [0.9.1] - August 15th, 2022
### Changed
- Couple misc version bumps

## [0.9.0] - July 4th, 2022
### Changed
- Kotlin 1.7

## [0.8.0] - May 1st, 2022
### Changed

- Differentiated extension names to reduce ambiguity and allow root configuration

## [0.7.0] - April 21st, 2022
### Added
- Gradle Toolchain support

### Changed

- Default JVM target is now 17 (most recent LTS)
- Several version bumps

## [0.6.0] - January 20th, 2022
### Added
- Support for NodeJS target

## [0.5.6] - January 16th, 2022
### Changed
- Bumped Kover to RC2, adjusted accordingly

## [0.5.5] - January 12th, 2022
### Changed
- Fixed bug where kover configuration was not included

## [0.5.4] - January 11th, 2022
### Changed
- Fixed bug where compiler args were not being applied

## [0.5.3] - January 11th, 2022
### Changed
- Fixed bug causing version in library to be null
- Fixed bug causing documentation modules to not show

## [0.5.2] - January 11th, 2022
### Changed
- Fixed bug where submodule documentation was not being generated

## [0.5.1] - January 11th, 2022
### Changed
- Library License is now only included if provided

## [0.5.0] - January 11th, 2022
### Added
- Application Configuration Plugin

## [0.4.1] - January 10th, 2022
### Changed
- Resolved bug where NodeJS extension was not being applied correctly

## [0.4.0] - January 10th, 2022
🚨 Breaking 🚨
### Added
- Experimental Kotlin Multiplatform Library Plugin
### Changed
- Huge refactor, modules are now decoupled and much less rigid

## [0.3.3] - January 8th, 2022
### Changed
- Fixed bug where documentation home page was being generated on plugin load

## [0.3.2] - January 8th, 2022
### Added
- Optional root `Project.md` file to include in documentation
### Changed
- Resolved bug in dokka version plugin configuration

## [0.3.1] - January 8th, 2022
### Added
- Overridable documentation root folder

## [0.3.0] - December 18th, 2021
### Question
- Was it all a dream?

## [0.2.3] - 12/17/2021
### Changes
- Going to try moving signing to after-evaluate block

## [0.2.2] - 12/17/2021
### Changed
- Honestly no idea if this will work, but switched the signing extension to sign the publication rather than the name

## [0.2.1] - 12/17/2021
### Changed
- Sonatype now applied directly, fixes bug where `publishToSonatype` task was not found on root

## [0.2.0] - 12/17/2021
### Changed
- Back to Properties (aka i'm a lazy boi)

## [0.1.2] - 12/17/2021
### Changed
- Modified Action Pipelines to use Gradle Wrapper with built-in caching

## [0.1.1] - 12/17/2021
### Changed
- Allow for JVM Target to be specified separate from JVM Version
- `javaVersion` is now `toolChainJavaVersion`

## [0.1.0] - 12/13/2021
### Changed
- Fixed bug in Dokka setup causing failure if missing folder

## [0.0.5] - 12/12/2021
### Added
- The beginnings of an actual README
### Changed
- Library extension changed to open class with normal strings

## [0.0.4] - 12/12/2021
### Added
- Snapshot Publishing to GitHub packages
### Changed
- Safer Dokka Configuration (will generate necessary setup if not present)
- Bumped gradle to 7.3.1

## [0.0.3] - 12/04/2021
### Changed
- Fixed bug in gradle file for application plugin

## [0.0.2] - 12/04/2021
### Changed
- Added `tags` to plugin to enable push to gradle repository

## [0.0.1] - 12/02/2021

- Started my project using [sourdough](https://github.com/bkbnio/sourdough-kt) ❤️
- Setup initial Gradle plugins with library and root configurations
