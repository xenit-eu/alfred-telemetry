package eu.xenit.alfred.telemetry.binder.care4alf;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import javax.annotation.Nonnull;

/**
 * Care4Alf only {@link MeterBinder} for legacy compatibility
 */
public class LegacySystemMetrics implements MeterBinder {

    @Override
    public void bindTo(@Nonnull MeterRegistry registry) {

        Runtime runtime = Runtime.getRuntime();

        Gauge.builder("jvm.memory.runtime.max", runtime, Runtime::maxMemory)
                .description("The maximum amount of memory that the Java virtual machine will attempt to use")
                .register(registry);
        Gauge.builder("jvm.memory.runtime.free", runtime, Runtime::freeMemory)
                .description("The amount of free memory in the Java Virtual Machine.")
                .register(registry);
        Gauge.builder("jvm.memory.runtime.total", runtime, Runtime::totalMemory)
                .description("The total amount of memory in the Java virtual machine.")
                .register(registry);

        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();

        Gauge.builder("system.loadavg", os, o -> o.getSystemLoadAverage() * 100)
                .description("The system load average for the last minute, multiplied by 100.")
                .register(registry);
        Gauge.builder("system.processors", os, OperatingSystemMXBean::getAvailableProcessors)
                .description("The number of processors available to the Java virtual machine.")
                .register(registry);
        Gauge.builder("system.loadPerNmbrOfCores", os,
                o -> ((long) o.getSystemLoadAverage() * 100) / (long) o.getAvailableProcessors())
                .register(registry);

        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        Gauge.builder("jvm.threads.count", threadMXBean, ThreadMXBean::getThreadCount)
                .description("The current number of live threads including both daemon and non-daemon threads.")
                .register(registry);

        for (final Thread.State state : Thread.State.values()) {
            Gauge.builder("jvm.threads." + state.name().toLowerCase(), threadMXBean,
                    tbean -> getNumberOfThreadsWithState(tbean, state))
                    .description("The number of threads in '" + state.name() + "' state")
                    .register(registry);
        }
    }

    private long getNumberOfThreadsWithState(final ThreadMXBean threadMXBean, final Thread.State state) {
        return Arrays.stream(threadMXBean.getThreadInfo(threadMXBean.getAllThreadIds()))
                .filter(t -> state.equals(t.getThreadState()))
                .count();
    }
}
