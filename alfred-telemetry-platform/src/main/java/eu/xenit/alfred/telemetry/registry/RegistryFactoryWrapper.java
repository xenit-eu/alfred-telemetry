package eu.xenit.alfred.telemetry.registry;

import eu.xenit.alfred.telemetry.util.VersionUtil;
import eu.xenit.alfred.telemetry.util.VersionUtil.Version;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.util.ClassUtils;

/**
 * {@link RegistryFactory} wrapper which adds
 * <ul>
 * <li>capability to enable / disable the {@link RegistryFactory} and therefore also the corresponding {@link
 * MeterRegistry}</li>
 * <li>capability to check if the corresponding {@link MeterRegistry} is available on the classpath</li>
 * </ul>
 * This logic is put in a wrapper to prevent e.g. {@link NoClassDefFoundError}'s during Spring's classpath scanning.
 */
public interface RegistryFactoryWrapper {

    /**
     * @return {@code true} if the corresponding {@link MeterRegistry} is enabled and may be used.
     */
    boolean isRegistryEnabled();

    /**
     * @return the class name of the specific {@link MeterRegistry} implementation that is created by the corresponiding
     * {@link RegistryFactory}. Used
     */
    String getRegistryClass();

    default boolean isRegistryAvailableOnClassPath() {
        return ClassUtils.isPresent(getRegistryClass(), this.getClass().getClassLoader());
    }

    RegistryFactory getRegistryFactory();

    default Version getRegistryVersion() {
        return VersionUtil.getMicrometerModuleVersion(getModuleName());
    }

    /**
     * @return the name of the Micrometer module which includes this {@link MeterRegistry}
     */
    default String getModuleName() {
        final String registryName =
                getRegistryClass()
                        .substring(getRegistryClass().lastIndexOf('.') + 1)
                        .replace("MeterRegistry", "")
                        .toLowerCase();
        return "micrometer-registry-" + registryName;
    }

}
