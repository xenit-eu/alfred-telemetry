package eu.xenit.alfred.telemetry.binder.care4alf;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.alfresco.service.cmr.admin.RepoAdminService;
import org.alfresco.service.cmr.admin.RepoUsage;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.license.LicenseDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class LegacyLicenseMetricsTest {
    @Mock
    private DescriptorService descriptorService;
    @Mock
    private RepoAdminService repoAdminService;
    @Mock
    private LicenseDescriptor licenseDescriptor;
    @Mock
    private RepoUsage repoUsage;

    private MeterRegistry meterRegistry;
    @InjectMocks
    private LegacyLicenseMetrics legacyLicenseMetrics;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        when(descriptorService.getLicenseDescriptor()).thenReturn(licenseDescriptor);
        when(repoAdminService.getUsage()).thenReturn(repoUsage);
        legacyLicenseMetrics = new LegacyLicenseMetrics(descriptorService, repoAdminService);
        meterRegistry = new SimpleMeterRegistry();
    }

    @Test
    public void testMaxUsersIs500() {
        when(licenseDescriptor.getMaxUsers()).thenReturn(500L);
        legacyLicenseMetrics.bindTo(meterRegistry);
        assertEquals(500L, meterRegistry.get("license.users.max").gauge().value());
    }

    @Test
    public void testMaxUsersIsNull() {
        when(licenseDescriptor.getMaxUsers()).thenReturn(null);
        legacyLicenseMetrics.bindTo(meterRegistry);
        assertEquals(-255L, meterRegistry.get("license.users.max").gauge().value());
    }

    @Test
    public void testRemainingDaysIsOneYear() {
        when(licenseDescriptor.getRemainingDays()).thenReturn(365);
        legacyLicenseMetrics.bindTo(meterRegistry);
        assertEquals(365, meterRegistry.get("license.valid").gauge().value());
    }

    @Test
    public void testRemainingDaysNull() {
        when(licenseDescriptor.getRemainingDays()).thenReturn(null);
        legacyLicenseMetrics.bindTo(meterRegistry);
        assertEquals(-255L, meterRegistry.get("license.valid").gauge().value());
    }

    @Test
    public void testAuthorizedUsersIs200() {
        when(repoUsage.getUsers()).thenReturn(200L);
        legacyLicenseMetrics.bindTo(meterRegistry);
        assertEquals(200L, meterRegistry.get("license.users.authorized").gauge().value());
    }

    @Test
    public void testAuthorizedUsersIsNull() {
        when(repoUsage.getUsers()).thenReturn(null);
        legacyLicenseMetrics.bindTo(meterRegistry);
        assertEquals(0L, meterRegistry.get("license.valid").gauge().value());
    }
}