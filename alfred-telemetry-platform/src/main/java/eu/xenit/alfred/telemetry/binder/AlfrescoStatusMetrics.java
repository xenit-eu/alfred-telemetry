package eu.xenit.alfred.telemetry.binder;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.admin.RepoAdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

public class AlfrescoStatusMetrics implements MeterBinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlfrescoStatusMetrics.class);
    private static final String STATUS_PREFIX = "alfresco.status";

    private RepoAdminService repoAdminService;
    private RetryingTransactionHelper retryingTransactionHelper;

    public AlfrescoStatusMetrics(RepoAdminService repoAdminService,
            RetryingTransactionHelper retryingTransactionHelper) {
        this.repoAdminService = repoAdminService;
        this.retryingTransactionHelper = retryingTransactionHelper;
    }

    @Override
    public void bindTo(@Nonnull MeterRegistry meterRegistry) {
        LOGGER.info("Registering Alfresco Status metrics");
        Gauge.builder(STATUS_PREFIX + ".readonly", repoAdminService, this::getReadOnly)
                .description("Metric about Alfresco being in read-only mode")
                .register(meterRegistry);

    }

    private double getReadOnly(RepoAdminService repoAdminService) {
        final boolean[] isReadOnly = {false};

        retryingTransactionHelper.doInTransaction(() ->
                        AuthenticationUtil.runAsSystem(() ->
                                isReadOnly[0] = repoAdminService.getUsage().isReadOnly()),
                true);

        if (isReadOnly[0]) {
            return 1d;
        } else {
            return 0d;
        }
    }
}
