package eu.xenit.alfred.telemetry.registry;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.xenit.alfred.telemetry.service.VersionUtilTest;
import eu.xenit.alfred.telemetry.util.VersionUtil.Version;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegistryRegistrarTest {

    @Mock
    private CompositeMeterRegistry meterRegistry;

    @Test
    void dontRegisterIfVersionIfIncompatibleRegistryVersion() {
        RegistryFactoryWrapper factoryWrapper = mock(RegistryFactoryWrapper.class);
        when(factoryWrapper.isRegistryEnabled()).thenReturn(true);
        when(factoryWrapper.isRegistryAvailableOnClassPath()).thenReturn(true);

        when(factoryWrapper.getRegistryVersion())
                .thenReturn(Version.fromString("101.12.07"));

        RegistryRegistrar registrar = new RegistryRegistrar(meterRegistry, factoryWrapper);
        registrar.afterPropertiesSet();

        verify(meterRegistry, never()).add(any(MeterRegistry.class));
    }

    @Test
    void dontRegisterIfRegistryEnabled() {
        RegistryFactoryWrapper factoryWrapper = mock(RegistryFactoryWrapper.class);
        when(factoryWrapper.isRegistryEnabled()).thenReturn(false);

        RegistryRegistrar registrar = new RegistryRegistrar(meterRegistry, factoryWrapper);
        registrar.afterPropertiesSet();

        verify(meterRegistry, never()).add(any(MeterRegistry.class));
    }

    @Test
    void dontRegisterIfRegistryNotOnClasspath() {
        RegistryFactoryWrapper factoryWrapper = mock(RegistryFactoryWrapper.class);
        when(factoryWrapper.isRegistryEnabled()).thenReturn(true);
        when(factoryWrapper.isRegistryAvailableOnClassPath()).thenReturn(false);

        RegistryRegistrar registrar = new RegistryRegistrar(meterRegistry, factoryWrapper);
        registrar.afterPropertiesSet();

        verify(meterRegistry, never()).add(any(MeterRegistry.class));
    }

    @Test
    void register() {
        RegistryFactoryWrapper factoryWrapper = mock(RegistryFactoryWrapper.class);
        when(factoryWrapper.isRegistryEnabled()).thenReturn(true);
        when(factoryWrapper.isRegistryAvailableOnClassPath()).thenReturn(true);
        when(factoryWrapper.getRegistryFactory()).thenReturn(SimpleMeterRegistry::new);

        RegistryRegistrar registrar = new RegistryRegistrar(meterRegistry, factoryWrapper);
        registrar.afterPropertiesSet();

        verify(meterRegistry).add(any(SimpleMeterRegistry.class));
    }

    @Test
    void register_verifyVersions() {
        RegistryFactoryWrapper factoryWrapper = mock(RegistryFactoryWrapper.class);
        when(factoryWrapper.isRegistryEnabled()).thenReturn(true);
        when(factoryWrapper.isRegistryAvailableOnClassPath()).thenReturn(true);
        when(factoryWrapper.getRegistryFactory()).thenReturn(SimpleMeterRegistry::new);
        when(factoryWrapper.getRegistryVersion()).thenReturn(VersionUtilTest.getMicrometerVersionFromGradle());

        RegistryRegistrar registrar = new RegistryRegistrar(meterRegistry, factoryWrapper);
        registrar.afterPropertiesSet();

        verify(meterRegistry).add(any(SimpleMeterRegistry.class));
    }
}
