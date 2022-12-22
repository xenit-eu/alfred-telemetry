package eu.xenit.alfred.telemetry.binder.solr.sharding;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.alfresco.repo.index.shard.Floc;
import org.alfresco.repo.index.shard.Shard;
import org.alfresco.repo.index.shard.ShardInstance;
import org.alfresco.repo.index.shard.ShardRegistry;
import org.alfresco.repo.index.shard.ShardRegistryImpl;
import org.alfresco.repo.index.shard.ShardRegistryImpl.ReplicaState;
import org.alfresco.repo.index.shard.ShardState;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.Mockito.when;

class SolrShardingMetricsTest {

    @Test
    void testUpdateMetrics() {
        Floc floc = new Floc();
        HashSet<StoreRef> storeRefs = new HashSet<>();
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
        Set<ShardState> shardStates = new HashSet<>();
        shardStates.add(shardState);
        Map<Shard, Set<ShardState>> shardHashSetHashMap = new HashMap<>();
        shardHashSetHashMap.put(shard, shardStates);
        Map<Floc, Map<Shard, Set<ShardState>>> metricsInformation = new HashMap<>();
        metricsInformation.put(floc, shardHashSetHashMap);

        ShardRegistry shardRegistry = Mockito.mock(ShardRegistry.class);
        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        SolrShardingMetrics solrShardingMetrics = new SolrShardingMetrics(shardRegistry, meterRegistry, true);

        when(shardRegistry.getFlocs()).thenReturn(metricsInformation);
        solrShardingMetrics.updateMetrics();
        //check if there is one metric with the requested name
        Assertions.assertEquals(1, meterRegistry.getMeters().stream()
                .filter(meter -> meter.getId().getName().equals("solr.sharding.lastIndexedTxId"))
                .collect(Collectors.toSet()).size());
    }

    @Test
    void testShardingStateNull() {
        Floc floc = new Floc();
        HashSet<StoreRef> storeRefs = new HashSet<>();
        storeRefs.add(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        floc.setStoreRefs(storeRefs);
        Shard shard = new Shard();
        shard.setFloc(floc);
        ShardInstance shardInstance = new ShardInstance();
        shardInstance.setShard(shard);
        shardInstance.setHostName("myInstanceHost");
        Set<ShardState> shardStates = new HashSet<>();
        shardStates.add(null);
        Map<Shard, Set<ShardState>> shardHashSetHashMap = new HashMap<>();
        shardHashSetHashMap.put(shard, shardStates);
        Map<Floc, Map<Shard, Set<ShardState>>> metricsInformation = new HashMap<>();
        metricsInformation.put(floc, shardHashSetHashMap);
        ShardRegistry shardRegistry = Mockito.mock(ShardRegistry.class);
        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        SolrShardingMetrics solrShardingMetrics = new SolrShardingMetrics(shardRegistry, meterRegistry, true);

        when(shardRegistry.getFlocs()).thenReturn(metricsInformation);
        //check if we don't get a nullpointer anymore
        solrShardingMetrics.updateMetrics();

        Assertions.assertEquals(0, meterRegistry.getMeters().stream()
                .filter(meter -> meter.getId().getName().equals("solr.sharding.lastIndexedTxId"))
                .collect(Collectors.toSet()).size());

    }
}