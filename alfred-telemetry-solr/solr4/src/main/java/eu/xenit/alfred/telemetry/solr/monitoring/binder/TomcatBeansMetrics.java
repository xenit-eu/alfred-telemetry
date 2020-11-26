package eu.xenit.alfred.telemetry.solr.monitoring.binder;

import eu.xenit.alfred.telemetry.solr.util.JmxUtils;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.util.ArrayList;
import java.util.Arrays;
import javax.management.MBeanServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 *  Implemented because micrometer's default TomcatMetrics needs a manager object which is not available for Alfresco's solr
 ***/
public class TomcatBeansMetrics implements MeterBinder {

    MBeanServer mBeanServer;
    MeterRegistry registry;

    // atm only monitor alfresco core
    ArrayList<String> beansToMonitorTomcat = new ArrayList(Arrays.asList(
            "Catalina:type=Manager,context=/solr4,host=localhost"));

    Logger logger = LoggerFactory.getLogger(TomcatBeansMetrics.class);

    public TomcatBeansMetrics(MBeanServer mBeanServer) {
        this.mBeanServer = mBeanServer;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        this.registry = registry;
        JmxUtils.registerBeans(mBeanServer, beansToMonitorTomcat, registry);
    }
}
