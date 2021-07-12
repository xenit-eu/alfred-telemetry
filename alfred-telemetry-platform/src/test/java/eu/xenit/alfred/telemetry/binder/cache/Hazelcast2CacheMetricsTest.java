package eu.xenit.alfred.telemetry.binder.cache;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class Hazelcast2CacheMetricsTest {

    @Test
    void testBasicPutAndGetMetrics() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(new Config());
        IMap<String, String> cache = hazelcastInstance.getMap(this.getClass().getSimpleName());

        Hazelcast2CacheMetrics.monitor(meterRegistry, cache);
        cache.put("foo", "bar");

        assertThat(cache.get("foo"), is("bar"));
        assertThat(cache.get("baz"), is(nullValue()));

        assertThat(meterRegistry.get("cache.gets").tag("result", "hit").functionCounter().count(), is(1.0));
        await().atMost(Duration.ofSeconds(5))
                .until(() -> meterRegistry.get("cache.puts").functionCounter().count(), is(1.0));

    }

}