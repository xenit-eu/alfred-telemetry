package eu.xenit.alfred.telemetry.registry.jmx;

import eu.xenit.alfred.telemetry.registry.RegistryFactory;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.jmx.JmxMeterRegistry;

public class JmxRegistryFactory implements RegistryFactory {

    @Override
    public MeterRegistry createRegistry() {
        return new JmxMeterRegistry(io.micrometer.jmx.JmxConfig.DEFAULT, Clock.SYSTEM);
    }
    
}
