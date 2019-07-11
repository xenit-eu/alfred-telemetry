package eu.xenit.alfred.telemetry.binder;

import io.github.mweirauch.micrometer.jvm.extras.ProcessMemoryMetrics;
import io.github.mweirauch.micrometer.jvm.extras.ProcessThreadMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import javax.annotation.Nonnull;

public class ProcessMetrics implements MeterBinder {

    @Override
    public void bindTo(@Nonnull MeterRegistry registry) {
        new ProcessThreadMetrics().bindTo(registry);
        new ProcessMemoryMetrics().bindTo(registry);
    }
}
