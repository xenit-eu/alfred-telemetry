package eu.xenit.alfred.telemetry.solr.handler;

import eu.xenit.alfred.telemetry.solr.monitoring.binder.JvmMetrics;
import eu.xenit.alfred.telemetry.solr.monitoring.binder.ProcessMetrics;
import eu.xenit.alfred.telemetry.solr.monitoring.binder.SolrMetrics;
import eu.xenit.alfred.telemetry.solr.monitoring.binder.SystemMetrics;
import eu.xenit.alfred.telemetry.solr.monitoring.registry.RegistryRegistraar;
import eu.xenit.alfred.telemetry.solr.util.PrometheusRegistryUtil;
import eu.xenit.alfred.telemetry.solr.util.Util;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jetty.TimedHandler;
import org.alfresco.solr.AlfrescoCoreAdminHandler;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.util.JmxUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import java.io.IOException;

public class MicrometerHandler extends RequestHandlerBase {

    static MeterRegistry registry = RegistryRegistraar.getInstance().getGlobalMeterRegistry();
    static SolrMetrics solrMetrics = null;
    static TimedHandler jettyMetrics = null;

    static {
        if( Util.isEnabled("ALFRED_TELEMETRY_JVM_ENABLED"))
            new JvmMetrics().bindTo(registry);
        if( Util.isEnabled("METRICS_PROCESS_ENABLED"))
            new ProcessMetrics().bindTo(registry);
        if( Util.isEnabled("METRICS_SYSTEM_ENABLED"))
            new SystemMetrics().bindTo(registry);
    }

    Logger logger = LoggerFactory.getLogger(MicrometerHandler.class);


    @Override
    public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
        AlfrescoCoreAdminHandler coreAdminHandler = (AlfrescoCoreAdminHandler) req.getCore().getCoreContainer()
                .getMultiCoreHandler();
        MBeanServer mbeanServer = JmxUtil.findFirstMBeanServer();

        if(jettyMetrics == null) {
  //          jettyMetrics = new TimedHandler(registry, Tags.empty());
            /*logger.error("class=" + req.getHttpSolrCall().getReq().getClass());
            Enumeration<String> attributeNames = req.getHttpSolrCall().getReq().getAttributeNames();
            logger.error("attributes=" + attributeNames);

            Server server = (Server) req.getHttpSolrCall().getReq().getAttribute("org.eclipse.jetty.server.Server");
            if(server==null) {
                logger.error("There is no jetty server");
                logger.error("attributes=" + req.getHttpSolrCall().getReq().getAttributeNames());
            } else {
                logger.error("server=" + server);
                StatisticsHandler statisticsHandler = (StatisticsHandler) server.getChildHandlerByClass(StatisticsHandler.class);

                jettyMetrics = new JettyStatisticsMetrics(statisticsHandler, null);
                jettyMetrics.bindTo(registry);
            }*/
        }

        if (solrMetrics == null && Util.isEnabled("METRICS_SOLR_ENABLED")) {
            solrMetrics = new SolrMetrics(coreAdminHandler, mbeanServer);
            solrMetrics.bindTo(registry);
        }
        writeTextToResponse(PrometheusRegistryUtil.extractPrometheusScrapeData(RegistryRegistraar.getInstance().getPrometheusMeterRegistry()),
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
