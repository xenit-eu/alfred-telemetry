package eu.xenit.alfred.telemetry.binder;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.alfresco.service.descriptor.Descriptor;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.license.LicenseDescriptor;
import org.alfresco.service.license.LicenseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LicenseMetricsTest {

    private DescriptorService descriptorService;
    private LicenseMetrics licenseMetrics;
    private MeterRegistry meterRegistry;
    private ApplicationContext applicationContext;
    private LicenseService licenseService;
    private Descriptor serverDescriptor;
    private LicenseDescriptor licenseDescriptor;

    @BeforeEach
    void setup() {
        licenseService = mock(LicenseService.class);
        applicationContext = mock(ApplicationContext.class);
        descriptorService = mock(DescriptorService.class);
        serverDescriptor = mock(Descriptor.class);
        licenseDescriptor = mock(LicenseDescriptor.class);

        meterRegistry = new SimpleMeterRegistry();
        licenseMetrics = new LicenseMetrics(descriptorService);
        licenseMetrics.setApplicationContext(applicationContext);

        when(descriptorService.getServerDescriptor()).thenReturn(serverDescriptor);
        when(descriptorService.getLicenseDescriptor()).thenReturn(licenseDescriptor);
        when(applicationContext.getBeansOfType(LicenseService.class,false,false)).thenReturn(Collections.singletonMap("licenseService", licenseService));
        when(serverDescriptor.getEdition()).thenReturn("Enterprise");
    }

    @Test
    public void testCommunity() {
        when(serverDescriptor.getEdition()).thenReturn("Community");
        licenseMetrics.bindTo(meterRegistry);
        assertFalse(meterRegistry.getMeters().contains("license.valid"));
    }

    @Test
    public void testNoLicenseService() {
        when(applicationContext.getBeansOfType(LicenseService.class,false,false)).thenReturn(Collections.singletonMap("licenseService", null));
        licenseMetrics.bindTo(meterRegistry);
        assertThat(meterRegistry.get("license.valid").gauge().value(),is(-1.0));
    }

    @Test
    public void testNoLicenseDescriptor() {
        when(descriptorService.getLicenseDescriptor()).thenReturn(null);
        licenseMetrics.bindTo(meterRegistry);
        assertThat(meterRegistry.get("license.maxDocs").gauge().value(),is(-1.0));
    }

    @Test
    public void testLicenseMetrics() {
        licenseMetrics.bindTo(meterRegistry);

        when(licenseService.isLicenseValid()).thenReturn(true);
        assertThat(meterRegistry.get("license.valid").gauge().value(),is(1.0));

        when(licenseService.isLicenseValid()).thenReturn(false);
        assertThat(meterRegistry.get("license.valid").gauge().value(),is(0.0));

        when(licenseDescriptor.getMaxDocs()).thenReturn(100L);
        assertThat(meterRegistry.get("license.maxDocs").gauge().value(),is(100.0));

        when(licenseDescriptor.getMaxDocs()).thenReturn(null);
        assertThat(meterRegistry.get("license.maxDocs").gauge().value(),is(-1.0));

        when(licenseDescriptor.getMaxUsers()).thenReturn(100L);
        assertThat(meterRegistry.get("license.maxUsers").gauge().value(),is(100.0));

        when(licenseDescriptor.getMaxUsers()).thenReturn(null);
        assertThat(meterRegistry.get("license.maxUsers").gauge().value(),is(-1.0));

        when(licenseDescriptor.getRemainingDays()).thenReturn(100);
        assertThat(meterRegistry.get("license.remainingDays").gauge().value(),is(100.0));

        when(licenseDescriptor.getRemainingDays()).thenReturn(null);
        assertThat(meterRegistry.get("license.remainingDays").gauge().value(),is(-1.0));

        when(licenseDescriptor.isClusterEnabled()).thenReturn(false);
        assertThat(meterRegistry.get("license.isClusterEnabled").gauge().value(),is(0.0));

        when(licenseDescriptor.isClusterEnabled()).thenReturn(true);
        assertThat(meterRegistry.get("license.isClusterEnabled").gauge().value(),is(1.0));

        when(licenseDescriptor.isCryptodocEnabled()).thenReturn(false);
        assertThat(meterRegistry.get("license.isCryptodocEnabled").gauge().value(),is(0.0));

        when(licenseDescriptor.isCryptodocEnabled()).thenReturn(true);
        assertThat(meterRegistry.get("license.isCryptodocEnabled").gauge().value(),is(1.0));

        when(licenseDescriptor.isHeartBeatDisabled()).thenReturn(false);
        assertThat(meterRegistry.get("license.isHeartbeatDisabled").gauge().value(),is(0.0));

        when(licenseDescriptor.isHeartBeatDisabled()).thenReturn(true);
        assertThat(meterRegistry.get("license.isHeartbeatDisabled").gauge().value(),is(1.0));
    }
}