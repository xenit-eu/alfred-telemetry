package eu.xenit.alfred.telemetry.solr.monitoring.binder;

import eu.xenit.alfred.telemetry.solr.util.StringUtils;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;

public class SystemMetrics implements MeterBinder {

    @Override
    public void bindTo(MeterRegistry registry) {
        if(StringUtils.isEnabled("METRICS_SYSTEM_UPTIME_ENABLED"))
            new UptimeMetrics().bindTo(registry);
        if(StringUtils.isEnabled("METRICS_SYSTEM_PROCESSOR_ENABLED"))
            new ProcessorMetrics().bindTo(registry);
        if(StringUtils.isEnabled("METRICS_SYSTEM_FILEDESCRIPTORS_ENABLED"))
            new FileDescriptorMetrics().bindTo(registry);
    }
}
