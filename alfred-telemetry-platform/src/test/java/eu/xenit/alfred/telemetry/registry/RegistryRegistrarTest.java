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
import java.util.Collections;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

@ExtendWith(MockitoExtension.class)
class RegistryRegistrarTest {

    @Mock
    private CompositeMeterRegistry meterRegistry;

    @Mock
    private ApplicationContext applicationContext;

    private RegistryRegistrar registrar;

    @BeforeEach
    void setup() {
        registrar = new RegistryRegistrar(meterRegistry);
        registrar.setApplicationContext(applicationContext);
    }

    @Test
    void dontRegisterIfVersionIfIncompatibleRegistryVersion() {
        RegistryFactoryWrapper factoryWrapper = mock(RegistryFactoryWrapper.class);
        when(factoryWrapper.isRegistryEnabled()).thenReturn(true);
        when(factoryWrapper.isRegistryAvailableOnClassPath()).thenReturn(true);

        when(factoryWrapper.getRegistryVersion())
                .thenReturn(Version.fromString("101.12.07"));

        when(applicationContext.getBeansOfType(RegistryFactoryWrapper.class))
                .thenReturn(Collections.singletonMap("factoryWrapper", factoryWrapper));

        registrar.afterPropertiesSet();

        verify(meterRegistry, never()).add(any(MeterRegistry.class));
    }

    @Test
    void dontRegisterIfRegistryEnabled() {
        RegistryFactoryWrapper factoryWrapper = mock(RegistryFactoryWrapper.class);
        when(factoryWrapper.isRegistryEnabled()).thenReturn(false);

        when(applicationContext.getBeansOfType(RegistryFactoryWrapper.class))
                .thenReturn(Collections.singletonMap("factoryWrapper", factoryWrapper));

        registrar.afterPropertiesSet();

        verify(meterRegistry, never()).add(any(MeterRegistry.class));
    }

    @Test
    void dontRegisterIfRegistryNotOnClasspath() {
        RegistryFactoryWrapper factoryWrapper = mock(RegistryFactoryWrapper.class);
        when(factoryWrapper.isRegistryEnabled()).thenReturn(true);
        when(factoryWrapper.isRegistryAvailableOnClassPath()).thenReturn(false);

        when(applicationContext.getBeansOfType(RegistryFactoryWrapper.class))
                .thenReturn(Collections.singletonMap("factoryWrapper", factoryWrapper));

        registrar.afterPropertiesSet();

        verify(meterRegistry, never()).add(any(MeterRegistry.class));
    }

    @Test
    void register() {
        RegistryFactoryWrapper factoryWrapper = mock(RegistryFactoryWrapper.class);
        when(factoryWrapper.isRegistryEnabled()).thenReturn(true);
        when(factoryWrapper.isRegistryAvailableOnClassPath()).thenReturn(true);
        when(factoryWrapper.getRegistryFactory()).thenReturn(SimpleMeterRegistry::new);

        when(applicationContext.getBeansOfType(RegistryFactoryWrapper.class))
                .thenReturn(Collections.singletonMap("factoryWrapper", factoryWrapper));

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

        when(applicationContext.getBeansOfType(RegistryFactoryWrapper.class))
                .thenReturn(Collections.singletonMap("factoryWrapper", factoryWrapper));

        registrar.afterPropertiesSet();

        verify(meterRegistry).add(any(SimpleMeterRegistry.class));
    }
}
