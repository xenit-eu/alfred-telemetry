package eu.xenit.alfred.telemetry.solr.monitoring.binder;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import javax.management.MBeanServer;
import org.alfresco.solr.AlfrescoCoreAdminHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolrMetrics implements MeterBinder {

    AlfrescoCoreAdminHandler coreAdminHandler;
    MBeanServer mBeanServer;

    Logger logger = LoggerFactory.getLogger(SolrMetrics.class);

    public SolrMetrics(AlfrescoCoreAdminHandler coreAdminHandler, MBeanServer mBeanServer) {
        this.coreAdminHandler = coreAdminHandler;
        this.mBeanServer = mBeanServer;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        new SolrCoreStatsMetrics(coreAdminHandler).bindTo(registry);
        new SolrFTSMetrics(coreAdminHandler).bindTo(registry);
        new SolrTrackerMetrics(coreAdminHandler).bindTo(registry);
        new SolrBeansMetrics(mBeanServer).bindTo(registry);
    }
}
