package eu.xenit.alfred.telemetry.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;

import org.junit.jupiter.api.Test;

class StringUtilsTest {

    @Test
    void parseList() {
        assertThat(StringUtils.parseList(null), hasSize(0));
        assertThat(StringUtils.parseList(""), hasSize(0));
        assertThat(StringUtils.parseList("first,"), contains("first"));
        assertThat(StringUtils.parseList("first, second,third"), contains("first", "second", "third"));
    }

}