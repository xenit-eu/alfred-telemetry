package eu.xenit.alfred.telemetry.binder;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.tomcat.TomcatMetrics;

import javax.annotation.Nonnull;
import javax.management.MBeanServer;
import java.util.ArrayList;

public class AlfrescoTomcatMetrics implements NamedMeterBinder {
    private MBeanServer mBeanServer;

    public AlfrescoTomcatMetrics(MBeanServer mBeanServer) {
        this.mBeanServer = mBeanServer;
    }

    @Override
    public void bindTo(@Nonnull MeterRegistry registry) {
        new TomcatMetrics(null, new ArrayList<>(), mBeanServer).bindTo(registry);
    }

    @Override
    public String getName() {
        return "tomcat";
    }
}
