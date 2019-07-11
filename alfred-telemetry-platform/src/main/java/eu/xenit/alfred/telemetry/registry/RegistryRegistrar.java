package eu.xenit.alfred.telemetry.registry;


import eu.xenit.alfred.telemetry.util.VersionUtil;
import eu.xenit.alfred.telemetry.util.VersionUtil.Version;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * {@link InitializingBean} that processes {@link RegistryFactoryWrapper}'s and registers the corresponding {@link
 * MeterRegistry}, retrieved via the {@link RegistryFactory}, into the {@link Metrics#globalRegistry global meter
 * registry} if the registry {@link RegistryFactoryWrapper#isRegistryEnabled() is enabled} and the registry {@link
 * RegistryFactoryWrapper#isRegistryAvailableOnClassPath() is available on the classpath}
 */
public class RegistryRegistrar implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistryRegistrar.class);

    private CompositeMeterRegistry globalMeterRegistry;

    private List<RegistryFactoryWrapper> registryFactoryWrappers;

    public RegistryRegistrar(CompositeMeterRegistry globalMeterRegistry, RegistryFactoryWrapper... wrappers) {
        this.globalMeterRegistry = globalMeterRegistry;
        this.registryFactoryWrappers = Arrays.asList(wrappers);
    }

    @Override
    public void afterPropertiesSet() {
        registryFactoryWrappers.forEach(this::processRegistryFactoryWrapper);
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

        globalMeterRegistry.add(factoryWrapper.getRegistryFactory().createRegistry());
        LOGGER.info("Registered Micrometer registry '{}'", factoryWrapper.getRegistryClass());

        this.incrementRegistryCounter();
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
}
