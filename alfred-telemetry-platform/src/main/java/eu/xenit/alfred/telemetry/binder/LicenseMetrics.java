package eu.xenit.alfred.telemetry.binder;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import javax.annotation.Nonnull;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.admin.RepoAdminService;
import org.alfresco.service.descriptor.Descriptor;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.license.LicenseDescriptor;
import org.alfresco.service.license.LicenseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class LicenseMetrics implements MeterBinder, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(LicenseMetrics.class);

    private static final String METRIC_NAME_LICENSE = "license";

    private ApplicationContext ctx;
    private DescriptorService descriptorService;
    private RepoAdminService repoAdminService;

    public LicenseMetrics(DescriptorService descriptorService, RepoAdminService repoAdminService) {
        this.descriptorService = descriptorService;
        this.repoAdminService = repoAdminService;
    }

    @Override
    public void bindTo(@Nonnull MeterRegistry registry) {
        // do not do anything for Community
        Descriptor serverDescriptor = descriptorService.getServerDescriptor();
        if (!"Enterprise".equals(serverDescriptor.getEdition())) {
            logger.info("Edition={}, license metrics are not available", serverDescriptor.getEdition());
            return;
        }

        Gauge.builder(METRIC_NAME_LICENSE + ".valid", ctx, LicenseMetrics::getValid)
                .description("Whether the license is still valid")
                .register(registry);

        Gauge.builder(METRIC_NAME_LICENSE + ".days", descriptorService, LicenseMetrics::getRemainingDays)
                .description("Remaining days")
                .tags("status", "remaining")
                .register(registry);
        Gauge.builder(METRIC_NAME_LICENSE + ".docs.max", descriptorService, LicenseMetrics::getMaxDocs)
                .description("Max docs")
                .register(registry);
        Gauge.builder(METRIC_NAME_LICENSE + ".users.max", descriptorService, LicenseMetrics::getMaxUsers)
                .description("Max users")
                .register(registry);
        Gauge.builder(METRIC_NAME_LICENSE + ".users", repoAdminService, LicenseMetrics::getAuthorizedUsers)
                .description("Authorized users")
                .tags("status", "current")
                .register(registry);
        Gauge.builder(METRIC_NAME_LICENSE + ".cluster.enabled", descriptorService, LicenseMetrics::isClusterEnabled)
                .description("Clustering enabled")
                .register(registry);
        Gauge.builder(METRIC_NAME_LICENSE + ".encryption.enabled", descriptorService,
                LicenseMetrics::isCryptodocEnabled)
                .description("Encription enabled")
                .register(registry);
        Gauge.builder(METRIC_NAME_LICENSE + ".heartbeat.enabled", descriptorService, LicenseMetrics::isHeartbeatEnabled)
                .description("Heartbeat enabled")
                .register(registry);
    }


    private static double getValid(final ApplicationContext ctx) {
        LicenseService licenseService = ctx.getBeansOfType(LicenseService.class, false, false).get("licenseService");
        if (licenseService != null) {
            return (licenseService.isLicenseValid() ? 1 : 0);
        }
        return -1;
    }

    private static int getRemainingDays(final DescriptorService descriptorService) {
        LicenseDescriptor licenseDescriptor = descriptorService.getLicenseDescriptor();
        if (licenseDescriptor != null && licenseDescriptor.getRemainingDays() != null) {
            return licenseDescriptor.getRemainingDays();
        }
        return -1;
    }

    private static long getMaxDocs(final DescriptorService descriptorService) {
        LicenseDescriptor licenseDescriptor = descriptorService.getLicenseDescriptor();
        if (licenseDescriptor != null && licenseDescriptor.getMaxDocs() != null) {
            return licenseDescriptor.getMaxDocs();
        }
        return -1L;
    }

    private static long getMaxUsers(final DescriptorService descriptorService) {
        LicenseDescriptor licenseDescriptor = descriptorService.getLicenseDescriptor();
        if (licenseDescriptor != null && licenseDescriptor.getMaxUsers() != null) {
            return licenseDescriptor.getMaxUsers();
        }
        return -1L;
    }

    private static long getAuthorizedUsers(final RepoAdminService repoAdminService) {
        return AuthenticationUtil.runAsSystem(() -> repoAdminService.getUsage().getUsers());
    }


    private static double isClusterEnabled(final DescriptorService descriptorService) {
        LicenseDescriptor licenseDescriptor = descriptorService.getLicenseDescriptor();
        if (licenseDescriptor != null) {
            return (licenseDescriptor.isClusterEnabled() ? 1 : 0);
        }
        return -1;
    }

    private static double isCryptodocEnabled(final DescriptorService descriptorService) {
        LicenseDescriptor licenseDescriptor = descriptorService.getLicenseDescriptor();
        if (licenseDescriptor != null) {
            return (licenseDescriptor.isCryptodocEnabled() ? 1 : 0);
        }
        return -1;
    }

    private static double isHeartbeatEnabled(final DescriptorService descriptorService) {
        LicenseDescriptor licenseDescriptor = descriptorService.getLicenseDescriptor();
        if (licenseDescriptor != null) {
            return (licenseDescriptor.isHeartBeatDisabled() ? 0 : 1);
        }
        return -1;
    }

    @Override
    public void setApplicationContext(@Nonnull ApplicationContext applicationContext) throws BeansException {
        this.ctx = applicationContext;
    }
}
