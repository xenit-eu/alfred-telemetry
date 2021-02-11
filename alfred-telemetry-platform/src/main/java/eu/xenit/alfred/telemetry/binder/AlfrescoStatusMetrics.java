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

    public AlfrescoStatusMetrics(RepoAdminService repoAdminService, RetryingTransactionHelper retryingTransactionHelper) {
        this.repoAdminService = repoAdminService;
        this.retryingTransactionHelper = retryingTransactionHelper;
    }

    @Override
    public void bindTo(@Nonnull MeterRegistry meterRegistry) {
        LOGGER.info("Registering Alfresco Status metrics");
        Gauge.builder(String.format("%s.%s", STATUS_PREFIX, "readonly"),
                repoAdminService,
                x -> getReadOnly(repoAdminService))
                .description("Metric about Alfresco being in read-only mode")
                .register(meterRegistry);

    }

    private double getReadOnly(RepoAdminService repoAdminService) {
        final boolean[] isReadOnly = {false};

        retryingTransactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {

            @Override
            public Object execute() throws Throwable {
                AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {

                    @Override
                    public Object doWork() throws Exception {
                        isReadOnly[0] = repoAdminService.getUsage().isReadOnly();

                        return null;
                    }
                }, AuthenticationUtil.getAdminUserName());
                return null;
            }
        },true);

        if(isReadOnly[0])
            return 1d;
        else
            return 0d;
    }
}