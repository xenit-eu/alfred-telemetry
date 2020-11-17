package eu.xenit.alfred.telemetry.binder.solr.nodedao;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.function.ToDoubleFunction;

import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.transaction.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolrNodeDAOMetrics {

    private static final Logger LOGGER = LoggerFactory.getLogger(eu.xenit.alfred.telemetry.binder.solr.nodedao.SolrNodeDAOMetrics.class);

    public static final String SOLR_METRICS_PREFIX = "solr.nodedao";
    private TransactionService transactionService;

    private NodeDAO nodeDAO;
    private MeterRegistry registry;

    public SolrNodeDAOMetrics(NodeDAO nodeDAOComponent, TransactionService transactionService,
                               MeterRegistry registry, boolean enabled) {
        this.nodeDAO = nodeDAOComponent;
        this.transactionService = transactionService;
        this.registry = registry;
        if (enabled) {
            registerMetrics();
        }
    }

    private void registerMetrics() {
        LOGGER.info("Registering Solr metrics");
        registerSolrNodeDAOMetric("maxTxNode", NodeDAO::getMaxNodeId, "number");
        registerSolrNodeDAOMetric("maxTxnId", NodeDAO::getMaxTxnId, "number");
        registerSolrNodeDAOMetric("maxTxnCommitTime", NodeDAO::getMaxTxnCommitTime, "number");


    }

    private void registerSolrNodeDAOMetric(String name, ToDoubleFunction<NodeDAO> function,
                                            String baseUnit) {
        ToDoubleFunction<NodeDAO> wrappedFunction = nodeDAO1 -> {
            RetryingTransactionHelper retryingTransactionHelper = transactionService.getRetryingTransactionHelper();
            return retryingTransactionHelper
                    .doInTransaction(() -> function.applyAsDouble(nodeDAO1), true);
        };
        Gauge.builder(String.format("%s.%s", SOLR_METRICS_PREFIX, name), nodeDAO, wrappedFunction)
                .baseUnit(baseUnit)
                .register(registry);
    }

}
