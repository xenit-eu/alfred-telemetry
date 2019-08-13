---
title: Changelog - Alfred Telemetry
date: 11 July 2019
report: true
colorlinks: true
---
<!--
Changelog for Alfred Telemetry

See http://keepachangelog.com/en as reference
Version template:

## [X.X.X] - yyyy-MM-dd
### Added (for new features)
### Changed (for changes in existing functionality)
### Deprecated (for soon-to-be removed features)
### Removed (for now removed features)
### Fixed (for any bug fixes)
### Security (in case of vulnerabilities)
### YANKED (for reverted functionality in)
 -->
 
# Alfred Telemetry Changelog

## [0.1.0] - 2019-07-11

Initial, early access release including:

- Support for the `Graphite`, `JMX` and `Prometheus` Micrometer `MeterRegistry`, which are automatically registered 
if available on the classpath.
- Provided out of the box metrics: jvm, files, database, process and ticket metrics. 
- Registering of custom metrics via the `meterRegistry`, Micrometers static API or custom `MeterBinder` implementations.
- The possibility to customize meters with custom `MeterFilter` implementations.
- Integration with the out of the box Alfresco metrics included since 6.1.
- Limited support for Care4Alf compatible metrics.
- ...

## [0.1.1] - 2019-08-13

### Fixed
* MeterFilters are applied after metrics are possibly already exposed to the global MeterRegistry

### Changed
* Changed default logging level to INFO
* Disable Cache metrics by default