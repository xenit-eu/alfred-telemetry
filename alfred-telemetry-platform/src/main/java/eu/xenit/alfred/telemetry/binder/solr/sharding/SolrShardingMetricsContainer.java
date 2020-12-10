package eu.xenit.alfred.telemetry.binder.solr.sharding;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.alfresco.repo.index.shard.Floc;
import org.alfresco.repo.index.shard.Shard;
import org.alfresco.repo.index.shard.ShardInstance;
import org.alfresco.repo.index.shard.ShardRegistry;
import org.alfresco.repo.index.shard.ShardRegistryImpl;
import org.alfresco.repo.index.shard.ShardRegistryImpl.ReplicaState;
import org.alfresco.repo.index.shard.ShardState;

public class SolrShardingMetricsContainer {

    private ShardRegistry shardRegistry;
    private HashMap<Floc, HashMap<Shard, HashSet<ShardState>>> rawData;
    private long lastRefresh;
    private long ttl; // how long before the raw data will be refreshed

    public SolrShardingMetricsContainer(ShardRegistry shardRegistry) {
        this(shardRegistry, 60);
    }

    public SolrShardingMetricsContainer(ShardRegistry shardRegistry, long ttl) {
        this.shardRegistry = shardRegistry;
        this.ttl = ttl;
        this.lastRefresh = 0;
    }

    public void refresh() {
        long now = System.currentTimeMillis();
        if (rawData == null || now - lastRefresh > ttl) {
            rawData = shardRegistry.getFlocs();
        }
    }

    public Set<Floc> getFlocs() {
        return rawData.keySet();
    }

    public Set<Shard> getShards(Floc floc) {
        return rawData.get(floc).keySet();
    }

    public Set<ShardInstance> getShardInstances(Shard shard) {
        return rawData.get(shard.getFloc()).get(shard).stream().filter(shardState -> Objects.nonNull(shardState))
                .map(shardState -> shardState.getShardInstance()).filter(instance -> instance != null)
                .collect(Collectors.toSet());
    }

    public Optional<ShardState> getShardState(ShardInstance shardInstance) {
        Shard shard = shardInstance.getShard();
        return rawData.get(shard.getFloc()).get(shard).stream()
                .filter(shardState -> shardState.getShardInstance() == shardInstance).findAny();
    }

    public Optional<ReplicaState> getReplicaState(ShardInstance shardInstance) {
        return getShardState(shardInstance)
                .map(shardState -> ReplicaState.valueOf(shardState.getPropertyBag().get(ShardRegistryImpl.INSTANCE_STATE)));
    }

}
