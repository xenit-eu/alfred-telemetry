package eu.xenit.alfred.telemetry.webscripts;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.xenit.alfred.telemetry.registry.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

@ExtendWith(MockitoExtension.class)
class PrometheusWebScriptTest {

    private static final String TEST_SCRAPE_TEXT = "dummy";

    @Mock
    private PrometheusMeterRegistry registry;

    @Test
    void execute() throws IOException {
        PrometheusWebScript webScript = initWebScript(1);
        when(registry.scrape()).thenReturn(TEST_SCRAPE_TEXT);

        WebScriptRequest request = mock(WebScriptRequest.class);
        WebScriptResponse response = mock(WebScriptResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(out);

        webScript.execute(request, response);
        verify(response).setStatus(200);
        assertThat(out.toString("UTF-8"), is(equalTo(TEST_SCRAPE_TEXT)));
    }

    @Test
    void execute_maxRequestsReached() {
        PrometheusWebScript webScript = initWebScript(1);

        when(registry.scrape()).thenAnswer(answer -> {
            await().forever().until(() -> false);
            return TEST_SCRAPE_TEXT;
        });

        Runnable firstHttpRequest = () -> {
            WebScriptRequest request = mock(WebScriptRequest.class);
            WebScriptResponse response = mock(WebScriptResponse.class);
            try {
                webScript.execute(request, response);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };

        Runnable secondHttpRequest = () -> {
            WebScriptRequest request = mock(WebScriptRequest.class);
            WebScriptResponse response = mock(WebScriptResponse.class);
            WebScriptException e = assertThrows(WebScriptException.class, () -> webScript.execute(request, response));
            assertThat(e.getStatus(), is(equalTo(503)));
        };

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.submit(firstHttpRequest);
        Future<?> future = executorService.submit(secondHttpRequest);

        await().until(future::isDone);
        executorService.shutdownNow();
    }

    private PrometheusWebScript initWebScript(int maxRequests) {
        PrometheusConfig config = new PrometheusConfig();
        config.setMaxRequests(maxRequests);
        return new PrometheusWebScript(registry, config);

    }

}