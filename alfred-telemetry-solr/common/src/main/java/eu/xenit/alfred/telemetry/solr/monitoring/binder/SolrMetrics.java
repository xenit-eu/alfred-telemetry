package eu.xenit.alfred.telemetry.solr.monitoring.binder;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map.Entry;
import javax.management.MBeanServer;
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
