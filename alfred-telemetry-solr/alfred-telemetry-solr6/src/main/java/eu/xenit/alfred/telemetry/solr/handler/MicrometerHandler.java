package eu.xenit.alfred.telemetry.solr.handler;

import eu.xenit.alfred.telemetry.solr.monitoring.binder.JvmMetrics;
import eu.xenit.alfred.telemetry.solr.monitoring.binder.ProcessMetrics;
import eu.xenit.alfred.telemetry.solr.monitoring.binder.SolrMetrics;
import eu.xenit.alfred.telemetry.solr.monitoring.binder.SystemMetrics;
import eu.xenit.alfred.telemetry.solr.monitoring.registry.RegistryRegistrar;
import eu.xenit.alfred.telemetry.solr.util.PrometheusRegistryUtil;
import eu.xenit.alfred.telemetry.solr.util.Util;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.jetty.JettyStatisticsMetrics;
import org.alfresco.solr.AlfrescoCoreAdminHandler;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.util.JmxUtil;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import java.io.IOException;

public class MicrometerHandler extends RequestHandlerBase {

    static MeterRegistry registry = RegistryRegistrar.getInstance().getGlobalMeterRegistry();
    static SolrMetrics solrMetrics = null;
    static JettyStatisticsMetrics jettyMetrics = null;

    static {
        if( Util.isEnabled("ALFRED_TELEMETRY_JVM_ENABLED")) {
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
        AlfrescoCoreAdminHandler coreAdminHandler = (AlfrescoCoreAdminHandler) req.getCore().getCoreContainer()
                .getMultiCoreHandler();
        MBeanServer mbeanServer = JmxUtil.findFirstMBeanServer();

        if(jettyMetrics == null && Util.isEnabled("METRICS_JETTY_ENABLED")) {
            Server server =  (Server)req.getHttpSolrCall().getReq().getAttribute("org.eclipse.jetty.server.Server");
            if(server==null) {
                logger.error("There is no jetty server");
            } else {
                StatisticsHandler statisticsHandler = (StatisticsHandler) server.getChildHandlerByClass(StatisticsHandler.class);
                if(statisticsHandler==null) {
                    logger.error("There is no StatisticsHandler");
                } else{
                    jettyMetrics = new JettyStatisticsMetrics(statisticsHandler, Tags.empty());
                    jettyMetrics.bindTo(registry);
                }
            }
        }

        if (solrMetrics == null && Util.isEnabled("METRICS_SOLR_ENABLED")) {
            solrMetrics = new SolrMetrics(coreAdminHandler, mbeanServer);
            solrMetrics.bindTo(registry);
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
