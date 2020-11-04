package eu.xenit.alfred.telemetry.solr.handler;

import org.alfresco.solr.AlfrescoCoreAdminHandler;
import org.alfresco.solr.SolrInformationServer;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrometheusSummaryHandler extends RequestHandlerBase {

    boolean enableSolrSummaryMetrics = true;
    boolean enableCoreStats = true;
    boolean enableJmxMetrics = true;
    boolean enableFTSMetrics = true;
    boolean enableTrackerMetrics = true;

    boolean enableJmxMetricsClassLoading = true;
    boolean enableJmxMetricsGC = true;
    boolean enableJmxMetricsMemory = true;
    boolean enableJmxMetricsMemoryPoolCMSOldGen = true;
    boolean enableJmxMetricsMemoryPoolParEdenSpace = true;
    boolean enableJmxMetricsMemoryPoolParSurvivorSpace = true;
    boolean enableJmxMetricsOS = true;
    boolean enableJmxMetricsRuntime = true;
    boolean enableJmxMetricsThreading = true;

    boolean enableJmxMetricsThreadPool = true;
    boolean enableJmxMetricsRequests = true;
    boolean enableJmxMetricsSessions = true;

    boolean enableJmxMetricsSolr = true;

    Logger logger = LoggerFactory.getLogger(PrometheusSummaryHandler.class);


    @Override
    public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
        if (req.getOriginalParams().getParams("enableSolrSummaryMetrics") != null) {
            enableSolrSummaryMetrics = req.getOriginalParams().getBool("enableSolrSummaryMetrics");
        }
        if (req.getOriginalParams().getParams("enableCoreStats") != null) {
            enableCoreStats = req.getOriginalParams().getBool("enableCoreStats");
        }
        if (req.getOriginalParams().getParams("enableFTSMetrics") != null) {
            enableFTSMetrics = req.getOriginalParams().getBool("enableFTSMetrics");
        }
        if (req.getOriginalParams().getParams("enableTrackerMetrics") != null) {
            enableTrackerMetrics = req.getOriginalParams().getBool("enableTrackerMetrics");
        }
        if (req.getOriginalParams().getParams("enableJmxMetrics") != null) {
            enableJmxMetrics = req.getOriginalParams().getBool("enableJmxMetrics");
        }
        if (req.getOriginalParams().getParams("enableJmxMetricsClassLoading") != null) {
            enableJmxMetricsClassLoading = req.getOriginalParams().getBool("enableJmxMetricsClassLoading");
        }
        if (req.getOriginalParams().getParams("enableJmxMetricsGC") != null) {
            enableJmxMetricsGC = req.getOriginalParams().getBool("enableJmxMetricsGC");
        }
        if (req.getOriginalParams().getParams("enableJmxMetricsMemory") != null) {
            enableJmxMetricsMemory = req.getOriginalParams().getBool("enableJmxMetricsMemory");
        }
        if (req.getOriginalParams().getParams("enableJmxMetricsMemoryPoolCMSOldGen") != null) {
            enableJmxMetricsMemoryPoolCMSOldGen = req.getOriginalParams().getBool("enableJmxMetricsMemoryPoolCMSOldGen");
        }
        if (req.getOriginalParams().getParams("enableJmxMetricsMemoryPoolParEdenSpace") != null) {
            enableJmxMetricsMemoryPoolParEdenSpace = req.getOriginalParams().getBool("enableJmxMetricsMemoryPoolParEdenSpace");
        }
        if (req.getOriginalParams().getParams("enableJmxMetricsMemoryPoolParSurvivorSpace") != null) {
            enableJmxMetricsMemoryPoolParSurvivorSpace = req.getOriginalParams().getBool("enableJmxMetricsMemoryPoolParSurvivorSpaces");
        }
        if (req.getOriginalParams().getParams("enableJmxMetricsOS") != null) {
            enableJmxMetricsOS = req.getOriginalParams().getBool("enableJmxMetricsOS");
        }
        if (req.getOriginalParams().getParams("enableJmxMetricsRuntime") != null) {
            enableJmxMetricsRuntime = req.getOriginalParams().getBool("enableJmxMetricsRuntime");
        }
        if (req.getOriginalParams().getParams("enableJmxMetricsThreading") != null) {
            enableJmxMetricsThreading = req.getOriginalParams().getBool("enableJmxMetricsThreading");
        }
        if (req.getOriginalParams().getParams("enableJmxMetricsThreadPool") != null) {
            enableJmxMetricsThreadPool = req.getOriginalParams().getBool("enableJmxMetricsThreadPool");
        }
        if (req.getOriginalParams().getParams("enableJmxMetricsRequests") != null) {
            enableJmxMetricsRequests = req.getOriginalParams().getBool("enableJmxMetricsRequests");
        }
        if (req.getOriginalParams().getParams("enableJmxMetricsSessions") != null) {
            enableJmxMetricsSessions = req.getOriginalParams().getBool("enableJmxMetricsSessions");
        }
        if (req.getOriginalParams().getParams("enableJmxMetricsSolr") != null) {
            enableJmxMetricsSolr = req.getOriginalParams().getBool("enableJmxMetricsSolr");
        }


        if (enableSolrSummaryMetrics) {
            SolrSummaryMetrics summaryMetrics = new SolrSummaryMetrics(enableCoreStats,enableFTSMetrics,enableTrackerMetrics);
            summaryMetrics.getSummaryMetrics(req, rsp);
        }
        if (enableJmxMetrics) {
            JmxMetrics jmxMetrics = new JmxMetrics(enableJmxMetricsClassLoading,enableJmxMetricsGC,enableJmxMetricsMemory,enableJmxMetricsMemoryPoolCMSOldGen,enableJmxMetricsMemoryPoolParEdenSpace,enableJmxMetricsMemoryPoolParSurvivorSpace,enableJmxMetricsOS,enableJmxMetricsRuntime,enableJmxMetricsThreading,enableJmxMetricsThreadPool,enableJmxMetricsSessions,enableJmxMetricsRequests,enableJmxMetricsSolr);
            jmxMetrics.getJmxMetrics(req, rsp);
        }
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getSource() {
        return null;
    }
}
