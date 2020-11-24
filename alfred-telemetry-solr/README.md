# Introduction
Alfred Telemetry Solr integrates Solr and [Micrometer](https://micrometer.io/), an application metrics facade that 
supports numerous monitoring systems.

This Solr extension offers:

* flexibility to setup and define custom metrics very easily.
* a wide range of out of the box instrumentation


# Getting started

At the moment Alfred Telemetry Solr extension auto-configures a CompositeRegistry containing 2 registries:

* a [`PrometheusMeterRegistry`](https://micrometer.io/docs/registry/prometheus) 
* a [`GraphiteMeterRegistry`](https://micrometer.io/docs/registry/graphite)

The extension implements a MicrometerHandler which binds all available metrics to the global registry. For graphite the handler needs to be called once in the beginning, which is done via an init script added to the image.

In order to visualize correctly the output, a DummyResponseWriter is also provided, which simply displays verbatim the output of Prometheus scraping.

The jar file of the module, together with all micrometer dependencies need to be added to solr's classpath and solr cores need to be configured to use the handler and the writer. 

See examples in integration tests.


# Supported monitoring systems

At the moment only Prometheus and Graphite are supported as monitoring systems.

For Graphite the library offered by micrometer conflicts with the library from inside solr6 and therefore replaces that one.

Disabling and configuring the graphite registry can be done via environment variables:

| Variable                                 | Default  |
|------------------------------------------|----------|
| ALFRED_TELEMETRY_EXPORT_GRAPHITE_ENABLED | false    | 
| ALFRED_TELEMETRY_EXPORT_GRAPHITE_HOST | localhost    | 
| ALFRED_TELEMETRY_EXPORT_GRAPHITE_PORT | 2004    | 
| ALFRED_TELEMETRY_EXPORT_GRAPHITE_STEP | 5    | 

# Supported metrics

Enabling / disabling metrics can be done via environment variables:

| Variable                                 | Default  |
|------------------------------------------|----------|
|METRICS_JVM_ENABLED                       | true     |
|METRICS_JVM_GC_ENABLED                    | true     |
|METRICS_JVM_MEMORY_ENABLED                | true     |
|METRICS_JVM_THREADS_ENABLED               | true     |
|METRICS_JVM_CLASSLOADER_ENABLED           | true     |
|METRICS_PROCESS_ENABLED                   | true     |
|METRICS_PROCESS_THREADS_ENABLED           | true     |
|METRICS_PROCESS_MEMORY_ENABLED            | true     |
|METRICS_SYSTEM_ENABLED                    | true     |
|METRICS_SYSTEM_UPTIME_ENABLED             | true     |
|METRICS_SYSTEM_PROCESSOR_ENABLED          | true     |
|METRICS_SYSTEM_FILEDESCRIPTORS_ENABLED    | true     |
|METRICS_SOLR_ENABLED                      | true     |
|METRICS_SOLR_CORESTATS_ENABLED            | true     |
|METRICS_SOLR_FTS_ENABLED                  | true     |
|METRICS_SOLR_TRACKER_ENABLED              | true     |
|METRICS_SOLR_JMX_ENABLED                  | true     |
|METRICS_TOMCAT_ENABLED                    | false    |
|METRICS_TOMCAT_JMX_ENABLED                | true    |

## Jvm metrics

JVM metrics binding provides several jvm metrics. 
At the moment following metrics are included:

* JvmGcMetrics
* JvmMemoryMetrics
* JvmThreadMetrics
* ClassLoaderMetrics

See metrics provided by these modules [here](https://github.com/micrometer-metrics/micrometer/tree/master/micrometer-core/src/main/java/io/micrometer/core/instrument/binder/jvm).

Note: at the moment GC metrics are not working fully.

## System metrics

System metrics binding provides several system metrics.
At the moment following metrics are included:

* UptimeMetrics
* ProcessorMetrics
* FileDescriptorMetrics

See metrics provided by these modules [here](https://github.com/micrometer-metrics/micrometer/tree/master/micrometer-core/src/main/java/io/micrometer/core/instrument/binder/system).

## Process metrics

Process metrics binding provides several process metrics.
At the moment following metrics are included:

* ProcessMemoryMetrics
* ProcessThreadMetrics

See metrics provided by these modules [here](https://github.com/mweirauch/micrometer-jvm-extras/tree/master/src/main/java/io/github/mweirauch/micrometer/jvm/extras).

## Tomcat metrics

Tomcat metrics binding provides several tomcat-related metrics.
At the moment following metrics are included:

* TomcatMetrics
* TomcatBeansMetrics (custom code, implemented because session-related metrics from default implementation did not work)

See metrics provided by TomcatMetrics [here](https://github.com/micrometer-metrics/micrometer/tree/master/micrometer-core/src/main/java/io/micrometer/core/instrument/binder/tomcat).


## Custom metrics

Following custom metrics have been implemented.

### Solr core stats metrics

At the moment following metrics are included, for each core tracked by solr:

| Name                                                       |
| -----------------------------------------------------------|
| alfresco.nodes{core=\<core\>,state="Indexed"}                |
| alfresco.nodes{core=\<core\>,state="Unindexed"}      |
| alfresco.nodes{core=\<core\>,state="Error"}          |
| alfresco.acls{core=\<core\>,state="Indexed"}                  |
| alfresco.states{core=\<core\>}                            |



### Solr FTS metrics

At the moment following metrics are included, for each core tracked by solr:

| Name                                        |
| --------------------------------------------|
| alfresco.fts{core=\<core\>,state="Clean"}   |
| alfresco.fts{core=\<core\>,state="Dirty"}   |
| alfresco.fts{core=\<core\>,state="New"}     |


### Solr tracker metrics

At the moment following metrics are included, for each core tracked by solr:

| Name                                                                            |
| --------------------------------------------------------------------------------|
| alfresco.transactions.nodes{core=\<core\>,state="Indexed"}                                   |    
| alfresco.transactions.acls{core=\<core\>,state="Indexed"}                                    |
| alfresco.transactions.nodes{core=\<core\>,state="Remaining"}               |
| alfresco.transactions.acls{core=\<core\>,state="Remaining"}                |
| alfresco.transactions.nodes.lag{core=\<core\>}                     |
| alfresco.transactions.acls.lag{core=\<core\>}                      |
| alfresco.transactions.nodes.lastIndexCommitTime{core=\<core\>}  |
| alfresco.transactions.acls.lastIndexCommitTime{core=\<core\>}   |

### Solr jmx metrics

At the moment following metrics are included, only for core "alfresco" for \<type\> one of 
"/afts" or "/cmis".

| Name                                                                  |
| ----------------------------------------------------------------------|
| solr.alfresco.handlerStart{type=\<type\>}                                     |
| solr.alfresco.requests{type=\<type\>}                                          |
| solr.alfresco.timeouts{type=\<type\>}                                         |
| solr.alfresco.errors{type=\<type\>}                                            |
| solr.alfresco.clientErrors{type=\<type\>}                                      |
| solr.alfresco.serverErrors{type=\<type\>}                                      |
| solr.alfresco.75thPcRequestTime{type=\<type\>}                                 |
| solr.alfresco.95thPcRequestTime{type=\<type\>}                                 |
| solr.alfresco.99thPcRequestTime{type=\<type\>}                                 |
| solr.alfresco.999thPcRequestTime{type=\<type\>}                                |
| solr.alfresco.medianRequestTime{type=\<type\>}                                 |
| solr.alfresco.avgTimePerRequest{type=\<type\>}                                 |
| solr.alfresco.totalTime{type=\<type\>}                                         |
| solr.alfresco.5minRateRequestsPerSecond{type=\<type\>}                         |
| solr.alfresco.15minRateRequestsPerSecond{type=\<type\>}                        |
| solr.alfresco.avgRequestsPerSecond{type=\<type\>}                              |
| solr.alfresco.warmupTime{type="searcher"}                                    |
| solr.alfresco.deletedDocs{type="searcher"}                                    |
| solr.alfresco.maxDoc{type="searcher"}                                         |
| solr.alfresco.numDocs{type="searcher"}                                        |
| solr.alfresco.indexVersion{type="searcher"}                                   |

### Tomcat jmx metrics

At the moment following metrics are included:

| Name                                                                  |
| ----------------------------------------------------------------------|
| Catalina.activeSessions{type="Manager"}                                    |
| Catalina.expiredSessions{type="Manager"}                                      |
| Catalina.maxActiveSessions{type="Manager"}                                    |
| Catalina.rejectedSessions{type="Manager"}                                     |
| Catalina.duplicates{type="Manager"}                                           |
| Catalina.maxActive{type="Manager"}                                            |
| Catalina.sessionCounter{type="Manager"}                                       |
| Catalina.sessionIdLength{type="Manager"}                                      |
| Catalina.maxInactiveInterval{type="Manager"}                                  |
| Catalina.processExpiresFrequency{type="Manager"}                              |
| Catalina.processingTime{type="Manager"}                                       |
| Catalina.sessionAverageAliveTime{type="Manager"}                              |
| Catalina.sessionMaxAliveTime{type="Manager"}                                  |
| Catalina.sessionCreateRate{type="Manager"}                                    |
| Catalina.sessionExpireRate{type="Manager"}                                    |



## Common tags
Common tag used for all metrics from solr extension is application=solr. 
This was done to be compatible with the public grafana dashboards.  
   

# How to build 

    ./gradlew alfred-telemetry-solr:solr4:bDI

    ./gradlew alfred-telemetry-solr:solr6:bDI
    
Note: the jar built for each version of solr contains also necessary runtime dependencies.

# How to deploy

**No docker** 

Copy alfred-telemetry-solr/<solrFlavor>/build/libs/<solrFlavor>_<version>.jar into solr's instance lib folder (solr4/lib or alfresco-search-services/solrhome/lib/).

Add to solrconfig.xml for each cores the RequestHandler:

      <lib dir="lib/" regex=".*\.jar" />
      <requestHandler name="/metrics" class="eu.xenit.alfred.telemetry.solr.handler.MicrometerHandler" />
      
and the ResponseWriter:

      <lib dir="lib/" regex=".*\.jar" />
      <queryResponseWriter name="dummy" class="eu.xenit.alfred.telemetry.solr.writer.DummyResponseWriter"/>
      
      
Restart solr.

**Docker**

Following docker images are created following the steps above:

    hub.xenit.eu/alfred-telemetry/solr-alfred-telemetry-<solrFlavor>:<version>

# How to check that it is working

**No docker**

Call an url similar to:

    http://localhost:8080/solr4/alfresco/metrics?wt=dummy
    
**Docker**

Start the whole stack with:
    
    ./gradlew integration-tests:alfresco-community-52:solrComposeUp
    
    ./gradlew integration-tests:alfresco-community-61:solrComposeUp
    
Verify in Prometheus (http://localhost:9090/) that targets are up and running. Verify in grafana (http://localhost:3000/) that Solr dashboard has data.

# Integration tests

    ./gradlew integrationTest
