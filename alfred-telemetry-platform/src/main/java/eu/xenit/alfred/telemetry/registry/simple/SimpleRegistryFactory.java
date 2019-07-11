package eu.xenit.alfred.telemetry.registry.simple;

import eu.xenit.alfred.telemetry.registry.RegistryFactory;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class SimpleRegistryFactory implements RegistryFactory {

    @Override
    public MeterRegistry createRegistry() {
        return new SimpleMeterRegistry();
    }
}
