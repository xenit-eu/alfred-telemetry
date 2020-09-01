package eu.xenit.alfred.telemetry.binder.solr.sharding;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sun.mail.iap.Argument;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicLong;
import org.alfresco.repo.index.shard.Floc;
import org.alfresco.repo.index.shard.Shard;
import org.alfresco.repo.index.shard.ShardInstance;
import org.alfresco.repo.index.shard.ShardRegistry;
import org.alfresco.repo.index.shard.ShardRegistryImpl;
import org.alfresco.repo.index.shard.ShardRegistryImpl.ReplicaState;
import org.alfresco.repo.index.shard.ShardState;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

class SolrShardingMetricsTest {

    @Test
    public void testUpdateMetrics() {
        Floc floc = new Floc();
        HashSet<StoreRef> storeRefs = new HashSet();
        storeRefs.add(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        floc.setStoreRefs(storeRefs);
        Shard shard = new Shard();
        shard.setFloc(floc);
        ShardState shardState = new ShardState();
        shardState.getPropertyBag().put(ShardRegistryImpl.INSTANCE_STATE, ReplicaState.ACTIVE.name());
        ShardInstance shardInstance = new ShardInstance();
        shardInstance.setShard(shard);
        shardInstance.setHostName("myInstanceHost");
        shardState.setShardInstance(shardInstance);
        HashSet<ShardState> shardStates = new HashSet<>();
        shardStates.add(shardState);
        HashMap<Shard, HashSet<ShardState>> shardHashSetHashMap = new HashMap<>();
        shardHashSetHashMap.put(shard, shardStates);
        HashMap<Floc, HashMap<Shard, HashSet<ShardState>>> metricsInformation = new HashMap<>();
        metricsInformation.put(floc, shardHashSetHashMap);

        ShardRegistry shardRegistry = Mockito.mock(ShardRegistry.class);
        MeterRegistry meterRegistry = Mockito.mock(MeterRegistry.class);
        SolrShardingMetrics solrShardingMetrics = new SolrShardingMetrics(shardRegistry, meterRegistry);

        when(shardRegistry.getFlocs()).thenReturn(metricsInformation);
        AtomicLong metricValue = spy(new AtomicLong(0));
        when(meterRegistry.gauge(anyString(), any(Iterable.class), (Number) any())).thenReturn(metricValue);
        solrShardingMetrics.updateMetrics();
        verify(meterRegistry, times(1)).gauge(eq("solr.sharding.lastIndexedTxId"), any(Iterable.class), (Number) any());
    }
}