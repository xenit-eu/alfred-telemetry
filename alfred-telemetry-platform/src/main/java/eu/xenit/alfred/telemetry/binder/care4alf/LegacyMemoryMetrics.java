package eu.xenit.alfred.telemetry.binder.care4alf;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import javax.annotation.Nonnull;

public class LegacyMemoryMetrics implements MeterBinder {

    @Override
    public void bindTo(@Nonnull MeterRegistry registry) {

        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = memBean.getHeapMemoryUsage();
        MemoryUsage nonHeap = memBean.getNonHeapMemoryUsage();

        // Retrieve the four values stored within MemoryUsage:
        // init: Amount of memory in bytes that the JVM initially requests from the OS.
        // used: Amount of memory used.
        // committed: Amount of memory that is committed for the JVM to use.
        // max: Maximum amount of memory that can be used for memory management.

        Gauge.builder("jvm.memory.heap.init", heap, MemoryUsage::getInit)
                .register(registry);
        Gauge.builder("jvm.memory.heap.used", heap, MemoryUsage::getUsed)
                .register(registry);
        Gauge.builder("jvm.memory.heap.committed", heap, MemoryUsage::getCommitted)
                .register(registry);
        Gauge.builder("jvm.memory.heap.max", heap, MemoryUsage::getMax)
                .register(registry);

        Gauge.builder("jvm.memory.nonheap.init", nonHeap, MemoryUsage::getInit)
                .register(registry);
        Gauge.builder("jvm.memory.nonheap.used", nonHeap, MemoryUsage::getUsed)
                .register(registry);
        Gauge.builder("jvm.memory.nonheap.committed", nonHeap, MemoryUsage::getCommitted)
                .register(registry);
        Gauge.builder("jvm.memory.nonheap.max", nonHeap, MemoryUsage::getMax)
                .register(registry);

        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            String name;
            String poolName = pool.getName().toLowerCase();
            if (poolName.contains("eden")) {
                name = "eden";
            } else if (poolName.contains("survivor")) {
                name = "survivor";
            } else if (poolName.contains("old")) {
                name = "old";
            } else {
                name = poolName.toLowerCase().replace(" ", "");
            }

            Gauge.builder("jvm.memory." + name + ".used", pool, p -> p.getUsage().getUsed())
                    .baseUnit("bytes")
                    .register(registry);
            Gauge.builder("jvm.memory." + name + ".max", pool, p -> p.getUsage().getMax())
                    .baseUnit("bytes")
                    .register(registry);
        }
    }
}
