package eu.xenit.alfred.telemetry.service;

import eu.xenit.alfred.telemetry.util.MeterRegistryUtil;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public class MeterRegistryService {

    private final MeterRegistry meterRegistry;

    public MeterRegistryService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public Set<String> getMeterNames() {
        TreeSet<String> names = new TreeSet<>();
        this.collectNames(names, meterRegistry);
        return names;
    }

    public Collection<Meter> findFirstMatchingMeters(final String name, final Iterable<Tag> tags) {
        return MeterRegistryUtil.findFirstMatchingMeters(meterRegistry, name, tags);
    }

    /* PRIVATE METHODS */

    private void collectNames(Set<String> names, MeterRegistry registry) {
        if (registry instanceof CompositeMeterRegistry) {
            ((CompositeMeterRegistry) registry).getRegistries().forEach((member) -> collectNames(names, member));
        } else {
            registry.getMeters().stream().map(this::getName).forEach(names::add);
        }
    }

    private String getName(Meter meter) {
        return meter.getId().getName();
    }
}
