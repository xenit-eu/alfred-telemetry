package eu.xenit.alfred.telemetry.binder;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.alfresco.service.descriptor.Descriptor;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.license.LicenseDescriptor;
import org.alfresco.service.license.LicenseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nonnull;

public class LicenseMetrics implements MeterBinder, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(LicenseMetrics.class);

    private static final String METRIC_NAME_LICENSE = "license";

    private ApplicationContext ctx;
    private DescriptorService descriptorService;

    public LicenseMetrics(DescriptorService descriptorService) {
        this.descriptorService = descriptorService;
    }

    @Override
    public void bindTo(@Nonnull MeterRegistry registry) {
        // do not do anything for Community
        Descriptor serverDescriptor = descriptorService.getServerDescriptor();
        if(!"Enterprise".equals(serverDescriptor.getEdition())) {
            logger.info("Edition={}, license metrics are not available", serverDescriptor.getEdition());
            return;
        }

        Gauge.builder(METRIC_NAME_LICENSE + ".valid", ctx, LicenseMetrics::getValid)
                .description("Whether the license is still valid")
                .register(registry);

        Gauge.builder(METRIC_NAME_LICENSE + ".remainingDays", descriptorService, LicenseMetrics::getRemainingDays)
                .description("Remaining days")
                .register(registry);
        Gauge.builder(METRIC_NAME_LICENSE + ".maxDocs", descriptorService, LicenseMetrics::getMaxDocs)
                .description("Max docs")
                .register(registry);
        Gauge.builder(METRIC_NAME_LICENSE + ".maxUsers", descriptorService, LicenseMetrics::getMaxUsers)
                .description("Max users")
                .register(registry);
        Gauge.builder(METRIC_NAME_LICENSE + ".isClusterEnabled", descriptorService, LicenseMetrics::isClusterEnabled)
                .description("Clustering enabled")
                .register(registry);
        Gauge.builder(METRIC_NAME_LICENSE + ".isCryptodocEnabled", descriptorService, LicenseMetrics::isCryptodocEnabled)
                .description("Encription enabled")
                .register(registry);
        Gauge.builder(METRIC_NAME_LICENSE + ".isHeartbeatDisabled", descriptorService, LicenseMetrics::isHeartbeatDisabled)
                .description("Heartbeat disabled")
                .register(registry);
    }


    private static double getValid(final ApplicationContext ctx) {
        LicenseService licenseService = ctx.getBeansOfType(LicenseService.class, false, false).get("licenseService");
        if(licenseService!=null)
            return (licenseService.isLicenseValid() ? 1 : 0);
        return -1;
    }

    private static int getRemainingDays(final DescriptorService descriptorService) {
        LicenseDescriptor licenseDescriptor = descriptorService.getLicenseDescriptor();
        if(licenseDescriptor!=null && licenseDescriptor.getRemainingDays()!=null)
            return licenseDescriptor.getRemainingDays();
        return -1;
    }

    private static long getMaxDocs(final DescriptorService descriptorService) {
        LicenseDescriptor licenseDescriptor = descriptorService.getLicenseDescriptor();
        if(licenseDescriptor!=null && licenseDescriptor.getMaxDocs()!=null)
            return licenseDescriptor.getMaxDocs();
        return -1L;
    }

    private static long getMaxUsers(final DescriptorService descriptorService) {
        LicenseDescriptor licenseDescriptor = descriptorService.getLicenseDescriptor();
        if(licenseDescriptor!=null && licenseDescriptor.getMaxUsers()!=null)
            return licenseDescriptor.getMaxUsers();
        return -1L;
    }

    private static double isClusterEnabled(final DescriptorService descriptorService) {
        LicenseDescriptor licenseDescriptor = descriptorService.getLicenseDescriptor();
        if(licenseDescriptor!=null)
            return (licenseDescriptor.isClusterEnabled()?1:0);
        return -1;
    }

    private static double isCryptodocEnabled(final DescriptorService descriptorService) {
        LicenseDescriptor licenseDescriptor = descriptorService.getLicenseDescriptor();
        if(licenseDescriptor!=null)
            return (licenseDescriptor.isCryptodocEnabled()?1:0);
        return -1;
    }

    private static double isHeartbeatDisabled(final DescriptorService descriptorService) {
        LicenseDescriptor licenseDescriptor = descriptorService.getLicenseDescriptor();
        if(licenseDescriptor!=null)
            return (licenseDescriptor.isHeartBeatDisabled()?1:0);
        return -1;
    }

    @Override
    public void setApplicationContext(@Nonnull ApplicationContext applicationContext) throws BeansException {
        this.ctx = applicationContext;
    }
}
