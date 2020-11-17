package eu.xenit.alfred.telemetry.binder;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.concurrent.TimeUnit;
import java.util.function.ToDoubleFunction;

import io.micrometer.core.instrument.TimeGauge;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.permissions.AclDAO;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.transaction.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeMetrics {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeMetrics.class);

    public static final String ACL_PREFIX = "alfresco.acl";
    public static final String DAO_PREFIX = "alfresco.node";
    private TransactionService transactionService;
    private TimeUnit baseTimeUnit = TimeUnit.MILLISECONDS;

    private NodeDAO nodeDAO;
    private AclDAO aclDAO;
    private MeterRegistry registry;

    public NodeMetrics(NodeDAO nodeDAOComponent, AclDAO aclDAOComponent, TransactionService transactionService,
            MeterRegistry registry, boolean enabled) {
        this.nodeDAO = nodeDAOComponent;
        this.aclDAO = aclDAOComponent;
        this.transactionService = transactionService;
        this.registry = registry;
        if (enabled) {
            registerMetrics();
        }
    }

    private void registerMetrics() {
        LOGGER.info("Registering Alfresco Node metrics");

        Gauge.builder(String.format("%s.%s", DAO_PREFIX, "maxNodeId"), nodeDAO, wrapDAOFunction(NodeDAO::getMaxNodeId))
                .description("Gives the maximum node id from the repo")
                .register(registry);
        Gauge.builder(String.format("%s.%s", DAO_PREFIX, "maxTxnId"), nodeDAO, wrapDAOFunction(NodeDAO::getMaxTxnId))
                .description("Gives the last transaction id from the repo")
                .register(registry);
        TimeGauge.builder(String.format("%s.%s", DAO_PREFIX, "maxTxnCommitTime"), nodeDAO, baseTimeUnit,
                wrapDAOFunction(NodeDAO::getMaxTxnCommitTime))
                .description("Gives the last transaction timestamp from the repo")
                .register(registry);
        Gauge.builder(String.format("%s.%s", ACL_PREFIX, "maxChangeSetId"), aclDAO, wrapAclFunction((dummy) -> {
            long maxCommitTime = aclDAO.getMaxChangeSetCommitTime() + 1L;
            return aclDAO.getMaxChangeSetIdByCommitTime(maxCommitTime);
        }))
                .description("Gives the last change set id from the repo")
                .register(registry);
        TimeGauge.builder(String.format("%s.%s", ACL_PREFIX, "maxChangeSetCommitTime"), aclDAO, baseTimeUnit,
                wrapAclFunction(AclDAO::getMaxChangeSetCommitTime))
                .description("Gives the last change set commit time from the repo")
                .register(registry);
    }

    private ToDoubleFunction<NodeDAO> wrapDAOFunction(ToDoubleFunction<NodeDAO> function) {
        return nodeDAO1 -> {
            RetryingTransactionHelper retryingTransactionHelper = transactionService.getRetryingTransactionHelper();
            return retryingTransactionHelper
                    .doInTransaction(() -> function.applyAsDouble(nodeDAO1), true);
        };
    }

    private ToDoubleFunction<AclDAO> wrapAclFunction(ToDoubleFunction<AclDAO> function) {
        return aclDAO1 -> {
            RetryingTransactionHelper retryingTransactionHelper = transactionService.getRetryingTransactionHelper();
            return retryingTransactionHelper
                    .doInTransaction(() -> function.applyAsDouble(aclDAO1), true);
        };
    }

}
