package eu.xenit.alfred.telemetry.binder.care4alf;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests added to investigate how Micrometer behaves, kept for knowledge purposes
 */
class MicrometerBehaviorTests {

    @Nested
    class CommonTagsWithCompositeRegistries {

        private CompositeMeterRegistry care4AlfRegistry;
        private CompositeMeterRegistry globalRegistry;
        private SimpleMeterRegistry simpleRegistry;

        @BeforeEach
        void setup() {
            care4AlfRegistry = new CompositeMeterRegistry();
            globalRegistry = new CompositeMeterRegistry();
            simpleRegistry = new SimpleMeterRegistry();

            globalRegistry.add(simpleRegistry);
            globalRegistry.config().commonTags(Tags.of("application", "alfresco"));

            care4AlfRegistry.add(globalRegistry);
            care4AlfRegistry.config().commonTags(Tags.of("application", "c4a"));
        }

        /**
         * Scenario: adding a Care4Alf specific meter through the dedicated Care4Alf MeterRegistry wrapper
         */
        @Test
        void commonTagsOfParentCompositeRegistryTakePrecedence() {
            Counter care4AlfCounter = care4AlfRegistry.counter("counter-c4a");
            assertThat(care4AlfCounter.getId().getTag("application"), is("c4a"));

            assertThat(simpleRegistry.getMeters(), hasSize(1));
            Counter counterViaSimpleMeterRegistry = simpleRegistry.find("counter-c4a").counter();
            assertThat(counterViaSimpleMeterRegistry, is(not(nullValue())));
            assertThat(
                    "We want to be able to add the 'application:c4a' tag to Care4Alf specific meters",
                    counterViaSimpleMeterRegistry.getId().getTag("application"), is("c4a"));
        }

        /**
         * Scenario: adding a meter through the global meter registry
         */
        @Test
        void commonTagsOfParentCompositeDontInfluenceNormalMeters() {
            Counter counter = globalRegistry.counter("counter");
            assertThat(counter.getId().getTag("application"), is("alfresco"));

            assertThat(simpleRegistry.getMeters(), hasSize(1));
            Counter counterViaSimpleMeterRegistry = simpleRegistry.find("counter").counter();
            assertThat(counterViaSimpleMeterRegistry, is(not(nullValue())));
            assertThat(
                    "We don't want the 'application:c4a' tag to meters that are not Care4Alf specific",
                    counterViaSimpleMeterRegistry.getId().getTag("application"), is("alfresco"));
        }
    }

}
