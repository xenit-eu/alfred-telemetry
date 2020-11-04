package eu.xenit.alfred.telemetry.solr.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularType;
import org.apache.solr.core.JmxMonitoredMap;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmxMetrics {

    private static final String PROMETHEUS_SEPARATOR = "_";

    // java beans
    ArrayList<String> beansToMonitorClassLoading = new ArrayList(Arrays.asList("java.lang:type=ClassLoading"));
    ArrayList<String> beansToMonitorGC = new ArrayList(Arrays.asList("java.lang:type=GarbageCollector,name=ConcurrentMarkSweep",
                                                                     "java.lang:type=GarbageCollector,name=PS MarkSweep"));
    ArrayList<String> beansToMonitorMemory = new ArrayList(Arrays.asList("java.lang:type=Memory"));
    ArrayList<String> beansToMonitorMemoryPoolCMSOldGen = new ArrayList(Arrays.asList("java.lang:type=MemoryPool,name=CMS Old Gen",
                                                                                      "java.lang:type=MemoryPool,name=PS Old Gen"));
    ArrayList<String> beansToMonitorMemoryPoolParEdenSpace = new ArrayList(Arrays.asList("java.lang:type=MemoryPool,name=Par Eden Space",
                                                                                        "java.lang:type=MemoryPool,name=PS Eden Space"));
    ArrayList<String> beansToMonitorMemoryPoolParSurvivorSpace = new ArrayList(Arrays.asList("java.lang:type=MemoryPool,name=Par Survivor Space",
                                                                                             "java.lang:type=MemoryPool,name=PS Survivor Space"));
    ArrayList<String> beansToMonitorOS = new ArrayList(Arrays.asList("java.lang:type=OperatingSystem"));
    ArrayList<String> beansToMonitorRuntime = new ArrayList(Arrays.asList("java.lang:type=Runtime"));
    ArrayList<String> beansToMonitorThreading = new ArrayList(Arrays.asList("java.lang:type=Threading"));

    // tomcat beans
    ArrayList<String> beansToMonitorThreadPool = new ArrayList(Arrays.asList("Catalina:type=ThreadPool,name=\"http-bio-8443\"",
                                                                             "Catalina:type=ThreadPool,name=\"http-bio-8080\""));
    ArrayList<String> beansToMonitorRequests = new ArrayList(Arrays.asList("Catalina:type=GlobalRequestProcessor,name=\"http-bio-8443\"",
                                                                           "Catalina:type=GlobalRequestProcessor,name=\"http-bio-8080\""));
    ArrayList<String> beansToMonitorSessions = new ArrayList(Arrays.asList("Catalina:type=Manager,context=/solr4,host=localhost"));

    // solr beans
    ArrayList<String> beansToMonitorSolr = new ArrayList(Arrays.asList("solr/alfresco:type=searcher,id=org.apache.solr.search.SolrIndexSearcher",
            "solr/alfresco:type=/afts,id=org.apache.solr.handler.component.AlfrescoSearchHandler",
            "solr/alfresco:type=/cmis,id=org.apache.solr.handler.component.AlfrescoSearchHandler"));

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

    Logger logger = LoggerFactory.getLogger(JmxMetrics.class);

    public JmxMetrics(boolean enableJmxMetricsClassLoading,
            boolean enableJmxMetricsGC,
            boolean enableJmxMetricsMemory,
            boolean enableJmxMetricsMemoryPoolCMSOldGen,
            boolean enableJmxMetricsMemoryPoolParEdenSpace,
            boolean enableJmxMetricsMemoryPoolParSurvivorSpace,
            boolean enableJmxMetricsOS,
            boolean enableJmxMetricsRuntime,
            boolean enableJmxMetricsThreading,
            boolean enableJmxMetricsThreadPool,
            boolean enableJmxMetricsSessions,
            boolean enableJmxMetricsRequests,
            boolean enableJmxMetricsSolr) {
        this.enableJmxMetricsClassLoading = enableJmxMetricsClassLoading;
        this.enableJmxMetricsGC = enableJmxMetricsGC;
        this.enableJmxMetricsMemory = enableJmxMetricsMemory;
        this.enableJmxMetricsMemoryPoolCMSOldGen = enableJmxMetricsMemoryPoolCMSOldGen;
        this.enableJmxMetricsMemoryPoolParEdenSpace = enableJmxMetricsMemoryPoolParEdenSpace;
        this.enableJmxMetricsMemoryPoolParSurvivorSpace = enableJmxMetricsMemoryPoolParSurvivorSpace;
        this.enableJmxMetricsOS = enableJmxMetricsOS;
        this.enableJmxMetricsRuntime = enableJmxMetricsRuntime;
        this.enableJmxMetricsThreading = enableJmxMetricsThreading;

        this.enableJmxMetricsRequests = enableJmxMetricsRequests;
        this.enableJmxMetricsThreadPool = enableJmxMetricsThreadPool;
        this.enableJmxMetricsSessions = enableJmxMetricsSessions;

        this.enableJmxMetricsSolr = enableJmxMetricsSolr;
    }

    void getJmxMetrics(SolrQueryRequest req, SolrQueryResponse rsp) {
        Map registry = req.getCore().getInfoRegistry();
        MBeanServer mbeanServer = ((JmxMonitoredMap) registry).getServer();

        if (enableJmxMetricsClassLoading) {
            getJmxMetricsPerBeans(mbeanServer, beansToMonitorClassLoading, rsp);
        }
        if (enableJmxMetricsGC) {
            getJmxMetricsPerBeans(mbeanServer, beansToMonitorGC, rsp);
        }
        if (enableJmxMetricsMemory) {
            getJmxMetricsPerBeans(mbeanServer, beansToMonitorMemory, rsp);
        }
        if (enableJmxMetricsMemoryPoolCMSOldGen) {
            getJmxMetricsPerBeans(mbeanServer, beansToMonitorMemoryPoolCMSOldGen, rsp);
        }
        if (enableJmxMetricsMemoryPoolParEdenSpace) {
            getJmxMetricsPerBeans(mbeanServer, beansToMonitorMemoryPoolParEdenSpace, rsp);
        }
        if (enableJmxMetricsMemoryPoolParSurvivorSpace) {
            getJmxMetricsPerBeans(mbeanServer, beansToMonitorMemoryPoolParSurvivorSpace, rsp);
        }
        if (enableJmxMetricsOS) {
            getJmxMetricsPerBeans(mbeanServer, beansToMonitorOS, rsp);
        }
        if (enableJmxMetricsRuntime) {
            getJmxMetricsPerBeans(mbeanServer, beansToMonitorRuntime, rsp);
        }
        if (enableJmxMetricsThreading) {
            getJmxMetricsPerBeans(mbeanServer, beansToMonitorThreading, rsp);
        }
        if (enableJmxMetricsThreadPool) {
            getJmxMetricsPerBeans(mbeanServer, beansToMonitorThreadPool, rsp);
        }
        if (enableJmxMetricsRequests) {
            getJmxMetricsPerBeans(mbeanServer, beansToMonitorRequests, rsp);
        }
        if (enableJmxMetricsSessions) {
            getJmxMetricsPerBeans(mbeanServer, beansToMonitorSessions, rsp);
        }
        if (enableJmxMetricsSolr) {
            getJmxMetricsPerBeans(mbeanServer, beansToMonitorSolr, rsp);
        }
    }


    private void getJmxMetricsPerBeans(MBeanServer mbeanServer, ArrayList<String> beansToMonitor,
            SolrQueryResponse rsp) {
        for (String beanToMonitor : beansToMonitor) {
            ObjectName objectName = null;
            try {
                objectName = new ObjectName(beanToMonitor);
            } catch (MalformedObjectNameException e) {
                logger.error("Malformed bean name " + beanToMonitor);
                continue;
            }
            MBeanInfo mBeanInfo = null;
            try {
                mBeanInfo = mbeanServer.getMBeanInfo(objectName);
            } catch (InstanceNotFoundException | IntrospectionException | ReflectionException e) {
                //logger.error("Problem with bean " + beanToMonitor);
                continue;
            }

            MBeanAttributeInfo[] attrInfos = mBeanInfo.getAttributes();
            Map<String, MBeanAttributeInfo> name2AttrInfo = new LinkedHashMap<String, MBeanAttributeInfo>();
            for (int i = 0; i < attrInfos.length; ++i) {
                MBeanAttributeInfo attr = attrInfos[i];
                if (!attr.isReadable()) {
                    logger.info("{} is not readable", attr);
                    continue;
                }
                name2AttrInfo.put(attr.getName(), attr);
            }
            AttributeList attributes = null;
            try {
                attributes = mbeanServer.getAttributes(objectName, name2AttrInfo.keySet().toArray(new String[0]));
            } catch (InstanceNotFoundException | ReflectionException e) {
                e.printStackTrace();
            }
            for (Object attributeObj : attributes.asList()) {
                if (Attribute.class.isInstance(attributeObj)) {
                    Attribute attribute = (Attribute) (attributeObj);
                    MBeanAttributeInfo attr = name2AttrInfo.get(attribute.getName());
                    processBeanValue(
                            rsp,
                            objectName.getDomain(),
                            objectName.getKeyPropertyList(),
                            new LinkedList<String>(),
                            attr.getName(),
                            attr.getType(),
                            attr.getDescription(),
                            attribute.getValue()
                    );
                }
            }
        }
    }

    /**
     * Recursive function for exporting the values of an mBean. JMX is a very open technology, without any prescribed
     * way of declaring mBeans so this function tries to do a best-effort pass of getting the values/names out in a way
     * it can be processed elsewhere easily.
     */
    private void processBeanValue(
            SolrQueryResponse rsp, String domain,
            Hashtable<String, String> beanProperties,
            LinkedList<String> attrKeys,
            String attrName,
            String attrType,
            String attrDescription,
            Object value) {
        if (value == null) {
            logger.debug("{} {} {} - null", domain, beanProperties, attrName);
            return;
        } else if (value instanceof Number || value instanceof java.util.Date) {
            if (value instanceof java.util.Date) {
                attrType = "java.lang.Double";
                value = ((java.util.Date) value).getTime() / 1000.0;
            }
            recordBean(rsp,
                    domain,
                    beanProperties,
                    attrKeys,
                    attrName,
                    attrType,
                    attrDescription,
                    value);
        } else if (value instanceof CompositeData) {
            CompositeData composite = (CompositeData) value;
            CompositeType type = composite.getCompositeType();
            attrKeys = new LinkedList<String>(attrKeys);
            attrKeys.add(attrName);
            for (String key : type.keySet()) {
                String typ = type.getType(key).getTypeName();
                Object valu = composite.get(key);
                processBeanValue(
                        rsp, domain,
                        beanProperties,
                        attrKeys,
                        key,
                        typ,
                        type.getDescription(),
                        valu);
            }
        } else if (value instanceof TabularData) {
            TabularData tds = (TabularData) value;
            TabularType tt = tds.getTabularType();

            List<String> rowKeys = tt.getIndexNames();

            CompositeType type = tt.getRowType();
            Set<String> valueKeys = new TreeSet<String>(type.keySet());
            valueKeys.removeAll(rowKeys);

            LinkedList<String> extendedAttrKeys = new LinkedList<String>(attrKeys);
            extendedAttrKeys.add(attrName);
            for (Object valu : tds.values()) {
                if (valu instanceof CompositeData) {
                    CompositeData composite = (CompositeData) valu;
                    Hashtable<String, String> l2s = new Hashtable<>(beanProperties);
                    for (String idx : rowKeys) {
                        Object obj = composite.get(idx);
                        if (obj != null) {
                            // Nested tabulardata will repeat the 'key' label, so
                            // append a suffix to distinguish each.
                            while (l2s.containsKey(idx)) {
                                idx = idx + "_";
                            }
                            l2s.put(idx, obj.toString());
                        }
                    }
                    for (String valueIdx : valueKeys) {
                        LinkedList<String> attrNames = extendedAttrKeys;
                        String typ = type.getType(valueIdx).getTypeName();
                        String name = valueIdx;
                        if (valueIdx.toLowerCase().equals("value")) {
                            // Skip appending 'value' to the name
                            attrNames = attrKeys;
                            name = attrName;
                        }
                        processBeanValue(
                                rsp, domain,
                                l2s,
                                attrNames,
                                name,
                                typ,
                                type.getDescription(),
                                composite.get(valueIdx));
                    }
                } else {
                    logger.error("not a correct tabulardata format");
                }
            }
        }
    }

    private void recordBean(SolrQueryResponse rsp, String domain,
            Hashtable<String, String> beanProperties, LinkedList<String> attrKeys,
            String attrName, String attrType, String attrDescription, Object value) {
        String resp = String.format("%s_%s_%s%s", getPrometheusEscape(domain), getPrometheusEscape(beanProperties.get("type")),attrName, getPrometheusEscape(beanProperties));
        if(attrKeys.size()>0)
            resp = String.format("%s_%s_%s_%s%s", getPrometheusEscape(domain), getPrometheusEscape(beanProperties.get("type")),getPrometheusEscape(attrKeys), attrName, getPrometheusEscape(beanProperties));
        rsp.add(resp, value);
    }


    private String getPrometheusEscape(Hashtable<String, String> beanProperties) {
        String result = "{";
        for(String property : beanProperties.keySet()) {
            if(!("type".equals(property))) {
                result += getPrometheusEscape(property) + "=";
                result += getPrometheusQuoted(beanProperties.get(property));
                result += ",";
            }
        }
        if(result.length()>1)
            result = result.substring(0,result.length()-1);
        result += "}";
        return result;
    }


    private String getPrometheusQuoted(String s) {
        return (!s.startsWith("\"")) ? "\"" + s + "\"" : s;
    }

    private String getPrometheusEscape(String property) {
        if(property.startsWith("/"))
            property = property.substring(1);
        return property.replace(" ", PROMETHEUS_SEPARATOR).replace(".", PROMETHEUS_SEPARATOR)
                .replace("/", PROMETHEUS_SEPARATOR);
    }

    private String getPrometheusEscape(LinkedList<String> attrKeys) {
        String result = "";
        for(String key : attrKeys) {
            result += getPrometheusEscape(key) + PROMETHEUS_SEPARATOR;
        }
        if(result.length()>0)
            result = result.substring(0,result.length()-1);

        return result;
    }
}
