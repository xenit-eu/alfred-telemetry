package eu.xenit.alfred.telemetry.solr.monitoring.binder;

import eu.xenit.alfred.telemetry.solr.util.JmxUtil;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.util.ArrayList;
import java.util.Arrays;
import javax.management.MBeanServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolrBeansMetrics implements MeterBinder {

    private MBeanServer mBeanServer;
    private MeterRegistry registry;

    // atm only monitor alfresco core
    private static final ArrayList<String> beansToMonitorSolr = new ArrayList(Arrays.asList(
            "solr/alfresco:type=searcher,id=org.apache.solr.search.SolrIndexSearcher",
            "solr/alfresco:type=/afts,id=org.apache.solr.handler.component.AlfrescoSearchHandler",
            "solr/alfresco:type=/cmis,id=org.apache.solr.handler.component.AlfrescoSearchHandler"));

    private static final Logger logger = LoggerFactory.getLogger(SolrBeansMetrics.class);

    public SolrBeansMetrics(MBeanServer mBeanServer) {
        this.mBeanServer = mBeanServer;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        this.registry = registry;
        JmxUtil.registerBeans(mBeanServer, beansToMonitorSolr, registry);
    }
}
