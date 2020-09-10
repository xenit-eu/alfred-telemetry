package eu.xenit.alfred.telemetry.binder.solr.sharding;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.alfresco.repo.index.shard.ShardInstance;
import org.alfresco.repo.index.shard.ShardRegistry;
import org.alfresco.repo.index.shard.ShardRegistryImpl.ReplicaState;
import org.alfresco.repo.index.shard.ShardState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolrShardingMetrics {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolrShardingMetrics.class);

    public static final String SOLR_SHARDING_METRICS_PREFIX = "solr.sharding";

    private SolrShardingMetricsContainer solrShardingMetricsContainer;
    private MeterRegistry registry;

    public SolrShardingMetrics(ShardRegistry shardRegistry, MeterRegistry registry) {
        this.solrShardingMetricsContainer = new SolrShardingMetricsContainer(shardRegistry);
        this.registry = registry;
    }

    private Map<String, AtomicLong> metrics = new HashMap<>();

    public void updateMetrics() {
        LOGGER.debug("Updating metrics");
        solrShardingMetricsContainer.refresh();
        solrShardingMetricsContainer.getFlocs().forEach(floc -> {
            final Tags flocTags = Tags.of("floc", String.valueOf(floc.hashCode()))
                    .and(floc.getStoreRefs().stream().map(storeRef -> Tag
                            .of("storeRef", String.format("%s_%s", storeRef.getProtocol(), storeRef.getIdentifier())))
                            .collect(Collectors.toSet()));
            setAndCreateMetricIfNotExists("shards", floc.getNumberOfShards(), flocTags);
            solrShardingMetricsContainer.getShards(floc).forEach(shard -> {
                Tags shardTags = flocTags.and(Tags.of("shard", String.valueOf(shard.getInstance())));
                Set<ShardInstance> shardInstances = solrShardingMetricsContainer.getShardInstances(shard);
                int numberOfActiveShardInstances = (int) shardInstances.stream().filter(shardInstance ->
                        solrShardingMetricsContainer.getReplicaState(shardInstance) == ReplicaState.ACTIVE).count();
                setAndCreateMetricIfNotExists("activeShardInstances", numberOfActiveShardInstances,
                        shardTags);
                shardInstances.forEach(shardInstance -> {
                    Tags instanceTags = shardTags.and(Tags.of("instanceHost", shardInstance.getHostName()));
                    ShardState shardState = solrShardingMetricsContainer.getShardState(shardInstance);
                    setAndCreateMetricIfNotExists("lastIndexedChangeSetId",
                            shardState.getLastIndexedChangeSetId(), instanceTags);
                    setAndCreateMetricIfNotExists("lastIndexedTxId", shardState.getLastIndexedTxId(),
                            instanceTags);
                    setAndCreateMetricIfNotExists("instanceMode",
                            solrShardingMetricsContainer.getReplicaState(shardInstance).ordinal(), instanceTags);
                    setAndCreateMetricIfNotExists("master", shardState.isMaster() ? 1 : 0,
                            instanceTags);
                    setAndCreateMetricIfNotExists("lastIndexedChangeSetCommitTime",
                            shardState.getLastIndexedChangeSetCommitTime(), instanceTags);
                    setAndCreateMetricIfNotExists("lastIndexedTxCommitTime",
                            shardState.getLastIndexedTxCommitTime(), instanceTags);
                    setAndCreateMetricIfNotExists("lastUpdated",
                            shardState.getLastUpdated(), instanceTags);
                });
            });
        });
    }

    private void setAndCreateMetricIfNotExists(String metricName, long value, Iterable<Tag> tags) {
        String fullMetricName = String.format("%s.%s", SOLR_SHARDING_METRICS_PREFIX, metricName);
        String metricIdentifier = metricName;
        for (Tag tag : tags) {
            metricIdentifier += String.format(".%s.%s", tag.getKey(), tag.getValue());
        }
        if (!metrics.containsKey(metricIdentifier)) {
            LOGGER.debug("Registering new metric {}", fullMetricName);
            metrics.put(metricIdentifier, registry.gauge(fullMetricName, tags, new AtomicLong(value)));
        } else {
            AtomicLong metric = metrics.get(metricIdentifier);
            metric.set(value);
        }
    }
}
