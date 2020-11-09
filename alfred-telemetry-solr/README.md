# Introduction
Alfred Telemetry Solr integrates Solr and [Micrometer](https://micrometer.io/), an application metrics facade that 
supports numerous monitoring systems.

> To learn more about Micrometer’s capabilities, please refer to its [reference documentation](https://micrometer.io/docs)
> , in particular the [concepts section](https://micrometer.io/docs/concepts).

This Solr extension offers:

* the flexibility to setup and define custom metrics very easily.
* a wide range of out of the box instrumentation


# Getting started

At the moment Alfred Telemetry Solr extension auto-configures a [`PrometheusMeterRegistry`](https://micrometer.io/docs/registry/prometheus) 
The extension implements a MicrometerHandler which binds all available metrics to the Prometheus registry. 
In order to visualize correctly the output, a DummyResponseWriter is also provided, which simply displays verbatim the output of scraping.
The jar file of the module, together with all micrometer dependencies need to be added to solr's classpath and solr core need to be configured to use the handler and the writer. See examples in integration tests.


# Supported monitoring systems

At the moment only Prometheus is supported as a monitoring system.

# Supported metrics

At the moment it is not possible to configure which metrics should be included.

## Jvm metrics

The JVM metrics binding will provide several jvm metrics. 
At the moment following metrics are included:

* JvmGcMetrics
* JvmMemoryMetrics
* JvmThreadMetrics
* ClassLoaderMetrics

See metrics provided by these modules [here](https://github.com/micrometer-metrics/micrometer/tree/master/micrometer-core/src/main/java/io/micrometer/core/instrument/binder/jvm).

Note: at the moment GC metrics are not working properly.

## System metrics

The system metrics binding will provide several system metrics.
At the moment following metrics are included:

* UptimeMetrics
* ProcessorMetrics
* FileDescriptorMetrics

See metrics provided by these modules [here](https://github.com/micrometer-metrics/micrometer/tree/master/micrometer-core/src/main/java/io/micrometer/core/instrument/binder/system).

## Process metrics

The process metrics binding will provide several process metrics.
At the moment following metrics are included:

* ProcessMemoryMetrics
* ProcessThreadMetrics

See metrics provided by these modules [here](https://github.com/mweirauch/micrometer-jvm-extras/tree/master/src/main/java/io/github/mweirauch/micrometer/jvm/extras).

## Custom metrics

Following custom metrics have been implemented.

### Solr core stats metrics

At the moment following metrics are included, for each core tracked by solr:

| Name                                                                   |
| -----------------------------------------------------------------------|
| alfresco_nodes{core=<core>,feature="Alfresco Nodes in Index"}          |
| alfresco_nodes{core=<core>,feature="Alfresco Transactions in Index"}   |
| alfresco_nodes{core=<core>,feature="Alfresco Stated in Index"}         |
| alfresco_nodes{core=<core>,feature="Alfresco Unindexed Nodes"}         |
| alfresco_nodes{core=<core>,feature="Alfresco Error Nodes in Index"}    |
| alfresco_acls{core=<core>,feature="Alfresco Acls in Index"}            |
| alfresco_acls{core=<core>,feature="Alfresco Acl Transactions in Index"}|

### Solr FTS metrics

At the moment following metrics are included, for each core tracked by solr:

| Name                                                                  |
| ----------------------------------------------------------------------|
| alfresco_fts{core=<core>,feature="Node count with FTSStatus Clean"}   |
| alfresco_fts{core=<core>,feature="Node count with FTSStatus Dirty"}   |
| alfresco_fts{core=<core>,feature="Node count with FTSStatus New"}     |


### Solr tracker metrics

At the moment following metrics are included, for each core tracked by solr:

| Name                                                                  |
| ----------------------------------------------------------------------|
| alfresco_nodes{core=<core>,feature="Approx transactions remaining"}   |
| alfresco_nodes{core=<core>,feature="TX lag"}                          |
| alfresco_nodes{core=<core>,feature="Last Index TX Commit Time"}       |
| alfresco_acls{core=<core>,feature="Approx change sets remaining"}     |
| alfresco_acls{core=<core>,feature="Change Set Lag"}                   |
| alfresco_acls{core=<core>,feature="Last Index Change Set Commit Time"}|

### Solr jmx metrics

At the moment following metrics are included, only for core "alfresco":

| Name                                                                  |
| ----------------------------------------------------------------------|
| solr_alfresco__afts_handlerStart                                      |
| solr_alfresco__afts_requests                                          |
| solr_alfresco__afts_timeouts                                          |
| solr_alfresco__afts_errors                                            |
| solr_alfresco__afts_clientErrors                                      |
| solr_alfresco__afts_serverErrors                                      |
| solr_alfresco__afts_75thPcRequestTime                                 |
| solr_alfresco__afts_95thPcRequestTime                                 |
| solr_alfresco__afts_99thPcRequestTime                                 |
| solr_alfresco__afts_999thPcRequestTime                                |
| solr_alfresco__afts_medianRequestTime                                 |
| solr_alfresco__afts_avgTimePerRequest                                 |
| solr_alfresco__afts_totalTime                                         |
| solr_alfresco__afts_5minRateRequestsPerSecond                         |
| solr_alfresco__afts_15minRateRequestsPerSecond                        |
| solr_alfresco__afts_avgRequestsPerSecond                              |
| solr_alfresco__cmis_handlerStart                                      |
| solr_alfresco__cmis_requests                                          |
| solr_alfresco__cmis_timeouts                                          |
| solr_alfresco__cmis_errors                                            |
| solr_alfresco__cmis_clientErrors                                      |
| solr_alfresco__cmis_serverErrors                                      |
| solr_alfresco__cmis_75thPcRequestTime                                 |
| solr_alfresco__cmis_95thPcRequestTime                                 |
| solr_alfresco__cmis_999thPcRequestTime                                |
| solr_alfresco__cmis_medianRequestTime                                 |
| solr_alfresco__afts_avgTimePerRequest                                 |
| solr_alfresco__cmis_totalTime                                         |
| solr_alfresco__cmis_5minRateRequestsPerSecond                         |
| solr_alfresco__cmis_15minRateRequestsPerSecond                        |
| solr_alfresco__cmis_avgRequestsPerSecond                              |
| solr_alfresco_searcher_warmupTime                                     |
| solr_alfresco_searcher_deletedDocs                                    |
| solr_alfresco_searcher_maxDoc                                         |
| solr_alfresco_searcher_numDocs                                        |
| solr_alfresco_searcher_indexVersion                                   |


## Common tags
Common tag used for all metrics from solr extension is application=solr. 
This was done mainly to be compatible with the Micrometer dashboard.  
   

# How to build 

    ./gradlew alfred-telemetry-solr:solr4:bDI

    ./gradlew alfred-telemetry-solr:solr6:bDI

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
