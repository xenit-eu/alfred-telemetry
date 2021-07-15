package eu.xenit.alfred.telemetry.registry.graphite;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class GraphiteConfigTest {

    @Test
    void setStep() {
        GraphiteConfig config = new GraphiteConfig();
        config.setStep("10s");

        assertThat(config.getStep(), is(equalTo(Duration.ofSeconds(10))));

    }

}