package eu.xenit.alfred.telemetry.solr.handler;

import eu.xenit.alfred.telemetry.solr.monitoring.binder.JvmMetrics;
import eu.xenit.alfred.telemetry.solr.monitoring.binder.MyTomcatMetrics;
import eu.xenit.alfred.telemetry.solr.monitoring.binder.ProcessMetrics;
import eu.xenit.alfred.telemetry.solr.monitoring.binder.SolrMetrics;
import eu.xenit.alfred.telemetry.solr.monitoring.binder.SystemMetrics;
import eu.xenit.alfred.telemetry.solr.monitoring.registry.RegistryRegistrar;
import eu.xenit.alfred.telemetry.solr.util.PrometheusRegistryUtil;
import eu.xenit.alfred.telemetry.solr.util.Util;
import io.micrometer.core.instrument.MeterRegistry;
import java.io.IOException;
import javax.management.MBeanServer;
import org.alfresco.solr.AlfrescoCoreAdminHandler;
import org.apache.solr.core.JmxMonitoredMap;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MicrometerHandler extends RequestHandlerBase {

    static MeterRegistry registry = RegistryRegistrar.getInstance().getGlobalMeterRegistry();
    static SolrMetrics solrMetrics = null;
    static MyTomcatMetrics tomcatMetrics = null;

    static {
        if( Util.isEnabled("METRICS_JVM_ENABLED")) {
            new JvmMetrics().bindTo(registry);
        }
        if( Util.isEnabled("METRICS_PROCESS_ENABLED")) {
            new ProcessMetrics().bindTo(registry);
        }
        if( Util.isEnabled("METRICS_SYSTEM_ENABLED")) {
            new SystemMetrics().bindTo(registry);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(MicrometerHandler.class);

    @Override
    public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
        AlfrescoCoreAdminHandler coreAdminHandler = (AlfrescoCoreAdminHandler) req.getCore().getCoreDescriptor()
                .getCoreContainer().getMultiCoreHandler();
        MBeanServer mbeanServer = ((JmxMonitoredMap) req.getCore().getInfoRegistry()).getServer();

        if (solrMetrics == null && Util.isEnabled("METRICS_SOLR_ENABLED")) {
            solrMetrics = new SolrMetrics(coreAdminHandler, mbeanServer);
            solrMetrics.bindTo(registry);
        }

        if (tomcatMetrics == null && Util.isEnabled("METRICS_TOMCAT_ENABLED")) {
            tomcatMetrics = new MyTomcatMetrics(mbeanServer);
            tomcatMetrics.bindTo(registry);
        }
        writeTextToResponse(PrometheusRegistryUtil.extractPrometheusScrapeData(RegistryRegistrar.getInstance().getPrometheusMeterRegistry()),
                rsp);
    }

    private void writeTextToResponse(final String text, final SolrQueryResponse rsp) throws IOException {
        rsp.add("allMetrics", text);
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
