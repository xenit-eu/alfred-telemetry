package eu.xenit.alfred.telemetry.solr.monitoring.binder;

import eu.xenit.alfred.telemetry.solr.util.Util;
import io.github.mweirauch.micrometer.jvm.extras.ProcessMemoryMetrics;
import io.github.mweirauch.micrometer.jvm.extras.ProcessThreadMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

public class ProcessMetrics implements MeterBinder {

    @Override
    public void bindTo(MeterRegistry registry) {
        if(Util.isEnabled("METRICS_PROCESS_THREADS_ENABLED")) {
            new ProcessThreadMetrics().bindTo(registry);
        }
        if(Util.isEnabled("METRICS_PROCESS_MEMORY_ENABLED")) {
            new ProcessMemoryMetrics().bindTo(registry);
        }
    }
}
