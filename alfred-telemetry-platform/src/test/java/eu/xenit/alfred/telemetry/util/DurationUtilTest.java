package eu.xenit.alfred.telemetry.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class DurationUtilTest {

    @Test
    void simpleDuration() {
        assertThat(DurationUtil.parseDuration("2ns"), is(equalTo(Duration.ofNanos(2))));
        assertThat(DurationUtil.parseDuration("2NS"), is(equalTo(Duration.ofNanos(2))));
        assertThat(DurationUtil.parseDuration("3us"), is(equalTo(Duration.ofNanos(3000))));
        assertThat(DurationUtil.parseDuration("4ms"), is(equalTo(Duration.ofMillis(4))));
        assertThat(DurationUtil.parseDuration("11s"), is(equalTo(Duration.ofSeconds(11))));
        assertThat(DurationUtil.parseDuration("11S"), is(equalTo(Duration.ofSeconds(11))));
        assertThat(DurationUtil.parseDuration("101m"), is(equalTo(Duration.ofMinutes(101))));
        assertThat(DurationUtil.parseDuration("1h"), is(equalTo(Duration.ofHours(1))));
        assertThat(DurationUtil.parseDuration("3d"), is(equalTo(Duration.ofDays(3))));
    }

    @Test
    void simpleDuration_invalidNumber() {
        assertThrows(IllegalArgumentException.class, () -> DurationUtil.parseDuration("1.0s"));
        assertThrows(IllegalArgumentException.class, () -> DurationUtil.parseDuration("1,0s"));
    }

    @Test
    void simpleDuration_defaultSuffix() {
        assertThat(DurationUtil.parseDuration("40"), is(equalTo(Duration.ofMillis(40))));
    }

    @Test
    void simpleDuration_invalidSuffix() {
        assertThrows(IllegalArgumentException.class, () -> DurationUtil.parseDuration("1ps"));
    }

    @Test
    void iso6801Duration() {
        assertThat(DurationUtil.parseDuration("PT0.005S"), is(equalTo(Duration.ofMillis(5))));
        assertThat(DurationUtil.parseDuration("PT1S"), is(equalTo(Duration.ofSeconds(1))));
        assertThat(DurationUtil.parseDuration("PT15M"), is(equalTo(Duration.ofMinutes(15))));
    }

}