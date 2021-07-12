package eu.xenit.alfred.telemetry.binder.cache;

import com.hazelcast.core.IMap;
import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.FunctionTimer;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.cache.CacheMeterBinder;
import io.micrometer.core.instrument.binder.cache.HazelcastCacheMetrics;
import io.micrometer.core.lang.Nullable;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

/**
 * {@link HazelcastCacheMetrics} equivalent which is compatible with Hazelcast 2 (Alfresco 5.x)
 */
public class Hazelcast2CacheMetrics extends CacheMeterBinder {

    static final String METER_CACHE_GETS_LATENCY = "cache.gets.latency";
    static final String METER_CACHE_PUTS_LATENCY = "cache.puts.latency";
    static final String METER_CACHE_REMOVALS_LATENCY = "cache.removals.latency";

    private static final String TAG_OWNERSHIP = "ownership";

    private final IMap<?, ?> cache;

    /**
     * Record metrics on a Hazelcast cache.
     *
     * @param registry The registry to bind metrics to.
     * @param cache The cache to instrument.
     * @param tags Tags to apply to all recorded metrics. Must be an even number of arguments representing key/value
     * pairs of tags.
     * @param <C> The cache type.
     * @param <K> The cache key type.
     * @param <V> The cache value type.
     * @return The instrumented cache, unchanged. The original cache is not wrapped or proxied in any way.
     */
    public static <K, V, C extends IMap<K, V>> C monitor(MeterRegistry registry, C cache, String... tags) {
        return monitor(registry, cache, Tags.of(tags));
    }

    /**
     * Record metrics on a Hazelcast cache.
     *
     * @param registry The registry to bind metrics to.
     * @param cache The cache to instrument.
     * @param tags Tags to apply to all recorded metrics.
     * @param <C> The cache type.
     * @param <K> The cache key type.
     * @param <V> The cache value type.
     * @return The instrumented cache, unchanged. The original cache is not wrapped or proxied in any way.
     */
    public static <K, V, C extends IMap<K, V>> C monitor(MeterRegistry registry, C cache, Iterable<Tag> tags) {
        new Hazelcast2CacheMetrics(cache, tags).bindTo(registry);
        return cache;
    }

    public <K, V, C extends IMap<K, V>> Hazelcast2CacheMetrics(C cache, Iterable<Tag> tags) {
        super(cache, cache.getName(), tags);
        this.cache = cache;
    }

    @Override
    protected Long size() {
        return cache.getLocalMapStats().getOwnedEntryCount();
    }

    /**
     * @return The number of hits against cache entries hold in this local partition. Not all gets had to result from a
     * get operation against {@link #cache}. If a get operation elsewhere in the cluster caused a lookup against an
     * entry held in this partition, the hit will be recorded against map stats in this partition and not in the map
     * stats of the calling {@link IMap}.
     */
    @Override
    protected long hitCount() {
        return cache.getLocalMapStats().getHits();
    }

    /**
     * @return There is no way to calculate miss count in Hazelcast. See issue #586.
     */
    @Override
    protected Long missCount() {
        return null;
    }

    @Nullable
    @Override
    protected Long evictionCount() {
        return null;
    }

    @Override
    protected long putCount() {
        return cache.getLocalMapStats().getOperationStats().getNumberOfPuts();
    }

    @Override
    protected void bindImplementationSpecificMetrics(@Nonnull MeterRegistry registry) {
        Gauge.builder("cache.entries", cache, cache -> cache.getLocalMapStats().getBackupEntryCount())
                .tags(getTagsWithCacheName()).tag(TAG_OWNERSHIP, "backup")
                .description("The number of backup entries held by this member")
                .register(registry);

        Gauge.builder("cache.entries", cache, cache -> cache.getLocalMapStats().getOwnedEntryCount())
                .tags(getTagsWithCacheName()).tag(TAG_OWNERSHIP, "owned")
                .description("The number of owned entries held by this member")
                .register(registry);

        Gauge.builder("cache.entry.memory", cache, cache -> cache.getLocalMapStats().getBackupEntryMemoryCost())
                .tags(getTagsWithCacheName()).tag(TAG_OWNERSHIP, "backup")
                .description("Memory cost of backup entries held by this member")
                .baseUnit("bytes")
                .register(registry);

        Gauge.builder("cache.entry.memory", cache, cache -> cache.getLocalMapStats().getOwnedEntryMemoryCost())
                .tags(getTagsWithCacheName()).tag(TAG_OWNERSHIP, "owned")
                .description("Memory cost of owned entries held by this member")
                .baseUnit("bytes")
                .register(registry);

        FunctionCounter.builder("cache.partition.gets", cache,
                c -> c.getLocalMapStats().getOperationStats().getNumberOfGets())
                .tags(getTagsWithCacheName())
                .description("The total number of get operations executed against this partition")
                .register(registry);

        timings(registry);
        // nearCacheMetrics(registry); not available in Hazelcast 2.4
    }

    private void timings(MeterRegistry registry) {
        FunctionTimer.builder(METER_CACHE_GETS_LATENCY, cache,
                c -> c.getLocalMapStats().getOperationStats().getNumberOfGets(),
                c -> c.getLocalMapStats().getOperationStats().getTotalGetLatency(),
                TimeUnit.NANOSECONDS)
                .tags(getTagsWithCacheName())
                .description("Cache gets")
                .register(registry);

        FunctionTimer.builder(METER_CACHE_PUTS_LATENCY, cache,
                c -> c.getLocalMapStats().getOperationStats().getNumberOfPuts(),
                c -> c.getLocalMapStats().getOperationStats().getTotalPutLatency(),
                TimeUnit.NANOSECONDS)
                .tags(getTagsWithCacheName())
                .description("Cache puts")
                .register(registry);

        FunctionTimer.builder(METER_CACHE_REMOVALS_LATENCY, cache,
                c -> c.getLocalMapStats().getOperationStats().getNumberOfRemoves(),
                c -> c.getLocalMapStats().getOperationStats().getTotalRemoveLatency(),
                TimeUnit.NANOSECONDS)
                .tags(getTagsWithCacheName())
                .description("Cache removals")
                .register(registry);
    }

}
