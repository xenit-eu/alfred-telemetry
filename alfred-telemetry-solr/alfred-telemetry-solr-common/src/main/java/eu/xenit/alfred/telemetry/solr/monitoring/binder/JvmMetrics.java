package eu.xenit.alfred.telemetry.solr.monitoring.binder;

import eu.xenit.alfred.telemetry.solr.util.Util;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;

public class JvmMetrics implements MeterBinder {

    @Override
    public void bindTo(MeterRegistry registry) {
        if(Util.isEnabled("METRICS_JVM_GC_ENABLED"))
            new JvmGcMetrics().bindTo(registry);
        if(Util.isEnabled("METRICS_JVM_MEMORY_ENABLED"))
            new JvmMemoryMetrics().bindTo(registry);
        if(Util.isEnabled("METRICS_JVM_THREADS_ENABLED"))
            new JvmThreadMetrics().bindTo(registry);
        if(Util.isEnabled("METRICS_JVM_CLASSLOADER_ENABLED"))
            new ClassLoaderMetrics().bindTo(registry);
    }
}

