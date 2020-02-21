# CHANGELOG

This changelog follows [Keep a Changelog v1.0.0](https://keepachangelog.com/en/1.0.0/).

## Unreleased

## v0.1.4 - 2020-02-21

### Added

* Implement `Slf4jDoNotLogMessageOfExceptionExplicitly`

### Changed

* Bump up Errorprone to version 2.3.4

## v0.1.3 - 2018-10-10

### Fixed

* False positive around logging method that has no parameter

### Changed

* Use JDK9 to build, because Errorprone has stopped support for JDK8

## v0.1.2 - 2018-07-06

### Fixed

* NPE in Slf4jIllegalPassedClass
* false positive in Slf4jIllegalPassedClass
* false positive in Slf4jPlaceholderMismatch

## v0.1.1 - 2018-07-05

### Added

* Slf4jIllegalPassedClass suggests not only inner class but also outer classes
* new Slf4jFormatShouldBeConst bug pattern
* new Slf4jSignOnlyFormat bug pattern

### Fixed

* Many bugs in PlaceholderMismatch bug pattern

## v0.1.0 - 2018-07-04

This is the first release, with following bug patterns:

* Slf4jLoggerShouldBePrivate
* Slf4jLoggerShouldBeNonStatic
* Slf4jIllegalPassedClass
* Slf4jPlaceholderMismatch
* Slf4jLoggerShouldBeFinal
