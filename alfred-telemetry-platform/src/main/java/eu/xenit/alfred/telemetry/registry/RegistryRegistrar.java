package eu.xenit.alfred.telemetry.registry;


import eu.xenit.alfred.telemetry.MeterRegistryCustomizer;
import eu.xenit.alfred.telemetry.util.LambdaSafe;
import eu.xenit.alfred.telemetry.util.VersionUtil;
import eu.xenit.alfred.telemetry.util.VersionUtil.Version;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * {@link InitializingBean} that processes {@link RegistryFactoryWrapper}'s and registers the corresponding {@link
 * MeterRegistry}, retrieved via the {@link RegistryFactory}, into the {@link Metrics#globalRegistry global meter
 * registry} if the registry {@link RegistryFactoryWrapper#isRegistryEnabled() is enabled} and the registry {@link
 * RegistryFactoryWrapper#isRegistryAvailableOnClassPath() is available on the classpath}
 */
public class RegistryRegistrar implements InitializingBean, ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistryRegistrar.class);

    private CompositeMeterRegistry globalMeterRegistry;
    private ApplicationContext ctx;

    public RegistryRegistrar(CompositeMeterRegistry globalMeterRegistry) {
        this.globalMeterRegistry = globalMeterRegistry;
    }

    @Override
    public void afterPropertiesSet() {
        ctx.getBeansOfType(RegistryFactoryWrapper.class).values().forEach(this::processRegistryFactoryWrapper);
    }

    private void processRegistryFactoryWrapper(RegistryFactoryWrapper factoryWrapper) {

        if (!factoryWrapper.isRegistryEnabled()) {
            LOGGER.debug("Micrometer '{}' registry is not enabled", factoryWrapper.getRegistryClass());
            return;
        }

        if (!factoryWrapper.isRegistryAvailableOnClassPath()) {
            LOGGER.debug("Micrometer '{}' registry is not available on the classpath",
                    factoryWrapper.getRegistryClass());
            return;
        }

        final Version coreVersion = VersionUtil.getMicrometerCoreVersion();
        final Version registryVersion = factoryWrapper.getRegistryVersion();
        if (coreVersion != null && registryVersion != null && !coreVersion.isCompatible(registryVersion)) {
            LOGGER.warn(
                    "{} version ('{}') is incompatible with the micrometer-core version ('{}') and will not be registered",
                    factoryWrapper.getRegistryClass(),
                    registryVersion,
                    coreVersion);
            return;
        }

        final MeterRegistry registry = factoryWrapper.getRegistryFactory().createRegistry();
        this.customize(registry);
        this.addFilters(registry);

        globalMeterRegistry.add(registry);
        LOGGER.info("Registered Micrometer registry '{}'", factoryWrapper.getRegistryClass());

        this.incrementRegistryCounter();
    }

    @SuppressWarnings("unchecked")
    private void customize(final MeterRegistry registry) {
        LambdaSafe.callbacks(MeterRegistryCustomizer.class, ctx.getBeansOfType(MeterRegistryCustomizer.class).values(),
                registry)
                .withLogger(this.getClass())
                .invoke((c) -> c.customize(registry));
    }

    private void addFilters(final MeterRegistry registry) {
        ctx.getBeansOfType(MeterFilter.class).values().forEach(registry.config()::meterFilter);
    }

    private void incrementRegistryCounter() {
        Counter counter = globalMeterRegistry.counter("alfred.telemetry.registries");
        //noinspection ConstantConditions -> can be null in unit test scenarios
        if (counter != null) {
            counter.increment();
        } else {
            LOGGER.warn("Unable to retrieve 'alfred.telemetry.registries' counter from global Micrometer registry");
        }

    }

    @Override
    public void setApplicationContext(@Nonnull ApplicationContext applicationContext) throws BeansException {
        this.ctx = applicationContext;
    }
}
