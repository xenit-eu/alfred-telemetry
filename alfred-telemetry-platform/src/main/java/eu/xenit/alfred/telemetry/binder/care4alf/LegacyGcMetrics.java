package eu.xenit.alfred.telemetry.binder.care4alf;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import javax.annotation.Nonnull;

public class LegacyGcMetrics implements MeterBinder {

    private static final String METER_PREFIX_JVM_GC = "jvm.gc.";

    @Override
    public void bindTo(@Nonnull MeterRegistry registry) {
        ManagementFactory.getGarbageCollectorMXBeans().forEach(b -> this.monitorBean(b, registry));
    }

    private void monitorBean(@Nonnull final GarbageCollectorMXBean bean, @Nonnull final MeterRegistry registry) {
        final String beanName = bean.getName().replace(" ", "");

        Gauge.builder(METER_PREFIX_JVM_GC + beanName + ".count", bean, GarbageCollectorMXBean::getCollectionCount)
                .description(
                        "Returns the total number of collections that have occurred or -1 if the collection count is undefined for this collector.")
                .register(registry);

        Gauge.builder(METER_PREFIX_JVM_GC + beanName + ".time.ms", bean, GarbageCollectorMXBean::getCollectionTime)
                .description("Returns the approximate accumulated collection elapsed time in milliseconds "
                        + "or -1 if the collection elapsed time is undefined for this collector.")
                .baseUnit("milliseconds")
                .register(registry);

        Gauge.builder(METER_PREFIX_JVM_GC + beanName + ".time.s", bean, b -> b.getCollectionTime() / 1000)
                .description("Returns the approximate accumulated collection elapsed time in seconds "
                        + "or -1 if the collection elapsed time is undefined for this collector.")
                .baseUnit("seconds")
                .register(registry);
    }
}
