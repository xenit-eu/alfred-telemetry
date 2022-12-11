---
title: Changelog - Alfred Telemetry
date: 12 December 2022
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

## [0.9.0] - 2022-12-01

### Fixed
* Fixes broken Apache Commons dbcp dependencies [#132]
* Fixes broken common tags not added to Alfrescos Prometheus registry [#134]

### Added
* Added support Alfresco 7.1, 7.2 and 7.3

### Removed
* Dropped support for Alfresco 5.x and 6.0 

## [0.8.0] - 2022-01-28

### BREAKING

* Alfred Telemetry declares `micrometer-core` and `micrometer-jvm-extras` as a provided-dependency [#129]
[#129]: https://github.com/xenit-eu/alfred-telemetry/pull/129

### Fixed
* Fixes bug when tracker is explicitly disabled [#125]


## [0.7.2] - 2021-10-07

### Added
* Added Common tags to Alfred Telemetry Solr

### Fixed
* Bug appearing when tracker is explicitly disabled [#125]

[#125]: https://github.com/xenit-eu/alfred-telemetry/pull/125

### Added
* Support more flexible Graphite step duration configuration [#123]
* Add metrics for solr backup [#124]

[#123]: https://github.com/xenit-eu/alfred-telemetry/pull/123
[#124]: https://github.com/xenit-eu/alfred-telemetry/pull/124


## [0.7.1] - 2021-07-12

### Fixed
* Hazelcast cache metrics broken, resulting in a broken Prometheus scrape endpoint [[#116]]
  
[#116]: https://github.com/xenit-eu/alfred-telemetry/pull/116

## [0.7.0] - 2021-07-09

### Added
* Clustering metrics

### Removed
* Legacy Care4alf metrics

### Fixed
* Solr tracking metrics were no longer registered since 0.6.0 [[#113]]

[#113]: https://github.com/xenit-eu/alfred-telemetry/pull/113

## [0.6.0] - 2021-06-30

### Added
* Alfresco 7 support [[#107]]

### Fixed
* Prometheus scraping: max number of requests reached during Alfresco startup [[#104]]

[#107]: https://github.com/xenit-eu/alfred-telemetry/pull/107
[#104]: https://github.com/xenit-eu/alfred-telemetry/pull/104

## [0.5.2] - 2021-05-19

### Added
* Configurable number of maximum prometheus scrape requests [[#70]]

### Changed
* Optimize names and tags of license metrics

[#70]: https://github.com/xenit-eu/alfred-telemetry/pull/70 

## [0.5.1] - 2021-04-29

### Added
* Alfresco license metrics [[#82]]

### Fixed
* NoSuchMethodError: 'java.util.HashMap org.alfresco.repo.index.shard.ShardRegistry.getFlocs()' [[#85]]

[#82]: https://github.com/xenit-eu/alfred-telemetry/pull/82
[#85]: https://github.com/xenit-eu/alfred-telemetry/pull/85

## [0.5.0] - 2021-03-22

### Added
* Alfresco 6.2.1 support [[#20]]
* Status metric - read-only - for alfresco [[#69]]

### Fixed

* Sharded solr monitoring throws exception in Alfresco 6.2
* Fixed and refactored code for solr, so that it works for ASS>=2.0.0. Improved handling of solrconfig.xml for solr6 [[#74]]

[#20]: https://github.com/xenit-eu/alfred-telemetry/pull/20
[#69]: https://github.com/xenit-eu/alfred-telemetry/pull/69
[#74]: https://github.com/xenit-eu/alfred-telemetry/pull/74

## [0.4.0] - 2020-01-12

### Added

* Jetty metrics for alfred-telemetry-solr [[#61]]

[#61]: https://github.com/xenit-eu/alfred-telemetry/pull/61

## [0.3.1] - 2020-12-11

### Fixed

* Rename solr projects and embed its dependencies [[#54]]
* Fix publication to maven central [[#55]]

[#54]: https://github.com/xenit-eu/alfred-telemetry/pull/54
[#55]: https://github.com/xenit-eu/alfred-telemetry/pull/55


## [0.3.0] - 2020-12-10

*This release was not published due to errors during the publication process*

### Added

* new module: `alfred-telemetry-solr` provides metrics for alfresco-solr4 and alfresco-solr6 [[#27][#40][#43][#44][#46][#47]]
* Added metrics tracker for maxNodeId, last TX ID, last TX timestamp, last changeset ID, last changeset timestamp [[#42]]

[#40]: https://github.com/xenit-eu/alfred-telemetry/pull/40
[#42]: https://github.com/xenit-eu/alfred-telemetry/pull/42
[#43]: https://github.com/xenit-eu/alfred-telemetry/pull/43
[#44]: https://github.com/xenit-eu/alfred-telemetry/pull/44
[#46]: https://github.com/xenit-eu/alfred-telemetry/pull/46
[#47]: https://github.com/xenit-eu/alfred-telemetry/pull/47

## [0.2.0] - 2020-10-29

### Added 

* Beans of type `MeterRegistryCustomizer` allow customization of a `MeterRegistry` before registration [[#10]]
* Added metrics for Solr tracking and Solr sharding [[#22]]
* Included Grafana dashboard in the test setup [[#13]]

### Fixed

* Alfresco 6.1 fails to start if the out-of-the-box metrics are disabled [[#15]]

[#10]: https://github.com/xenit-eu/alfred-telemetry/issues/10
[#13]: https://github.com/xenit-eu/alfred-telemetry/issues/13
[#15]: https://github.com/xenit-eu/alfred-telemetry/issues/15
[#22]: https://github.com/xenit-eu/alfred-telemetry/issues/22

## [0.1.1] - 2019-08-13

### Fixed

* MeterFilters are applied after metrics are possibly already exposed to the global MeterRegistry [[#6]]

### Changed
* Changed default logging level to INFO [[#4]]
* Disable Cache metrics by default [[#7]][[#9]]

[#4]: https://github.com/xenit-eu/alfred-telemetry/pull/4
[#6]: https://github.com/xenit-eu/alfred-telemetry/issues/6
[#7]: https://github.com/xenit-eu/alfred-telemetry/issues/7
[#9]: https://github.com/xenit-eu/alfred-telemetry/issues/9

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
