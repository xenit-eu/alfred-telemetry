package eu.xenit.alfred.telemetry.webscripts;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import eu.xenit.alfred.telemetry.registry.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

@ExtendWith(MockitoExtension.class)
class PrometheusWebScriptTest {

    @Mock
    private PrometheusMeterRegistry registry;

    @Test
    void execute_maxRequestsReached() {
        PrometheusWebScript webScript = initWebScript(1);

        when(registry.scrape()).thenAnswer(answer -> {
            await().forever().until(() -> false);
            return "dummy";
        });

        Runnable runnable = () -> {
            WebScriptRequest request = mock(WebScriptRequest.class);
            WebScriptResponse response = mock(WebScriptResponse.class);
            try {
                webScript.execute(request, response);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();

        WebScriptRequest request = mock(WebScriptRequest.class);
        WebScriptResponse response = mock(WebScriptResponse.class);

        WebScriptException e = assertThrows(WebScriptException.class, () -> webScript.execute(request, response));
        assertThat(e.getStatus(), is(equalTo(503)));

        thread.interrupt();
    }

    private PrometheusWebScript initWebScript(int maxRequests) {
        PrometheusConfig config = new PrometheusConfig();
        config.setMaxRequests(maxRequests);
        return new PrometheusWebScript(registry, config);

    }

}