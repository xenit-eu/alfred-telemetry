# Solr Prometheus endpoint

This project builds a jar file which allows exposing some metrics in Prometheus format.

It exposes 4 kinds of metrics:

* core stats: information about number of nodes, transactions, acls, change set transactions, errors, states, unindexed nodes
* FTS metrics: information about full text search
* tracker metrics: information about last transaction / change set indexed, lag, transactions / change sets remaining
* jmx metrics: jvm-related metrics (memory, load, GC), Catalina metrics (thread pool, sessions, requests info)

## How to build

./gradlew jar

## How to deploy

Copy build/libs/solr-prometheus-exporter.jar into solr's server lib folder (solr4/lib or alfresco-search-services/solr/server/lib/).

Add the to solrconfig.xml for all cores the RequestHandler:

      <lib dir="lib/" regex=".*\.jar" />
      <requestHandler name="/prometheus" class="eu.xenit.solr.handler.PrometheusSummaryHandler" />
      
and the ResponseWriter:

      <lib dir="lib/" regex=".*\.jar" />
      <queryResponseWriter name="prometheus" class="eu.xenit.solr.writer.PrometheusResponseWriter"/>
      
      
Restart solr.

## How to check that it is working

Call an url similar to:

    http://localhost:8080/solr4/alfresco/prometheus?wt=prometheus
    
Each core has its own url.
    
In order to disable some of the metrics:

    http://localhost:8080/solr4/alfresco/prometheus?wt=prometheus&enableFTSMetrics=false
 
Possible parameters:

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