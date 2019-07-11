package eu.xenit.alfred.telemetry.registry;

import io.micrometer.core.instrument.MeterRegistry;

public interface RegistryFactory {

    MeterRegistry createRegistry();

}
