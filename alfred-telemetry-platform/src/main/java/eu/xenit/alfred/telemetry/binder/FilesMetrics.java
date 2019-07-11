package eu.xenit.alfred.telemetry.binder;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import javax.annotation.Nonnull;

/**
 * {@link io.micrometer.core.instrument.binder.system.FileDescriptorMetrics} wrapper to make this metrics configurable
 * under the 'files' property key.
 */
public class FilesMetrics implements MeterBinder {

    @Override
    public void bindTo(@Nonnull MeterRegistry registry) {
        new FileDescriptorMetrics().bindTo(registry);
    }
}
