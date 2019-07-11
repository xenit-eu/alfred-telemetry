package eu.xenit.alfred.telemetry.util;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Statistic;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MeterRegistryUtilTest {

    private static final String TEST_NAME_1 = "this-is-a-meter-name-for-testing-purposes";
    private static final String TEST_NAME_2 = "this-is-another-meter-name-for-testing-purposes";

    private final static Tag TEST_TAG_1 = Tag.of("firstTag", "firstTagValue");
    private final static Tag TEST_TAG_2 = Tag.of("secondTag", "secondTagValue");

    @Nested
    class FindFirstMatchingMeters {

        @SuppressWarnings("unchecked")
        private void testScenarioWithSomeMeters(final MeterRegistry registry) {
            final Counter counter = registry.counter(TEST_NAME_1, Collections.singletonList(TEST_TAG_1));
            final DistributionSummary summary = registry.summary(TEST_NAME_1, Arrays.asList(TEST_TAG_1, TEST_TAG_2));
            final Counter gauge = registry.counter(TEST_NAME_2, Collections.singleton(TEST_TAG_2));

            assertThat(
                    MeterRegistryUtil.findFirstMatchingMeters(registry, TEST_NAME_1, Collections.singleton(TEST_TAG_1)),
                    hasItems(
                            is(hasProperty("id", equalTo(counter.getId()))),
                            is(hasProperty("id", equalTo(summary.getId())))
                    )
            );
            assertThat(
                    MeterRegistryUtil.findFirstMatchingMeters(registry, TEST_NAME_1, Collections.singleton(TEST_TAG_2)),
                    hasItems(
                            is(hasProperty("id", equalTo(summary.getId())))
                    )
            );
            assertThat(
                    MeterRegistryUtil.findFirstMatchingMeters(registry, TEST_NAME_2, Collections.singleton(TEST_TAG_2)),
                    hasItems(
                            is(hasProperty("id", equalTo(gauge.getId())))
                    )
            );
            assertThat(
                    MeterRegistryUtil.findFirstMatchingMeters(registry, TEST_NAME_2, Collections.singleton(TEST_TAG_1)),
                    empty()
            );
        }

        @Test
        void usingSimpleMeterRegistry() {
            testScenarioWithSomeMeters(new SimpleMeterRegistry());
        }

        @Test
        void findFirstMatchingMeters_usingCompositeMeterRegistry() {
            final CompositeMeterRegistry compositeMeterRegistry = new CompositeMeterRegistry();
            compositeMeterRegistry.add(new SimpleMeterRegistry());

            testScenarioWithSomeMeters(compositeMeterRegistry);
        }

    }

    @Test
    void getSamples() {
        final SimpleMeterRegistry simpleMeterRegistry = new SimpleMeterRegistry();
        final Counter counter = simpleMeterRegistry.counter(TEST_NAME_1, Collections.singletonList(TEST_TAG_1));
        counter.increment(5.0);
        final Counter anotherCounter = simpleMeterRegistry.counter("another-counter");
        anotherCounter.increment(20.0);

        assertThat(MeterRegistryUtil.getSamples(Arrays.asList(counter, anotherCounter)),
                hasEntry(
                        is(Statistic.COUNT),
                        is(25.0)
                )
        );
        assertThat(MeterRegistryUtil.getSamples(Collections.singletonList(counter)),
                hasEntry(
                        is(Statistic.COUNT),
                        is(5.0)
                )
        );
        assertThat(MeterRegistryUtil.getSamples(Collections.singletonList(anotherCounter)),
                hasEntry(
                        is(Statistic.COUNT),
                        is(20.0)
                )
        );
    }

    @Test
    void getAvailableTags() {
        final SimpleMeterRegistry simpleMeterRegistry = new SimpleMeterRegistry();
        final Counter counter = simpleMeterRegistry
                .counter(TEST_NAME_1, Arrays.asList(TEST_TAG_1, TEST_TAG_2));
        final Counter anotherCounter = simpleMeterRegistry
                .counter(TEST_NAME_2, TEST_TAG_1.getKey(), "another-tag-value-for-test-tag-1");

        assertThat(
                MeterRegistryUtil.getAvailableTags(Arrays.asList(counter, anotherCounter)),
                allOf(
                        hasEntry(is(TEST_TAG_2.getKey()), contains(TEST_TAG_2.getValue())),
                        hasEntry(is(TEST_TAG_1.getKey()),
                                containsInAnyOrder(TEST_TAG_1.getValue(), "another-tag-value-for-test-tag-1"))
                )
        );
    }

}