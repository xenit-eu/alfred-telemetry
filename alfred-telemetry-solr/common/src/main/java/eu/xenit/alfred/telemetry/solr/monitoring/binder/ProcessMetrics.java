package eu.xenit.alfred.telemetry.solr.monitoring.binder;

import io.github.mweirauch.micrometer.jvm.extras.ProcessMemoryMetrics;
import io.github.mweirauch.micrometer.jvm.extras.ProcessThreadMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

public class ProcessMetrics implements MeterBinder {

    @Override
    public void bindTo(MeterRegistry registry) {
        new ProcessThreadMetrics().bindTo(registry);
        new ProcessMemoryMetrics().bindTo(registry);
    }
}
