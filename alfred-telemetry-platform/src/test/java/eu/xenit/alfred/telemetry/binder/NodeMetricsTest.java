package eu.xenit.alfred.telemetry.binder;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.permissions.AclDAO;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.transaction.TransactionService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.hamcrest.Matchers.is;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

public class NodeMetricsTest {

    @Test
    public void testNodeMetrics(){
        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        NodeDAO nodeDAO = Mockito.mock(NodeDAO.class);
        AclDAO aclDAO = Mockito.mock(AclDAO.class);
        TransactionService transactionService = createTransactionService();
        AlfrescoNodeMeterBinder nodeMetrics = new AlfrescoNodeMeterBinder(nodeDAO, aclDAO, transactionService);
        nodeMetrics.bindTo(meterRegistry);
        when(nodeDAO.getMaxNodeId()).thenReturn(1L);
        assertThat(
                meterRegistry.get("alfresco.node.maxNodeId")
                        .gauge().value(),
                is(1.0));

        when(nodeDAO.getMaxTxnId()).thenReturn(2L);
        assertThat(
                meterRegistry.get("alfresco.node.maxTxnId")
                        .gauge().value(),
                is(2.0));

        when(nodeDAO.getMaxTxnCommitTime()).thenReturn(3L);
        assertThat(
                meterRegistry.get("alfresco.node.maxTxnCommitTime")
                        .timeGauge().value(),
                is(3.0));

        when(aclDAO.getMaxChangeSetCommitTime()).thenReturn(4L);
         assertThat(
                meterRegistry.get("alfresco.acl.maxChangeSetCommitTime")
                        .gauge().value(),
                is(4.0));
        when(aclDAO.getMaxChangeSetIdByCommitTime(anyLong())).thenReturn(5L);

        assertThat(
                meterRegistry.get("alfresco.acl.maxChangeSetId")
                        .gauge().value(),
                is(5.0));
    }

    private static TransactionService createTransactionService() {
        TransactionService transactionService = Mockito.mock(TransactionService.class);
        Mockito.when(transactionService.getRetryingTransactionHelper()).thenReturn(new RetryingTransactionHelper(){

            @Override
            public <R> R doInTransaction(RetryingTransactionCallback<R> cb, boolean readOnly, boolean requiresNew) {
                try {
                    return cb.execute();
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            }
        });
        return transactionService;
    }
}