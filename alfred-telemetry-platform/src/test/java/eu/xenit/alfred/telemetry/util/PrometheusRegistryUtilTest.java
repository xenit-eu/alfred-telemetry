package eu.xenit.alfred.telemetry.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.junit.jupiter.api.Test;

class PrometheusRegistryUtilTest {

    @Test
    void isOrContainsPrometheusRegistry() {
        assertThat(PrometheusRegistryUtil.isOrContainsPrometheusRegistry(mock(PrometheusMeterRegistry.class)),
                is(equalTo(true)));

        assertThat(PrometheusRegistryUtil.isOrContainsPrometheusRegistry(mock(SimpleMeterRegistry.class)),
                is(equalTo(false)));

        final CompositeMeterRegistry compositeMeterRegistry = new CompositeMeterRegistry();
        compositeMeterRegistry.add(new SimpleMeterRegistry());

        assertThat(PrometheusRegistryUtil.isOrContainsPrometheusRegistry(compositeMeterRegistry),
                is(equalTo(false)));

        compositeMeterRegistry.add(mock(PrometheusMeterRegistry.class));

        assertThat(PrometheusRegistryUtil.isOrContainsPrometheusRegistry(compositeMeterRegistry),
                is(equalTo(true)));
    }

    @Test
    void isOrContainsPrometheusRegistry_worksRecursive() {
        final CompositeMeterRegistry parent = new CompositeMeterRegistry();
        final CompositeMeterRegistry childComposite = new CompositeMeterRegistry();
        parent.add(childComposite);

        childComposite.add(new SimpleMeterRegistry());

        assertThat(PrometheusRegistryUtil.isOrContainsPrometheusRegistry(parent),
                is(equalTo(false)));

        childComposite.add(new PrometheusMeterRegistry(PrometheusConfig.DEFAULT));

        assertThat(PrometheusRegistryUtil.isOrContainsPrometheusRegistry(parent),
                is(equalTo(true)));
    }

    @Test
    void extractPrometheusScrapeData() {
        assertThrows(IllegalArgumentException.class,
                () -> PrometheusRegistryUtil.extractPrometheusScrapeData(mock(SimpleMeterRegistry.class)));

        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        final Counter counter = registry.counter("test-counter");
        counter.increment(5);

        assertThat(PrometheusRegistryUtil.extractPrometheusScrapeData(registry),
                containsString("test_counter_total 5.0"));
    }


}