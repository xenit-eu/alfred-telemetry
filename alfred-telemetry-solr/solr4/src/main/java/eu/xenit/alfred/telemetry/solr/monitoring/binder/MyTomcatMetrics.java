package eu.xenit.alfred.telemetry.solr.monitoring.binder;


import eu.xenit.alfred.telemetry.solr.util.StringUtils;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.tomcat.TomcatMetrics;
import javax.management.MBeanServer;


public class MyTomcatMetrics implements MeterBinder {

    MBeanServer mBeanServer;

    public MyTomcatMetrics(MBeanServer mBeanServer) {
        this.mBeanServer = mBeanServer;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        new TomcatMetrics(null, null, mBeanServer).bindTo(registry);
        if(StringUtils.isEnabled("METRICS_TOMCAT_JMX_ENABLED"))
            new TomcatBeansMetrics(mBeanServer).bindTo(registry);
    }
}

