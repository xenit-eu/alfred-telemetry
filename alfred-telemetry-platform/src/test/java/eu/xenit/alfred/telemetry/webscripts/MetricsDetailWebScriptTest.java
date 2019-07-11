package eu.xenit.alfred.telemetry.webscripts;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import eu.xenit.alfred.telemetry.service.MeterRegistryService;
import eu.xenit.alfred.telemetry.webscripts.MetricsDetailWebScript.AvailableTag;
import eu.xenit.alfred.telemetry.webscripts.MetricsDetailWebScript.Sample;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Statistic;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

@ExtendWith(MockitoExtension.class)
// to avoid UnnecessaryStubbingException:
@MockitoSettings(strictness = Strictness.LENIENT)
class MetricsDetailWebScriptTest {

    private static final String TEST_NAME_1 = "this-is-a-meter-name-for-testing-purposes";

    private final static Tag TEST_TAG_1 = Tag.of("firstTag", "firstTagValue");

    private MeterRegistryService service;

    @BeforeEach
    void initializeSomeMeters() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        service = new MeterRegistryService(registry);

        Counter testCounter = registry.counter(TEST_NAME_1, Collections.singletonList(TEST_TAG_1));
        testCounter.increment(7.0);
    }

    @Test
    void executeImpl_missingMeterName() {
        assertThrowsWebScriptException(() -> executeMetricsDetailWebScript(null), e -> {
            assertThat(e.getMessage(), containsString("name"));
            assertThat(e.getStatus(), is(equalTo(404)));
        });
    }

    @Test
    void executeImpl_failedToParseTags() {
        assertThrowsWebScriptException(() -> executeMetricsDetailWebScript(TEST_NAME_1, "invalid-format"), e -> {
            assertThat(e.getMessage(), containsString("key:value"));
            assertThat(e.getStatus(), is(equalTo(422)));
        });
        assertThrowsWebScriptException(() -> executeMetricsDetailWebScript(TEST_NAME_1, "invalid/format"), e -> {
            assertThat(e.getMessage(), containsString("key:value"));
            assertThat(e.getStatus(), is(equalTo(422)));
        });
    }

    @Test
    void executeImpl_noMeterFound() {
        assertThrowsWebScriptException(() -> executeMetricsDetailWebScript("non-existing-meter"), e -> {
            assertThat(e.getMessage(), containsString("meter not found"));
            assertThat(e.getStatus(), is(equalTo(404)));
        });
        assertThrowsWebScriptException(() -> executeMetricsDetailWebScript(TEST_NAME_1, "nonexisting:value"), e -> {
            assertThat(e.getMessage(), containsString("meter not found"));
            assertThat(e.getStatus(), is(equalTo(404)));
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeImpl() {
        Map<String, Object> model = executeMetricsDetailWebScript(TEST_NAME_1);

        List<AvailableTag> availableTags = (List<AvailableTag>) model.get("availableTags");
        assertThat(availableTags, hasItems(hasProperty("tag", is(TEST_TAG_1.getKey()))));
        List<Sample> measurements = (List<Sample>) model.get("measurements");
        assertThat(measurements, hasItems(
                allOf(
                        hasProperty("statistic", is(Statistic.COUNT)),
                        hasProperty("value", is(7.0)))
                )
        );
    }

    private Map<String, Object> executeMetricsDetailWebScript(final String meterName, final String... tags) {
        MetricsDetailWebScript webScript = new MetricsDetailWebScript(service);
        return webScript.executeImpl(mockWebScriptRequest(meterName, tags), mock(Status.class), mock(Cache.class));
    }

    private WebScriptRequest mockWebScriptRequest(final String meterName, final String... tags) {
        WebScriptRequest mock = mock(WebScriptRequest.class);

        when(mock.getParameterValues("tag")).thenReturn(tags);
        when(mock.getServiceMatch()).thenReturn(new Match("", Collections.singletonMap("meterName", meterName), ""));

        return mock;
    }

    private void assertThrowsWebScriptException(Executable executable, Consumer<WebScriptException> exceptionChecker) {
        WebScriptException e = assertThrows(WebScriptException.class, executable);
        exceptionChecker.accept(e);
    }

}