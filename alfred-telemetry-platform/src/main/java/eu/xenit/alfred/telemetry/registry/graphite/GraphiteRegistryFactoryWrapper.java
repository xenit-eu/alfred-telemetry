package eu.xenit.alfred.telemetry.registry.graphite;

import eu.xenit.alfred.telemetry.registry.RegistryFactory;
import eu.xenit.alfred.telemetry.registry.RegistryFactoryWrapper;
import org.springframework.util.StringUtils;

public class GraphiteRegistryFactoryWrapper implements RegistryFactoryWrapper {

    private GraphiteConfig config;

    public GraphiteRegistryFactoryWrapper(GraphiteConfig config) {
        this.config = config;
    }

    @Override
    public boolean isRegistryEnabled() {
        return config.isEnabled();
    }

    @Override
    public String getRegistryClass() {
        return "io.micrometer.graphite.GraphiteMeterRegistry";
    }

    @Override
    public RegistryFactory getRegistryFactory() {
        return new GraphiteRegistryFactory(config);
    }
}
