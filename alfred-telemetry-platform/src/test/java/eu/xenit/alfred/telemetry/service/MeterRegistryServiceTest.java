package eu.xenit.alfred.telemetry.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItems;

import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MeterRegistryServiceTest {

    private static final String METER_TEST_NAME = "this-is-a-meter-name-for-testing-purposes";

    @Nested
    class getMeterNames {

        @Test
        void simpleMeterRegistry() {
            final SimpleMeterRegistry simpleMeterRegistry = new SimpleMeterRegistry();
            simpleMeterRegistry.counter(METER_TEST_NAME);

            MeterRegistryService service = new MeterRegistryService(simpleMeterRegistry);

            assertThat(service.getMeterNames(), hasItems(METER_TEST_NAME));
        }

        @Test
        void compositeMeterRegistry_insertCounterThroughCompositeRegistry() {
            final CompositeMeterRegistry compositeMeterRegistry = new CompositeMeterRegistry();
            final SimpleMeterRegistry simpleMeterRegistry = new SimpleMeterRegistry();
            compositeMeterRegistry.add(simpleMeterRegistry);

            compositeMeterRegistry.counter(METER_TEST_NAME);

            MeterRegistryService service = new MeterRegistryService(compositeMeterRegistry);

            assertThat(service.getMeterNames(), hasItems(METER_TEST_NAME));
        }

        @Test
        void compositeMeterRegistry_insertCounterThroughChildRegistry() {
            final CompositeMeterRegistry compositeMeterRegistry = new CompositeMeterRegistry();
            final SimpleMeterRegistry simpleMeterRegistry = new SimpleMeterRegistry();
            compositeMeterRegistry.add(simpleMeterRegistry);

            simpleMeterRegistry.counter(METER_TEST_NAME);

            MeterRegistryService service = new MeterRegistryService(compositeMeterRegistry);

            assertThat(service.getMeterNames(), hasItems(METER_TEST_NAME));
        }

        @Test
        void isAlphabetical() {
            final CompositeMeterRegistry compositeMeterRegistry = new CompositeMeterRegistry();
            final SimpleMeterRegistry simpleMeterRegistry = new SimpleMeterRegistry();
            compositeMeterRegistry.add(simpleMeterRegistry);

            simpleMeterRegistry.counter(METER_TEST_NAME);
            simpleMeterRegistry.counter("z-counter");
            simpleMeterRegistry.counter("a-counter");

            MeterRegistryService service = new MeterRegistryService(compositeMeterRegistry);

            assertThat(service.getMeterNames(), contains("a-counter", METER_TEST_NAME, "z-counter"));
        }
    }

}