package eu.xenit.alfred.telemetry.binder.solr.tracking;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.function.ToDoubleFunction;
import org.alfresco.repo.solr.SOLRTrackingComponentImpl;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.transaction.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolrTrackingMetrics {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolrTrackingMetrics.class);

    public static final String SOLR_METRICS_PREFIX = "solr.tracking";
    private TransactionService transactionService;

    private SOLRTrackingComponentImpl solrTrackingComponent;
    private MeterRegistry registry;

    public SolrTrackingMetrics(SOLRTrackingComponentImpl solrTrackingComponent, TransactionService transactionService,
            MeterRegistry registry) {
        this.solrTrackingComponent = solrTrackingComponent;
        this.transactionService = transactionService;
        this.registry = registry;
        registerMetrics();
    }

    private void registerMetrics() {
        LOGGER.info("Registering Solr metrics");
        registerSolrTrackingMetric("maxTxnId", SOLRTrackingComponentImpl::getMaxTxnId, "number");
        registerSolrTrackingMetric("maxTxnCommitTime", SOLRTrackingComponentImpl::getMaxTxnCommitTime, "timestamp");
        registerSolrTrackingMetric("maxChangeSetId", SOLRTrackingComponentImpl::getMaxChangeSetId, "number");
        registerSolrTrackingMetric("maxChangeSetCommitTime", SOLRTrackingComponentImpl::getMaxChangeSetCommitTime,
                "timestamp");
    }

    private void registerSolrTrackingMetric(String name, ToDoubleFunction<SOLRTrackingComponentImpl> function,
            String baseUnit) {
        ToDoubleFunction<SOLRTrackingComponentImpl> wrappedFunction = solrTrackingComponent1 -> {
            RetryingTransactionHelper retryingTransactionHelper = transactionService.getRetryingTransactionHelper();
            return retryingTransactionHelper
                    .doInTransaction(() -> function.applyAsDouble(solrTrackingComponent1), true);
        };
        Gauge.builder(String.format("%s.%s", SOLR_METRICS_PREFIX, name), solrTrackingComponent, wrappedFunction)
                .baseUnit(baseUnit)
                .register(registry);
    }

}
