package eu.xenit.alfred.telemetry.binder.solr.sharding;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
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

    private static final String BASE_UNIT_NUMBER = "number";
    private static final String BASE_UNIT_TIMESTAMP = "timestamp";
    public static final String SOLR_SHARDING_METRICS_PREFIX = "solr.sharding";

    private final boolean flocIdEnabled;

    private final SolrShardingMetricsContainer solrShardingMetricsContainer;
    private final MeterRegistry registry;

    public SolrShardingMetrics(ShardRegistry shardRegistry, MeterRegistry registry, boolean flocIdEnabled) {
        this.solrShardingMetricsContainer = new SolrShardingMetricsContainer(shardRegistry);
        this.registry = registry;
        this.flocIdEnabled = flocIdEnabled;
    }

    private Map<String, AtomicLong> metrics = new HashMap<>();

    public void updateMetrics() {
        LOGGER.debug("Updating metrics");
        solrShardingMetricsContainer.refresh();
        solrShardingMetricsContainer.getFlocs().forEach(floc -> {
            //Disable floc id if option is disabled so we don't have the problems of having to chose the floc in grafana
            final Tags flocTags = Tags.of("floc", String.valueOf(flocIdEnabled ? floc.hashCode() : 1))
                    .and(floc.getStoreRefs().stream().map(storeRef -> Tag
                            .of("storeRef", String.format("%s_%s", storeRef.getProtocol(), storeRef.getIdentifier())))
                            .collect(Collectors.toSet()));
            setAndCreateMetricIfNotExists("shards", floc.getNumberOfShards(), flocTags, BASE_UNIT_NUMBER);
            solrShardingMetricsContainer.getShards(floc).forEach(shard -> {
                Tags shardTags = flocTags.and(Tags.of("shard", String.valueOf(shard.getInstance())));
                Set<ShardInstance> shardInstances = solrShardingMetricsContainer.getShardInstances(shard);
                AtomicInteger numberOfActiveShardInstances = new AtomicInteger();
                shardInstances.forEach(shardInstance -> {
                    Optional<ReplicaState> replicaState = solrShardingMetricsContainer.getReplicaState(shardInstance);
                    if (replicaState.map(state -> (state == ReplicaState.ACTIVE)).orElse(false)) {
                        numberOfActiveShardInstances.getAndIncrement();
                    }
                    Tags instanceTags = shardTags.and(Tags.of("instanceHost", shardInstance.getHostName()));
                    ShardState shardState = solrShardingMetricsContainer.getShardState(shardInstance).orElseGet(() -> {
                        ShardState state = new ShardState();
                        state = new ShardState();
                        state.setLastIndexedChangeSetId(-1);
                        state.setLastIndexedTxId(-1);
                        state.setLastIndexedChangeSetCommitTime(-1);
                        state.setLastIndexedTxCommitTime(-1);
                        state.setMaster(false);
                        state.setLastUpdated(-1);
                        return state;
                    });
                    setAndCreateMetricIfNotExists("lastIndexedChangeSetId",
                            shardState.getLastIndexedChangeSetId(), instanceTags, BASE_UNIT_NUMBER);
                    setAndCreateMetricIfNotExists("lastIndexedTxId", shardState.getLastIndexedTxId(),
                            instanceTags, BASE_UNIT_NUMBER);
                    int instanceMode = replicaState.map(ReplicaState::ordinal).orElse(-1);
                    setAndCreateMetricIfNotExists("instanceMode", instanceMode, instanceTags, "enumValue");
                    setAndCreateMetricIfNotExists("master", shardState.isMaster() ? 1 : 0,
                            instanceTags, "boolean");
                    setAndCreateMetricIfNotExists("lastIndexedChangeSetCommitTime",
                            shardState.getLastIndexedChangeSetCommitTime(), instanceTags, BASE_UNIT_TIMESTAMP);
                    setAndCreateMetricIfNotExists("lastIndexedTxCommitTime",
                            shardState.getLastIndexedTxCommitTime(), instanceTags, BASE_UNIT_TIMESTAMP);
                    setAndCreateMetricIfNotExists("lastUpdated",
                            shardState.getLastUpdated(), instanceTags, BASE_UNIT_TIMESTAMP);
                });
                setAndCreateMetricIfNotExists("activeShardInstances", numberOfActiveShardInstances.intValue(),
                        shardTags, BASE_UNIT_NUMBER);
            });
        });
    }

    private void setAndCreateMetricIfNotExists(String metricName, long value, Iterable<Tag> tags, String baseUnit) {
        String fullMetricName = String.format("%s.%s", SOLR_SHARDING_METRICS_PREFIX, metricName);
        String metricIdentifier = metricName;
        for (Tag tag : tags) {
            metricIdentifier += String.format(".%s.%s", tag.getKey(), tag.getValue());
        }
        if (!metrics.containsKey(metricIdentifier)) {
            LOGGER.debug("Registering new metric {}", fullMetricName);
            AtomicLong atomicLongValue = new AtomicLong(value);
            Gauge.builder(fullMetricName, atomicLongValue, Number::doubleValue).tags(tags).baseUnit(baseUnit)
                    .register(registry);
            metrics.put(metricIdentifier, atomicLongValue);
        } else {
            AtomicLong metric = metrics.get(metricIdentifier);
            metric.set(value);
        }
    }
}
