package eu.xenit.alfred.telemetry.binder.solr.tracking;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.function.ToDoubleFunction;
import org.alfresco.repo.solr.SOLRTrackingComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.transaction.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolrTrackingMetrics {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolrTrackingMetrics.class);

    public static final String SOLR_METRICS_PREFIX = "solr.tracking";
    private TransactionService transactionService;

    private SOLRTrackingComponent solrTrackingComponent;
    private MeterRegistry registry;

    public SolrTrackingMetrics(SOLRTrackingComponent solrTrackingComponent, TransactionService transactionService,
            MeterRegistry registry, boolean enabled) {
        this.solrTrackingComponent = solrTrackingComponent;
        this.transactionService = transactionService;
        this.registry = registry;
        if (enabled) {
            registerMetrics();
        }
    }

    private void registerMetrics() {
        LOGGER.info("Registering Solr metrics");
        registerSolrTrackingMetric("maxTxnId", SOLRTrackingComponent::getMaxTxnId, "number");
        registerSolrTrackingMetric("maxTxnCommitTime", SOLRTrackingComponent::getMaxTxnCommitTime, "timestamp");
        registerSolrTrackingMetric("maxChangeSetId", SOLRTrackingComponent::getMaxChangeSetId, "number");
        registerSolrTrackingMetric("maxChangeSetCommitTime", SOLRTrackingComponent::getMaxChangeSetCommitTime,
                "timestamp");
    }

    private void registerSolrTrackingMetric(String name, ToDoubleFunction<SOLRTrackingComponent> function,
            String baseUnit) {
        ToDoubleFunction<SOLRTrackingComponent> wrappedFunction = solrTrackingComponent1 -> {
            RetryingTransactionHelper retryingTransactionHelper = transactionService.getRetryingTransactionHelper();
            return retryingTransactionHelper
                    .doInTransaction(() -> function.applyAsDouble(solrTrackingComponent1), true);
        };
        Gauge.builder(String.format("%s.%s", SOLR_METRICS_PREFIX, name), solrTrackingComponent, wrappedFunction)
                .baseUnit(baseUnit)
                .register(registry);
    }

}
