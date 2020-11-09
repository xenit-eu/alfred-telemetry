package eu.xenit.alfred.telemetry.solr.monitoring.binder;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.management.Attribute;
import javax.management.AttributeList;
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
import org.alfresco.solr.AlfrescoCoreAdminHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolrBeansMetrics implements MeterBinder {

    MBeanServer mBeanServer;
    MeterRegistry registry;
    // atm only monitor alfresco core
    ArrayList<String> beansToMonitorSolr = new ArrayList(Arrays.asList(
            "solr/alfresco:type=searcher,id=org.apache.solr.search.SolrIndexSearcher",
            "solr/alfresco:type=/afts,id=org.apache.solr.handler.component.AlfrescoSearchHandler",
            "solr/alfresco:type=/cmis,id=org.apache.solr.handler.component.AlfrescoSearchHandler"));

    Logger logger = LoggerFactory.getLogger(SolrBeansMetrics.class);

    public SolrBeansMetrics(MBeanServer mBeanServer) {
        this.mBeanServer = mBeanServer;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        this.registry = registry;
        registerSolrBeans();
    }

    private void registerSolrBeans() {
        for (String beanToMonitor : beansToMonitorSolr) {
            ObjectName objectName = null;
            try {
                objectName = new ObjectName((String) beanToMonitor);
            } catch (MalformedObjectNameException e) {
                logger.error("Malformed bean name " + beanToMonitor);
                continue;
            }
            MBeanInfo mBeanInfo = null;
            try {
                mBeanInfo = mBeanServer.getMBeanInfo(objectName);
            } catch (InstanceNotFoundException | IntrospectionException | ReflectionException e) {
                logger.error("Problem with bean " + beanToMonitor);
                continue;
            }
            MBeanAttributeInfo[] attributeInfos = mBeanInfo.getAttributes();

            Map<String, MBeanAttributeInfo> name2AttributeInfo = new LinkedHashMap<String, MBeanAttributeInfo>();
            for (int idx = 0; idx < attributeInfos.length; ++idx) {
                MBeanAttributeInfo attr = attributeInfos[idx];
                name2AttributeInfo.put(attr.getName(), attr);
            }
            final AttributeList attributes;
            try {
                attributes = mBeanServer.getAttributes(objectName, name2AttributeInfo.keySet().toArray(new String[0]));
                if (attributes == null) {
                    logger.error("Null attributes for " + objectName);
                    return;
                }
            } catch (Exception e) {
                logger.error("Fail to get attributes for " + objectName.toString());
                return;
            }

            for (Attribute attribute : attributes.asList()) {
                String attributeName = attribute.getName();
                if (attribute.getValue() instanceof Number) {
                    ObjectName finalObjectName = objectName;
                    Gauge.builder(objectName.getDomain() + "_" + objectName.getKeyProperty("type") + "_" + attribute.getName(), objectName, x -> getValueFromBean(mBeanServer,finalObjectName,attribute.getName()))
                            .register(registry);
                }
            }
        }
    }

    private Double getValueFromBean(MBeanServer mBeanServer, ObjectName finalObjectName,
            String attributeName) {
        try {
            Object attribute = mBeanServer.getAttribute(finalObjectName, attributeName);
            return new Double(attribute.toString());
        } catch (InstanceNotFoundException | ReflectionException | MBeanException | AttributeNotFoundException e) {
            logger.error("Fail to get attribute " + attributeName + " for " + finalObjectName);
        }
        return 0.0;
    }
}
