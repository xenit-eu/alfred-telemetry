package eu.xenit.alfred.telemetry.solr.handler;

import eu.xenit.alfred.telemetry.solr.monitoring.binder.JvmMetrics;
import eu.xenit.alfred.telemetry.solr.monitoring.binder.ProcessMetrics;
import eu.xenit.alfred.telemetry.solr.monitoring.binder.SolrMetrics;
import eu.xenit.alfred.telemetry.solr.monitoring.binder.SystemMetrics;
import eu.xenit.alfred.telemetry.solr.monitoring.registry.RegistryRegistraar;
import eu.xenit.alfred.telemetry.solr.util.PrometheusRegistryUtil;
import io.micrometer.core.instrument.MeterRegistry;
import java.io.IOException;
import javax.management.MBeanServer;
import org.alfresco.solr.AlfrescoCoreAdminHandler;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.util.JmxUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MicrometerHandler extends RequestHandlerBase {

    static RegistryRegistraar registraar = new RegistryRegistraar();
    static MeterRegistry registry = registraar.getGlobalMeterRegistry();
    static SolrMetrics solrMetrics = null;

    static {
        new JvmMetrics().bindTo(registry);
        new ProcessMetrics().bindTo(registry);
        new SystemMetrics().bindTo(registry);
    }

    Logger logger = LoggerFactory.getLogger(MicrometerHandler.class);


    @Override
    public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
        AlfrescoCoreAdminHandler coreAdminHandler = (AlfrescoCoreAdminHandler) req.getCore().getCoreContainer()
                .getMultiCoreHandler();
        MBeanServer mbeanServer = JmxUtil.findFirstMBeanServer();
        if (solrMetrics == null) {
            solrMetrics = new SolrMetrics(coreAdminHandler, mbeanServer);
            solrMetrics.bindTo(registry);
        }
        writeTextToResponse(PrometheusRegistryUtil.extractPrometheusScrapeData(registraar.getPrometheusMeterRegistry()),
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
