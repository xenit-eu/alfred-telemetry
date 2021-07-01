package eu.xenit.alfred.telemetry.binder.care4alf;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import javax.annotation.Nonnull;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.admin.RepoAdminService;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.license.LicenseDescriptor;

/**
 * Mostly copy pasta of the Care4Alf LicenceMetric class for legacy support.
 *
 */
public class LegacyLicenseMetrics implements MeterBinder {
    private DescriptorService descriptorService;
    private RepoAdminService repoAdminService;

    public LegacyLicenseMetrics(DescriptorService descriptorService, RepoAdminService repoAdminService) {
        this.descriptorService = descriptorService;
        this.repoAdminService = repoAdminService;
    }

    @Override
    public void bindTo(@Nonnull MeterRegistry registry) {
        Gauge.builder("license.users.max", descriptorService, this::getMaxUsers)
                .description("The max amount of users")
                .register(registry);

        Gauge.builder("license.users.authorized", repoAdminService, this::getAuthorizedUsers)
                .description("The amount of authorized users")
                .register(registry);

        Gauge.builder("license.valid", descriptorService, this::getRemainingDays)
                .description("The amount of days still remaining")
                .register(registry);
    }

    protected long getRemainingDays(final DescriptorService descriptorService) {
        LicenseDescriptor licenseDescriptor = descriptorService.getLicenseDescriptor();
        if (licenseDescriptor != null && licenseDescriptor.getRemainingDays() != null) {
            return licenseDescriptor.getRemainingDays();
        }
        return -255L;
    }

    protected long getMaxUsers(final DescriptorService descriptorService) {
        LicenseDescriptor licenseDescriptor = descriptorService.getLicenseDescriptor();
        if (licenseDescriptor != null && licenseDescriptor.getMaxUsers() != null) {
            return licenseDescriptor.getMaxUsers();
        }
        return -255L;
    }

    protected long getAuthorizedUsers(final RepoAdminService repoAdminService) {
        return AuthenticationUtil.runAsSystem(() -> repoAdminService.getUsage().getUsers());
    }
}