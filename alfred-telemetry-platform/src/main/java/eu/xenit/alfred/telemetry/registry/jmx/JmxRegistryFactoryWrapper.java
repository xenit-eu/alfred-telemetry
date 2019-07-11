package eu.xenit.alfred.telemetry.registry.jmx;

import eu.xenit.alfred.telemetry.registry.RegistryFactory;
import eu.xenit.alfred.telemetry.registry.RegistryFactoryWrapper;

public class JmxRegistryFactoryWrapper implements RegistryFactoryWrapper {

    private JmxConfig config;

    public JmxRegistryFactoryWrapper(JmxConfig config) {
        this.config = config;
    }

    @Override
    public boolean isRegistryEnabled() {
        return config.isEnabled();
    }

    @Override
    public String getRegistryClass() {
        return "io.micrometer.jmx.JmxMeterRegistry";
    }

    @Override
    public RegistryFactory getRegistryFactory() {
        return new JmxRegistryFactory();
    }
}
