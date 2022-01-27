package eu.xenit.alfred.telemetry.registry.simple;

import static eu.xenit.alfred.telemetry.util.MicrometerModules.MODULE_MICROMETER_CORE;

import eu.xenit.alfred.telemetry.registry.RegistryFactory;
import eu.xenit.alfred.telemetry.registry.RegistryFactoryWrapper;
import eu.xenit.alfred.telemetry.util.MicrometerModules;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class SimpleRegistryFactoryWrapper implements RegistryFactoryWrapper {

    private SimpleRegistryConfig config;

    public SimpleRegistryFactoryWrapper(SimpleRegistryConfig config) {
        this.config = config;
    }

    @Override
    public boolean isRegistryEnabled() {
        return config.isEnabled();
    }

    @Override
    public String getRegistryClass() {
        return "io.micrometer.core.instrument.simple.SimpleMeterRegistry";
    }

    /**
     * Override because {@link SimpleMeterRegistry} doesn't have it's own Micrometer module, but is available in the
     * Micrometer core module.
     *
     * @return the name of the Micrometer core module which includes the {@link SimpleMeterRegistry}, being {@link
     * MicrometerModules#MODULE_MICROMETER_CORE}
     */
    @Override
    public String getModuleName() {
        return MODULE_MICROMETER_CORE;
    }

    @Override
    public RegistryFactory getRegistryFactory() {
        return new SimpleRegistryFactory();
    }
}
