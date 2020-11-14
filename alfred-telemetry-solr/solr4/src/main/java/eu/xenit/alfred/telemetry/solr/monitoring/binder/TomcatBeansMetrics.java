package eu.xenit.alfred.telemetry.solr.monitoring.binder;

import eu.xenit.alfred.telemetry.solr.util.JmxUtils;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* Implemented because jmx metrics from micrometer - TomcatMetrics need a manager object which is not available */
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
        JmxUtils.registerBeans(mBeanServer,beansToMonitorTomcat,registry);
    }
}
