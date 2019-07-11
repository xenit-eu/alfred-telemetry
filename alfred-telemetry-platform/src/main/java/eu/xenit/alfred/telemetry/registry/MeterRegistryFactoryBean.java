package eu.xenit.alfred.telemetry.registry;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import javax.annotation.Nonnull;
import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
 * {@link org.springframework.beans.factory.FactoryBean} to make the {@link Metrics#globalRegistry global Micrometer
 * MeterRegistry} available as bean.
 */
public class MeterRegistryFactoryBean extends AbstractFactoryBean<MeterRegistry> {

    @Override
    public Class<?> getObjectType() {
        return MeterRegistry.class;
    }

    @Override
    @Nonnull
    protected MeterRegistry createInstance() {
        return Metrics.globalRegistry;
    }
}
