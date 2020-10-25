# Solr Prometheus endpoint

This subproject creates the artifacts and the docker images needed for exposing Alfresco-Solr metrics in Prometheus format.

It is implemented for 2 flavors of Solr: solr4 and solr6 (solr libraries changed in some respects).

There are 4 kinds of metrics exposed:

* core stats: information about number of nodes, transactions, acls, change set transactions, errors, states, unindexed nodes
* FTS metrics: information about full text search
* tracker metrics: information about last transaction / change set indexed, lag, transactions / change sets remaining
* jmx metrics: jvm-related metrics (memory, load, GC), Catalina metrics (thread pool, sessions, requests info) for solr4 and solr specific metrics

They can be switch on/off with parameters added to the url:

* enableCoreStats
* enableFTSMetrics
* enableTrackerMetrics
* enableJmxMetrics
    * enableJmxMetricsOS
    * enableJmxMetricsMemory
    * enableJmxMetricsClassLoading
    * enableJmxMetricsGC
    * enableJmxMetricsThreading
    * enableJmxMetricsThreadPool
    * enableJmxMetricsRequests
    * enableJmxMetricsSessions
    * enableJmxMetricsSolr

## How to build

    ./gradlew alfred-telemetry-solr:solr4:bDI

    ./gradlew alfred-telemetry-solr:solr6:bDI

## How to deploy

**No docker** 

Copy build/libs/solr-prometheus-exporter.jar into solr's instance lib folder (solr4/lib or alfresco-search-services/solrhome/lib/).

Add to solrconfig.xml for each cores the RequestHandler:

      <lib dir="lib/" regex=".*\.jar" />
      <requestHandler name="/prometheus" class="eu.xenit.solr.handler.PrometheusSummaryHandler" />
      
and the ResponseWriter:

      <lib dir="lib/" regex=".*\.jar" />
      <queryResponseWriter name="prometheus" class="eu.xenit.solr.writer.PrometheusResponseWriter"/>
      
      
Restart solr.

**Docker**

There are 2 docker images created which do the steps above (replace tag with version from main build.gradle)

    hub.xenit.eu/alfred-telemetry/solr-alfred-telemetry-solr4:0.1.2-SNAPSHOT
    
    hub.xenit.eu/alfred-telemetry/solr-alfred-telemetry-solr6:0.1.2-SNAPSHOT

## How to check that it is working

**No docker**

Call an url similar to:

    http://localhost:8080/solr4/alfresco/prometheus?wt=prometheus
    
Each core has its own url.
    
In order to disable some of the metrics:

    http://localhost:8080/solr4/alfresco/prometheus?wt=prometheus&enableFTSMetrics=false
 
**Docker**

Start the whole stack with:
    
    ./gradlew integration-tests:alfresco-community-52:solrComposeUp
    
    ./gradlew integration-tests:alfresco-community-61:solrComposeUp
    
Verify in Prometheus (http://localhost:9090/) that targets are up and running.

## Integration tests

    ./gradlew solrIntegrationTest