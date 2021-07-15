# Introduction
Alfred Telemetry integrates Alfresco and [Micrometer](https://micrometer.io/), an application metrics facade that 
supports numerous monitoring systems.

> To learn more about Micrometer’s capabilities, please refer to its [reference documentation](https://micrometer.io/docs)
> , in particular the [concepts section](https://micrometer.io/docs/concepts).

This Alfresco Platform extension offers:

* the flexibility to setup and define custom metrics very easily.
* a wide range of out of the box instrumentation


# Getting started

Alfred Telemetry auto-configures a [composite `MeterRegistry`](https://micrometer.io/docs/concepts#_composite_registries) 
and adds a registry to the composite for each of the supported implementations that it finds on the classpath. 
Having a dependency on `micrometer-registry-{system}` in your runtime classpath is enough for 
Alfred Telemetry to configure the registry.

> Alfred Telemetry uses [Micrometers global static registry](https://micrometer.io/docs/concepts#_global_registry) 
> as composite `MeterRegistry`. This allows consumers of the library to expose metrics through the static 
> Micrometer API. (E.g. `Metrics.counter(...)`)

Most registries share common features. For instance, you can disable a particular registry even if the Micrometer 
registry implementation is on the classpath. For instance, to disable Jmx:

`alfred.telemetry.export.jmx.enabled=false`

With Alfred Telemetry installed as an extension in the Alfresco Platform, you can inject `MeterRegistry` in your 
components and register metrics:

```xml
<bean id="alfred-telemetry.SampleBean" class="eu.xenit.alfred.telemetry.manual.SampleBean">
    <constructor-arg ref="meterRegistry"/>
</bean>
```

```java
public class SampleBean {

    private final Counter counter;

    public SampleBean(MeterRegistry registry) {
        this.counter = registry.counter("received.messages");
    }

    public void handleMessage(String message) {
        this.counter.increment();
        // handle message implementation
    }

}
```

Alfred Telemetry also configures built-in instrumentation (i.e. `MeterBinder` implementations) that 
you can control via configuration.


# Supported monitoring systems

As mentioned before, Alfred Telemetry adds all `MeterRegistry` implementations that it finds on the classpath 
to the global registry. Alfred Telemetry only supports monitoring systems included in this section.

> Metrics will not be exposed to the monitoring system if the version of the additional Micrometer module is 
> incompatible with the version of the Micrometer core module.  
> If this happens, a warning will be printed in the Alfresco logs:
>  `micrometer-registry-graphite version ('1.1.0') is incompatible with the micrometer-core version ('1.0.6') and will 
> not be registered`

## Graphite

**Control Property**: `alfred.telemetry.export.graphite.enabled`

By default, metrics are exported to [Graphite](https://micrometer.io/docs/registry/graphite) running on your 
local machine. The [Graphite server](https://graphiteapp.org/) host and port to use can be provided using:

```properties
alfred.telemetry.export.graphite.host=localhost
alfred.telemetry.export.graphite.port=2004
```

Additional Graphite configuration:

```properties
# The interval at which metrics are sent to Graphite. The duration can be provided in a simple format (5s, 1m, ...)
# or in an ISO8601 compliant format (PT5S, PT1M, ... see java.time.Duration#parse(CharSequence))
alfred.telemetry.export.graphite.step=5s

# Applies the tag value of a set of common tags as a prefix
alfred.telemetry.export.graphite.tags-as-prefix=application,host
```

If you want Graphite support in your Alfresco docker image, you'll also need to add the following
in your build.gradle:
```groovy
alfrescoSM "io.micrometer:micrometer-registry-graphite:${version}"
```
## JMX

**Control Property**: `alfred.telemetry.export.jmx.enabled`

## Prometheus

**Control Property**: `alfred.telemetry.export.prometheus.enabled`

[Prometheus expects](https://micrometer.io/docs/registry/prometheus) to scrape or poll individual app instances for 
metrics. Alfred Telemetry provides an endpoint available at `/alfresco/s/alfred/telemetry/prometheus` 
to present a [Prometheus scrape](https://prometheus.io/) with the appropriate format.

> If the micrometer-registry-prometheus module is not available on the classpath, requests to the endpoint 
> will result in a HTTP Status code 404

Additional Prometheus configuration:

```properties
# The maximum number of concurrent requests the Prometheus endpoint should handle. Once the maximum number of 
# requests is being processed, the response for new requests will have status code 503.
alfred.telemetry.export.prometheus.max-requests=1
```

# Supported metrics

## Jvm metrics
The JVM metrics bindings will provide several jvm metrics.

**Control Property**: `alfred.telemetry.binder.jvm.enabled`

Metrics provided  

| Name                                  |
| :------------------------------------ |
| jvm.buffer.count                      |
| jvm.buffer.memory.used                |
| jvm.buffer.total.capacity             |
| jvm.classes.loaded                    |
| jvm.classes.unloaded                  |
| jvm.gc.live.data.size                 |
| jvm.gc.max.data.size                  |
| jvm.gc.memory.allocated               |
| jvm.gc.memory.promoted                |
| jvm.memory.committed                  |
| jvm.memory.max                        |
| jvm.memory.used                       |
| jvm.threads.daemon                    |
| jvm.threads.live                      |
| jvm.threads.peak                      |



## System metrics
There are multiple metrics that can be separately toggled.

### Uptime metrics
The uptime metrics bindings will provide system uptime metrics.

**Control Property**: `alfred.telemetry.binder.uptime.enabled`

Metrics provided  

| Name                                  |
| :------------------------------------ |
| process.uptime                        |
| process.start.time                    |

### Processor metrics
The processor metrics bindings will provide system processor metrics.

**Control Property**: `alfred.telemetry.binder.processor.enabled`

Metrics provided  

| Name                                  |
| :------------------------------------ |
| system.load.average.1m                |
| system.cpu.usage                      |
| system.cpu.count                      |
| process.cpu.usage                     |

### File Descriptor metrics
The file descriptor metrics bindings will provide system file descriptor metrics.

**Control Property**: `alfred.telemetry.binder.files.enabled`

Metrics provided  

| Name                                  |
| :------------------------------------ |
| process.files.open                    |
| process.files.max                     |



## Cache Metrics
The cache metrics bindings will provide metrics for the Alfresco caches. These metrics are **disabled by default**.

**Control Property**: `alfred.telemetry.binder.cache.enabled`

Metrics are currently only available for following cache types:

* `org.alfresco.repo.cache.DefaultSimpleCache`
* `org.alfresco.enterprise.repo.cluster.cache.InvalidatingCache` (Enterprise only)
* `org.alfresco.enterprise.repo.cluster.cache.HazelcastSimpleCache` (Enterprise only)

For cache beans with a type not specified in the above list
(e.g. `org.alfresco.repo.cache.NullCache`, `org.alfresco.repo.cache.TransactionalCache`)
no metrics will be available. 

Metrics provided

| Name                                  | Available tags                                                                 |
| :------------------------------------ | :----------------------------------------------------------------------------- | 
| cache.size                            | cache:[*], type: [DefaultSimpleCache, InvalidatingCache, HazelcastSimpleCache] |
| cache.gets                            | cache:[*], type: [DefaultSimpleCache, InvalidatingCache, HazelcastSimpleCache] |
| cache.puts                            | cache:[*], type: [DefaultSimpleCache, InvalidatingCache, HazelcastSimpleCache] |
| cache.evictions                       | cache:[*], type: [DefaultSimpleCache, InvalidatingCache, HazelcastSimpleCache] |

All cache metrics are tagged with following tags:

* `cache`: the bean ID of the cache
* `type`: the simple class name of the cache



## DataSource Metrics
The data source metrics bindings will provide data source pool metrics.

**Control Property**: `alfred.telemetry.binder.jdbc.enabled`

> The current implementation of this metrics binding expects the `dataSource` bean to be of type 
> `org.apache.commons.dbcp.BasicDataSource`. 

Metrics provided

| Name                                  |  Available tags           |
| :------------------------------------ | :------------------------ |
| jdbc.connections.count                | status:[active, idle]     |
| jdbc.connections.usage                |                           |
| jdbc.connections.max                  |                           |
| jdbc.connections.min                  |                           |

## Ticket metrics
The ticket metrics bindings will provide ticket metrics.

**Control Property**: `alfred.telemetry.binder.ticket.enabled`

Metrics provided  

| Name                                  | Available tags           |
| :------------------------------------ | :----------------------- |
| users.tickets.count                   | status:[valid, expired]  |


## Status metrics
The status metrics bindings will provide a metric about Alfresco being in read-only mode.

**Control Property**: `alfred.telemetry.binder.alfresco-status.enabled`

Metrics provided

| Name                                  | Available tags           |
| :------------------------------------ | :----------------------- |
| alfresco.status.readonly              |                          |

## License metrics
The license metrics bindings will provide metrics about Alfresco license. Enterprise-only feature.

**Control Property**: `alfred.telemetry.binder.license.enabled`

Metrics provided

| Name                                  | Available tags           | Comment                                    |
| :------------------------------------ | :----------------------  | :----------------------------------------- |
| license.valid                         |                          |                                            |
| license.users.max                      |                          |                                            |
| license.users                         |       status:[current]   | User needs to have logged in at least once |
| license.docs.max                       |                          |                                            |
| license.days                          |       status:[remaining] |                                            |
| license.cluster.enabled               |                          |                                            |
| license.encryption.enabled            |                          |                                            |
| license.heartbeat.enabled             |                          |                                            |



## Alfresco Node metrics

**Control property**: `alfred.telemetry.binder.alfresco-node.enabled`

| Name                                  |
| :------------------------------------ |
| alfresco.node.maxTxnId                |
| alfresco.node.maxTxnCommitTime        |
| alfresco.node.maxNodeId               |
| alfresco.acl.maxChangeSetCommitTime   |
| alfresco.acl.maxChangeSetId           |


## Clustering metrics
The Cluster metrics will provide info about the amount of clusters setup. Enterprise-only feature the metric will not be there on Community.

**Control Property**: `alfred.telemetry.binder.clustering.enabled`

Metrics provided

| Name                                  | Available tags           
| :------------------------------------ | :----------------------  
| repository.cluster.nodes.count        | : clustertype:[member, non-member, offline]                                           |


## Solr metrics


### Solr tracking metrics
**Control Property**: `alfred.telemetry.binder.solr.tracking.enabled`

Metrics provided

| Name                                  |
| :------------------------------------ |
| solr.tracking.maxTxnId                |
| solr.tracking.maxTxnCommitTime        |
| solr.tracking.maxChangeSetId          |
| solr.tracking.maxChangeSetCommitTime  |

### Solr sharding metrics
Solr sharding metrics are only available on Alfresco enterprise versions greater than 6.0.

**Control Property**: 

`alfred.telemetry.binder.solr.sharding.enabled` :Enable solr sharding metrics

`alfred.telemetry.binder.solr.sharding.floc.id.enabled`: This option is enabled by default. If this option is disabled, then it will always use a floc id of 1 for the output metrics folder. On a restart, alfresco always generates a new floc id which can be annoying (for example in Grafana)

| Name                                         | Available tags                                                                              | Values                       |
|:-------------------------------------------|:------------------------------------------------------------------------------------------|:---------------------------|
| solr.sharding.shards                         | floc:[*], storeRef:[workspace_SpacesStore, archive_SpacesStore]                             |                              |
| solr.sharding.shardInstances                 | floc:[*], storeRef:[workspace_SpacesStore, archive_SpacesStore], shard:[*], instanceHost[*] |                              |
| solr.sharding.lastIndexedChangeSetId         | floc:[*], storeRef:[workspace_SpacesStore, archive_SpacesStore], shard:[*], instanceHost[*] |                              |
| solr.sharding.lastIndexedTxId                | floc:[*], storeRef:[workspace_SpacesStore, archive_SpacesStore], shard:[*], instanceHost[*] |                              |
| solr.sharding.instanceMode                   | floc:[*], storeRef:[workspace_SpacesStore, archive_SpacesStore], shard:[*], instanceHost[*] | 0=ACTIVE, 1=PAUSED, 2=SILENT |
| solr.sharding.master                         | floc:[*], storeRef:[workspace_SpacesStore, archive_SpacesStore], shard:[*], instanceHost[*] |                              |
| solr.sharding.lastIndexedChangeSetCommitTime | floc:[*], storeRef:[workspace_SpacesStore, archive_SpacesStore], shard:[*], instanceHost[*] | 0=false, 1=true              |
| solr.sharding.lastIndexedTxCommitTime        | floc:[*], storeRef:[workspace_SpacesStore, archive_SpacesStore], shard:[*], instanceHost[*] |                              |
| solr.sharding.lastUpdated                    | floc:[*], storeRef:[workspace_SpacesStore, archive_SpacesStore], shard:[*], instanceHost[*] |                              |

# Registering custom metrics

## Registering with the 'meterRegistry' bean

To register custom metrics, inject the `meterRegistry` bean into your component, as shown in the following example:

```xml
<bean id="alfred-telemetry.SampleBean" class="eu.xenit.alfred.telemetry.manual.SampleBean">
    <constructor-arg ref="meterRegistry"/>
</bean>
```

```java
public class SampleBean {

    private final Counter counter;

    public SampleBean(MeterRegistry registry) {
        this.counter = registry.counter("received.messages");
    }

    public void handleMessage(String message) {
        this.counter.increment();
        // handle message implementation
    }

}
```

If you find that you repeatedly instrument a suite of metrics across components or applications, you may 
encapsulate this suite in a 
[`MeterBinder`](https://github.com/micrometer-metrics/micrometer/blob/master/micrometer-core/src/main/java/io/micrometer/core/instrument/binder/MeterBinder.java) 
implementation. By default, metrics from all `MeterBinder` beans will be automatically bound to the 
global `MeterRegistry`.

## Registering with Micrometers static API

Micrometers static `Metrics` class can also be used as an alternative to register custom metrics.

```java
public class SampleBean {

    private final Counter counter;

    public SampleBean() {
        this.counter = Metrics.counter("received.messages");
    }

    public void handleMessage(String message) {
        this.counter.increment();
        // handle message implementation
    }

}
```


# Metrics endpoint

Alfred Telemetry provides a metrics endpoint that can be used diagnostically to examine the metrics collected in 
Alfresco.

Navigating to `/alfresco/s/alfred/telemetry/metrics` displays a list of available meter names. You can drill 
down to view information about a particular meter by providing its name as a selector, e.g. 
`/alfresco/s/alfred/telemetry/metrics/jvm.memory.max`.

> The name you use here should match the name used in the code, not the name after it has been naming-convention 
> normalized for a monitoring system it is shipped to. In other words, if `jvm.memory.max` appears as 
> `jvm_memory_max` in Prometheus because of its snake case naming convention, you should still use `jvm.memory.max` 
> as the selector when inspecting the meter in the metrics endpoint.

You can also add any number of `tag=KEY:VALUE` query parameters to the end of the URL to dimensionally 
drill down on a meter,  
e.g. `/alfresco/s/alfred/telemetry/metrics/jvm.memory.max?tag=area:nonheap`.

> The reported measurements are the sum of the statistics of all meters matching the meter name and any tags that 
> have been applied. So in the example above, the returned "Value" statistic is the sum of the maximum memory 
> footprints of "Code Cache", "Compressed Class Space", and "Metaspace" areas of the heap. 
> If you just wanted to see the maximum size for the "Metaspace", you could add an additional `tag=id:Metaspace`, 
> i.e. `/alfresco/s/alfred/telemetry/metrics/jvm.memory.max?tag=area:nonheap&tag=id:Metaspace`.


# Customizing individual metrics

If you need to apply customizations to specific Meter instances you can use the 
`io.micrometer.core.instrument.config.MeterFilter` interface. By default, all 
[`MeterFilter`](https://micrometer.io/docs/concepts#_meter_filters) beans will be automatically applied 
to the `Config` of the global `MeterRegistry`.


## Common tags
Common tags are generally used for dimensional drill-down on the operating environment like host, 
instance, region, stack, etc. Commons tags are applied to all meters and can be 
configured in `alfresco-global.prooperties` as shown in the following example:

```properties
alfred.telemetry.tags.region=us-east-1
alfred.telemetry.tags.stack=prod
```

The example above adds `region` and `stack` tags to all meters with a value of `us-east-1` and `prod` respectively.

> The order of common tags is important if you are using Graphite. As the order of common tags cannot be 
> guaranteed using this approach, Graphite users are advised to define a custom `MeterFilter` instead.


# Alfred Telemetry Alfresco integration

Since ACS 6.1, Alfresco contains its own Micrometer metrics integration. This module allows integration between
the Alfred Telemetry and Alfresco metrics. 

With this integration, it is possible to:

* Write metrics exposed by the Alfred Telemetry module to the default Alfresco output
* Write metrics exposed by Alfresco to the outputs registered in the Alfred Telemetry module

## Configuration

An overview of the configurable parameters and their default value. These values can be overwritten in the 
`alfresco-global.properties` file.

* `alfred.telemetry.alfresco-integration.enabled=false`
    Enables integration between Alfred Telemetry and Alfresco Micrometer metrics. If enabled, all Alfresco metrics 
    will automatically be written to the Alfred Telemetry registries.

* `alfred.telemetry.alfresco-integration.use-default-alfresco-registry=false`  
    Since Alfresco 6.1, Alfresco itself provides a Micrometer meter registry. More specifically:
    Alfresco provides a `PrometheusMeterRegistry`. With this property it is possible to indicate that Alfred Telemetry
    should use the registry provided by Alfresco as it's Prometheus registry.  
    The only advantage of using the default Alfresco registry is that metrics registered by Alfred Telemetry will
    be available in the Alfresco scrape endpoint (`/alfresco/s/prometheus`). However this default 
    registry is managed by Alfresco, hence that means no customizations like e.g. common tags are 
    applied to the Prometheus metrics.  
    If the default Alfresco registry is used, the prometheus registry initialized by Alfred Telemetry must be disabled
    (`alfred.telemetry.export.prometheus.enabled=false`). 

## Known limitations
* Alfresco's `ServletMetricsFilter` is not compatible with this module.  
    Staring up Alfresco with the `alfresco-metrics-integration` module and the `ServletMetricsFilter` not disabled, 
    will result in following exception:
    
    ```text
    2019-05-02 15:08:21,829  WARN  [app.servlet.ServletMetricsFilter] [localhost-startStop-1] Could not initialize the application server metrics reporter: io.micrometer.core.instrument.composite.CompositeMeterRegistry cannot be cast to io.micrometer.prometheus.PrometheusMeterRegistry
    java.lang.ClassCastException: io.micrometer.core.instrument.composite.CompositeMeterRegistry cannot be cast to io.micrometer.prometheus.PrometheusMeterRegistry
        at org.alfresco.web.app.servlet.ServletMetricsFilter.initServletMetrics(ServletMetricsFilter.java:129)
    ```
    
    Therefore this module automatically disables the `ServletMetricsFilter` metrics by setting following global 
    property: `metrics.tomcatMetricsReporter.enabled=false`.
  