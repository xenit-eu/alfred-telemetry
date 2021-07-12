package eu.xenit.alfred.telemetry.binder.cache;

import static eu.xenit.alfred.telemetry.binder.cache.Hazelcast2CacheMetrics.METER_CACHE_GETS_LATENCY;
import static eu.xenit.alfred.telemetry.binder.cache.Hazelcast2CacheMetrics.METER_CACHE_PUTS_LATENCY;
import static eu.xenit.alfred.telemetry.binder.cache.Hazelcast2CacheMetrics.METER_CACHE_REMOVALS_LATENCY;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class Hazelcast2CacheMetricsTest {

    private SimpleMeterRegistry meterRegistry;

    @BeforeEach
    void setup() {
        meterRegistry = new SimpleMeterRegistry();
    }

    @Test
    void testCacheMetrics() {
        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(new Config());
        IMap<String, String> cache = hazelcastInstance.getMap(this.getClass().getSimpleName());

        Hazelcast2CacheMetrics.monitor(meterRegistry, cache);
        cache.put("foo", "bar");

        assertThat(cache.get("foo"), is("bar"));
        assertThat(cache.get("baz"), is(nullValue()));

        assertThat(meterRegistry.get("cache.gets").tag("result", "hit").functionCounter().count(), is(1.0));
        await().atMost(Duration.ofSeconds(5))
                .until(() -> meterRegistry.get("cache.puts").functionCounter().count(), is(1.0));

        validateLatencyMetrics(METER_CACHE_GETS_LATENCY, is(2.0), is(greaterThanOrEqualTo(0.0)));
        validateLatencyMetrics(METER_CACHE_PUTS_LATENCY, is(1.0), is(greaterThanOrEqualTo(0.0)));
        validateLatencyMetrics(METER_CACHE_REMOVALS_LATENCY, is(0.0), is(0.0));

        cache.remove("foo");
        validateLatencyMetrics(METER_CACHE_REMOVALS_LATENCY, is(1.0), is(greaterThanOrEqualTo(0.0)));
    }

    private void validateLatencyMetrics(final String latencyMeterName, Matcher<Double> counterMatcher,
            Matcher<Double> totalTimeNanosMatcher) {
        await().atMost(Duration.ofSeconds(5))
                .until(() -> meterRegistry.get(latencyMeterName).functionTimer().count(), counterMatcher);
        await().atMost(Duration.ofSeconds(5))
                .until(() -> meterRegistry.get(latencyMeterName).functionTimer().totalTime(TimeUnit.NANOSECONDS),
                        totalTimeNanosMatcher);
    }

}