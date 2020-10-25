package eu.xenit.solr.handler;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import org.alfresco.service.cmr.repository.datatype.Duration;
import org.alfresco.solr.AlfrescoCoreAdminHandler;
import org.alfresco.solr.SolrInformationServer;
import org.alfresco.solr.TrackerState;
import org.alfresco.solr.tracker.AclTracker;
import org.alfresco.solr.tracker.MetadataTracker;
import org.alfresco.solr.tracker.TrackerRegistry;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.util.JmxUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrometheusSummaryHandler extends RequestHandlerBase {

    private static final String PROMETHEUS_SEPARATOR = "_";
    AlfrescoCoreAdminHandler coreAdminHandler;
    SolrInformationServer server;
    String coreName;
    boolean enableCoreStats = true;
    boolean enableJmxMetrics = true;
    boolean enableFTSMetrics = true;
    boolean enableTrackerMetrics = true;
    boolean enableJmxMetricsOS = true;
    boolean enableJmxMetricsMemory = true;
    boolean enableJmxMetricsClassLoading = true;
    boolean enableJmxMetricsGC = true;
    boolean enableJmxMetricsThreading = true;
    boolean enableJmxMetricsSolr = true;


    Logger logger = LoggerFactory.getLogger(PrometheusSummaryHandler.class);

    ArrayList fieldsToMonitor = new ArrayList(Arrays.asList(
            "Alfresco Acls in Index",
            "Alfresco Nodes in Index",
            "Alfresco Transactions in Index",
            "Alfresco Acl Transactions in Index",
            "Alfresco Stated in Index",
            "Alfresco Unindexed Nodes",
            "Alfresco Error Nodes in Index"));
    ArrayList<AbstractMap.SimpleEntry> beansToMonitorOS = new ArrayList(Arrays.asList(
            new AbstractMap.SimpleEntry<>("java.lang:type=OperatingSystem",new ArrayList(Arrays.asList("*")))));
    ArrayList<AbstractMap.SimpleEntry> beansToMonitorMemory = new ArrayList(Arrays.asList(
            new AbstractMap.SimpleEntry<>("java.lang:type=Memory",new ArrayList(Arrays.asList("*")))));
    ArrayList<AbstractMap.SimpleEntry> beansToMonitorClassLoading = new ArrayList(Arrays.asList(
            new AbstractMap.SimpleEntry<>("java.lang:type=ClassLoading",new ArrayList(Arrays.asList("*")))));
    ArrayList<AbstractMap.SimpleEntry> beansToMonitorThreading = new ArrayList(Arrays.asList(
            new AbstractMap.SimpleEntry<>("java.lang:type=Threading",new ArrayList(Arrays.asList("*")))));
    ArrayList<AbstractMap.SimpleEntry> beansToMonitorGC = new ArrayList(Arrays.asList(
            new AbstractMap.SimpleEntry<>("java.lang:type=GarbageCollector,name=ConcurrentMarkSweep",new ArrayList(Arrays.asList("*")))));
    ArrayList<AbstractMap.SimpleEntry> beansToMonitorSolr = new ArrayList(Arrays.asList(
            new AbstractMap.SimpleEntry<>("solr/alfresco:type=searcher,id=org.apache.solr.search.SolrIndexSearcher",new ArrayList(Arrays.asList("*"))),
            new AbstractMap.SimpleEntry<>("solr/alfresco:type=/afts,id=org.apache.solr.handler.component.AlfrescoSearchHandler",new ArrayList(Arrays.asList("*"))),
            new AbstractMap.SimpleEntry<>("solr/alfresco:type=/cmis,id=org.apache.solr.handler.component.AlfrescoSearchHandler",new ArrayList(Arrays.asList("*")))));

    @Override
    public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
        if(req.getOriginalParams().getParams("enableCoreStats") != null)
            enableCoreStats = req.getOriginalParams().getBool("enableCoreStats");
        if(req.getOriginalParams().getParams("enableFTSMetrics") != null)
            enableFTSMetrics = req.getOriginalParams().getBool("enableFTSMetrics");
        if(req.getOriginalParams().getParams("enableTrackerMetrics") != null)
            enableTrackerMetrics = req.getOriginalParams().getBool("enableTrackerMetrics");
        if(req.getOriginalParams().getParams("enableJmxMetrics") != null)
            enableJmxMetrics = req.getOriginalParams().getBool("enableJmxMetrics");
        if(req.getOriginalParams().getParams("enableJmxMetricsOS") != null)
            enableJmxMetricsOS = req.getOriginalParams().getBool("enableJmxMetricsOS");
        if(req.getOriginalParams().getParams("enableJmxMetricsMemory") != null)
            enableJmxMetricsMemory = req.getOriginalParams().getBool("enableJmxMetricsMemory");
        if(req.getOriginalParams().getParams("enableJmxMetricsClassLoading") != null)
            enableJmxMetricsClassLoading = req.getOriginalParams().getBool("enableJmxMetricsClassLoading");
        if(req.getOriginalParams().getParams("enableJmxMetricsGC") != null)
            enableJmxMetricsGC = req.getOriginalParams().getBool("enableJmxMetricsGC");
        if(req.getOriginalParams().getParams("enableJmxMetricsThreading") != null)
            enableJmxMetricsThreading = req.getOriginalParams().getBool("enableJmxMetricsThreading");
        if(req.getOriginalParams().getParams("enableJmxMetricsSolr") != null)
            enableJmxMetricsSolr = req.getOriginalParams().getBool("enableJmxMetricsSolr");


        coreAdminHandler = (AlfrescoCoreAdminHandler)(req.getCore().getCoreContainer().getMultiCoreHandler());
        coreName = req.getCore().getName();
        server = (SolrInformationServer) coreAdminHandler.getInformationServers().get(coreName);

        if(enableCoreStats)
            getCoreStats(req, rsp);
        if(enableFTSMetrics)
            getFTSMetrics(req, rsp);
        if(enableTrackerMetrics)
            getTrackerMetrics(req, rsp);
        if(enableJmxMetrics)
            getJmxMetrics(req,rsp);
    }

    private void getJmxMetrics(SolrQueryRequest req, SolrQueryResponse rsp) {
        MBeanServer mbeanServer = JmxUtil.findFirstMBeanServer();
        if(mbeanServer==null) {
            logger.error("No mbeanServer found, jmx metrics will not be activated");
            return;
        }
        if(enableJmxMetricsOS)
            getJmxMetricsPerBeans(mbeanServer,beansToMonitorOS,rsp);
        if(enableJmxMetricsMemory)
            getJmxMetricsPerBeans(mbeanServer,beansToMonitorMemory,rsp);
        if(enableJmxMetricsGC)
            getJmxMetricsPerBeans(mbeanServer,beansToMonitorGC,rsp);
        if(enableJmxMetricsClassLoading)
            getJmxMetricsPerBeans(mbeanServer,beansToMonitorClassLoading,rsp);
        if(enableJmxMetricsThreading)
            getJmxMetricsPerBeans(mbeanServer,beansToMonitorThreading,rsp);
        if(enableJmxMetricsSolr)
            getJmxMetricsPerBeans(mbeanServer,beansToMonitorSolr,rsp);
    }


    private void getJmxMetricsPerBeans(MBeanServer mbeanServer, ArrayList<AbstractMap.SimpleEntry> beansToMonitor, SolrQueryResponse rsp) {
        for(AbstractMap.SimpleEntry beanToMonitor : beansToMonitor) {
            ObjectName objectName = null;
            try {
                objectName = new ObjectName((String) beanToMonitor.getKey());
            } catch (MalformedObjectNameException e) {
                logger.error("Malformed bean name " + beanToMonitor.getKey());
                continue;
            }
            MBeanInfo mBeanInfo = null;
            try {
                mBeanInfo = mbeanServer.getMBeanInfo(objectName);
            } catch (InstanceNotFoundException | IntrospectionException | ReflectionException e) {
                logger.error("Problem with bean " + beanToMonitor.getKey());
                continue;
            }
            for (MBeanAttributeInfo attributeInfo : mBeanInfo.getAttributes()) {
                ArrayList values = (ArrayList) beanToMonitor.getValue();
                if (prometheusAllowedType(attributeInfo.getType()) && ("*".equals(values.get(0)))) {
                    String resp = String.format("%s_%s{%s}", getPrometheusName(objectName), attributeInfo.getName(),
                            getPrometheusLabels(objectName));
                    try {
                        rsp.add(resp, mbeanServer.getAttribute(objectName, attributeInfo.getName()));
                    } catch (MBeanException | AttributeNotFoundException | InstanceNotFoundException | ReflectionException e) {
                        logger.error("Problem with attributes " + attributeInfo + " checked against " + values);
                        continue;
                    }
                }
            }
        }
    }

    private String getPrometheusLabels(ObjectName objectName) {
        String result = "";
        for(String property : objectName.getKeyPropertyList().keySet()) {
            if(!"type".equals(property)) {
                result += getPrometheusEscape(property) + "=" + getPrometheusQuoted(objectName.getKeyPropertyList().get(property)) + ",";
            }
        }
        if(result.length()>0)
            return result.substring(0,result.length()-1);
        else
            return result;
    }

    private String getPrometheusQuoted(String s) {
        return (!s.startsWith("\"")) ? "\"" + s + "\"" : s;
    }

    private String getPrometheusEscape(String property) {
        return property.replace(" ",PROMETHEUS_SEPARATOR).replace(".",PROMETHEUS_SEPARATOR).replace("/",PROMETHEUS_SEPARATOR);
    }

    private String getPrometheusName(ObjectName objectName) {
        String result = getPrometheusEscape(objectName.getDomain());
        result += PROMETHEUS_SEPARATOR + getPrometheusEscape(objectName.getKeyPropertyList().get("type"));
        return result;
    }

    private boolean prometheusAllowedType(String type) {
        return ("int".equals(type) || "double".equals(type) || "long".equals(type) ||
                "java.lang.Integer".equals(type) || "java.lang.Double".equals(type) || "java.lang.Long".equals(type));
    }

    private void getTrackerMetrics(SolrQueryRequest req, SolrQueryResponse rsp) {
        TrackerRegistry trackerRegistry = coreAdminHandler.getTrackerRegistry();

        MetadataTracker metaTrkr = trackerRegistry.getTrackerForCore(coreName, MetadataTracker.class);
        TrackerState metadataTrkrState = metaTrkr.getTrackerState();
        long lastIndexedTxId = metadataTrkrState.getLastIndexedTxId();
        long lastTxCommitTimeOnServer = metadataTrkrState.getLastTxCommitTimeOnServer();
        long lastTxIdOnServer = metadataTrkrState.getLastTxIdOnServer();
        long lastIndexTxCommitTime = metadataTrkrState.getLastIndexedTxCommitTime();
        long transactionsToDo = lastTxIdOnServer - lastIndexedTxId;
        Date lastIndexTxCommitDate = new Date(lastIndexTxCommitTime);
        Date lastTxOnServerDate = new Date(lastTxCommitTimeOnServer);
        if (transactionsToDo < 0)
        {
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
        if (changeSetsToDo < 0)
        {
            changeSetsToDo = 0;
        }

        Duration txLag = new Duration(lastIndexTxCommitDate, lastTxOnServerDate);
        if (lastIndexTxCommitDate.compareTo(lastTxOnServerDate) > 0)
        {
            txLag = new Duration();
        }
        long txLagSeconds = (lastTxCommitTimeOnServer - lastIndexTxCommitTime) / 1000;
        if (txLagSeconds < 0)
        {
            txLagSeconds = 0;
        }

        Duration changeSetLag = new Duration(lastIndexChangeSetCommitDate, lastChangeSetOnServerDate);
        if (lastIndexChangeSetCommitDate.compareTo(lastChangeSetOnServerDate) > 0)
        {
            changeSetLag = new Duration();
        }
        long changeSetLagSeconds = (lastChangeSetCommitTimeOnServer - lastIndexChangeSetCommitTime) / 1000;
        if (changeSetLagSeconds < 0)
        {
            changeSetLagSeconds = 0;
        }

        String resp = String.format("alfresco_summary{core=\"%s\",feature=\"Approx transactions remaining\"}",coreName);
        rsp.add(resp,transactionsToDo);
        resp = String.format("alfresco_summary{core=\"%s\",feature=\"TX lag\"}",coreName);
        rsp.add(resp,txLagSeconds);
        resp = String.format("alfresco_summary{core=\"%s\",feature=\"Last Index TX Commit Time\"}",coreName);
        rsp.add(resp,lastIndexTxCommitTime);

        resp = String.format("alfresco_summary{core=\"%s\",feature=\"Approx change sets remaining\"}",coreName);
        rsp.add(resp,changeSetsToDo);
        resp = String.format("alfresco_summary{core=\"%s\",feature=\"Change Set Lag\"}",coreName);
        rsp.add(resp,changeSetLagSeconds);
        resp = String.format("alfresco_summary{core=\"%s\",feature=\"Last Index Change Set Commit Time\"}",coreName);
        rsp.add(resp,lastIndexChangeSetCommitTime);
    }

    private void getFTSMetrics(SolrQueryRequest req, SolrQueryResponse rsp) {
        if(server==null) return;        NamedList<Object> report = new NamedList();
        // FTS
        server.addFTSStatusCounts(report);
        for(Entry fts : report) {
            String resp = String.format("alfresco_summary{core=\"%s\",feature=\"%s\"}",req.getCore().getName(),fts.getKey());
            rsp.add(resp,fts.getValue());
        }
    }

    private void getCoreStats(SolrQueryRequest req, SolrQueryResponse rsp) throws IOException {
        // Core stats
        if(server==null) return; Iterable<Entry<String, Object>> stats = server.getCoreStats();
        for(Entry<String,Object> stat : stats) {
            if(fieldsToMonitor.contains(stat.getKey())) {
                String resp = String.format("alfresco_summary{core=\"%s\",feature=\"%s\"}",req.getCore().getName(),stat.getKey());
                rsp.add(resp,stat.getValue());
            }
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
