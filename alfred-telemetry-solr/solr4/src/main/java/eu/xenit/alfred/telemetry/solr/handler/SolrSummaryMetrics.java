package eu.xenit.alfred.telemetry.solr.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map.Entry;
import org.alfresco.service.cmr.repository.datatype.Duration;
import org.alfresco.solr.AlfrescoCoreAdminHandler;
import org.alfresco.solr.SolrInformationServer;
import org.alfresco.solr.TrackerState;
import org.alfresco.solr.tracker.AclTracker;
import org.alfresco.solr.tracker.MetadataTracker;
import org.alfresco.solr.tracker.TrackerRegistry;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolrSummaryMetrics {

    AlfrescoCoreAdminHandler coreAdminHandler;
    SolrInformationServer server;
    String coreName;

    boolean enableCoreStats = true;
    boolean enableFTSMetrics = true;
    boolean enableTrackerMetrics = true;

    ArrayList fieldsToMonitor = new ArrayList(Arrays.asList(
            "Alfresco Acls in Index",
            "Alfresco Nodes in Index",
            "Alfresco Transactions in Index",
            "Alfresco Acl Transactions in Index",
            "Alfresco Stated in Index",
            "Alfresco Unindexed Nodes",
            "Alfresco Error Nodes in Index"));

    Logger logger = LoggerFactory.getLogger(SolrSummaryMetrics.class);


    public SolrSummaryMetrics(boolean enableCoreStats, boolean enableFTSMetrics, boolean enableTrackerMetrics) {
        this.enableCoreStats = enableCoreStats;
        this.enableFTSMetrics = enableFTSMetrics;
        this.enableTrackerMetrics = enableTrackerMetrics;
    }


    public void getSummaryMetrics(SolrQueryRequest req, SolrQueryResponse rsp) {
        coreAdminHandler = (AlfrescoCoreAdminHandler) (req.getCore().getCoreDescriptor().getCoreContainer()
                .getMultiCoreHandler());
        coreName = req.getCore().getName();
        server = (SolrInformationServer) coreAdminHandler.getInformationServers().get(coreName);

        if (enableCoreStats) {
            getCoreStats(req, rsp);
        }
        if (enableFTSMetrics) {
            getFTSMetrics(req, rsp);
        }
        if (enableTrackerMetrics) {
            getTrackerMetrics(req, rsp);
        }
    }

    private void getTrackerMetrics(SolrQueryRequest req, SolrQueryResponse rsp) {
        TrackerRegistry trackerRegistry = coreAdminHandler.getTrackerRegistry();

        if(trackerRegistry==null) {
            logger.info("There is no tracker registry yet, solr is not yet tracking");
            return;
        }

        MetadataTracker metaTrkr = trackerRegistry.getTrackerForCore(coreName, MetadataTracker.class);
        TrackerState metadataTrkrState = metaTrkr.getTrackerState();
        long lastIndexedTxId = metadataTrkrState.getLastIndexedTxId();
        long lastTxCommitTimeOnServer = metadataTrkrState.getLastTxCommitTimeOnServer();
        long lastTxIdOnServer = metadataTrkrState.getLastTxIdOnServer();
        long lastIndexTxCommitTime = metadataTrkrState.getLastIndexedTxCommitTime();
        long transactionsToDo = lastTxIdOnServer - lastIndexedTxId;
        Date lastIndexTxCommitDate = new Date(lastIndexTxCommitTime);
        Date lastTxOnServerDate = new Date(lastTxCommitTimeOnServer);
        if (transactionsToDo < 0) {
            transactionsToDo = 0;
        }

        AclTracker aclTrkr = trackerRegistry.getTrackerForCore(coreName, AclTracker.class);
        TrackerState aclTrkrState = aclTrkr.getTrackerState();
        long lastIndexChangeSetCommitTime = aclTrkrState.getLastIndexedChangeSetCommitTime();
        long lastIndexedChangeSetId = aclTrkrState.getLastIndexedChangeSetId();
        long lastChangeSetCommitTimeOnServer = aclTrkrState.getLastChangeSetCommitTimeOnServer();
        long lastChangeSetIdOnServer = aclTrkrState.getLastChangeSetIdOnServer();
        Date lastIndexChangeSetCommitDate = new Date(lastIndexChangeSetCommitTime);
        Date lastChangeSetOnServerDate = new Date(lastChangeSetCommitTimeOnServer);
        long changeSetsToDo = lastChangeSetIdOnServer - lastIndexedChangeSetId;
        if (changeSetsToDo < 0) {
            changeSetsToDo = 0;
        }

        Duration txLag = new Duration(lastIndexTxCommitDate, lastTxOnServerDate);
        if (lastIndexTxCommitDate.compareTo(lastTxOnServerDate) > 0) {
            txLag = new Duration();
        }
        long txLagSeconds = (lastTxCommitTimeOnServer - lastIndexTxCommitTime) / 1000;
        if (txLagSeconds < 0) {
            txLagSeconds = 0;
        }

        Duration changeSetLag = new Duration(lastIndexChangeSetCommitDate, lastChangeSetOnServerDate);
        if (lastIndexChangeSetCommitDate.compareTo(lastChangeSetOnServerDate) > 0) {
            changeSetLag = new Duration();
        }
        long changeSetLagSeconds = (lastChangeSetCommitTimeOnServer - lastIndexChangeSetCommitTime) / 1000;
        if (changeSetLagSeconds < 0) {
            changeSetLagSeconds = 0;
        }

        String resp = "# HELP alfresco_summarystring various metrics from Alfresco Solr SUMMARY screen.";
        //rsp.add(resp, "");
        resp = String
                .format("alfresco_summarystring{core=\"%s\",feature=\"Approx transactions remaining\"}", coreName);
        rsp.add(resp, transactionsToDo);
        resp = String.format("alfresco_summarystring{core=\"%s\",feature=\"TX lag\"}", coreName);
        rsp.add(resp, txLagSeconds);
        resp = String.format("alfresco_summarystring{core=\"%s\",feature=\"Last Index TX Commit Time\"}", coreName);
        rsp.add(resp, lastIndexTxCommitTime);

        resp = String.format("alfresco_summarystring{core=\"%s\",feature=\"Approx change sets remaining\"}", coreName);
        rsp.add(resp, changeSetsToDo);
        resp = String.format("alfresco_summarystring{core=\"%s\",feature=\"Change Set Lag\"}", coreName);
        rsp.add(resp, changeSetLagSeconds);
        resp = String.format("alfresco_summarystring{core=\"%s\",feature=\"Last Index Change Set Commit Time\"}", coreName);
        rsp.add(resp, lastIndexChangeSetCommitTime);
    }

    private void getFTSMetrics(SolrQueryRequest req, SolrQueryResponse rsp) {
        if (server == null) {
            return;
        }
        String resp = "# HELP alfresco_summarystring various metrics from Alfresco Solr SUMMARY screen.";
        //rsp.add(resp, "");
        NamedList<Object> report = new NamedList();
        // FTS
        server.addFTSStatusCounts(report);
        for (Entry fts : report) {
            resp = String
                    .format("alfresco_summarystring{core=\"%s\",feature=\"%s\"}", req.getCore().getName(), fts.getKey());
            rsp.add(resp, fts.getValue());
        }
    }

    private void getCoreStats(SolrQueryRequest req, SolrQueryResponse rsp) {
        // Core stats
        if (server == null) {
            return;
        }
        Iterable<Entry<String, Object>> stats = null;
        try {
            stats = server.getCoreStats();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        String resp = "# HELP alfresco_summarystring various metrics from Alfresco Solr SUMMARY screen.";
        //rsp.add(resp, "");
        for (Entry<String, Object> stat : stats) {
            if (fieldsToMonitor.contains(stat.getKey())) {
                resp = String
                        .format("alfresco_summarystring{core=\"%s\",feature=\"%s\"}", req.getCore().getName(), stat.getKey());
                rsp.add(resp, stat.getValue());
            }
        }
    }
}
