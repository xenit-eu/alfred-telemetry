package eu.xenit.alfred.telemetry.webscripts;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
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
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

@ExtendWith(MockitoExtension.class)
class PrometheusWebScriptTest {

    private static final String TEST_SCRAPE_TEXT = "dummy";

    @Mock
    private PrometheusMeterRegistry registry;

    @Mock
    private WebScriptRequest mockedRequest;
    @Mock
    private WebScriptResponse mockedResponse;
    private ByteArrayOutputStream responseOutputStream;

    @BeforeEach
    void setup() throws IOException {
        responseOutputStream = new ByteArrayOutputStream();
        when(mockedResponse.getOutputStream()).thenReturn(responseOutputStream);
    }

    @Test
    void execute() throws IOException {
        PrometheusWebScript webScript = initWebScript(1);
        when(registry.scrape()).thenReturn(TEST_SCRAPE_TEXT);

        webScript.execute(mockedRequest, mockedResponse);
        verify(mockedResponse).setStatus(200);
        assertThat(responseOutputStream.toString("UTF-8"), is(equalTo(TEST_SCRAPE_TEXT)));
    }

    @Test
    void execute_maxRequestsReached() throws IOException {
        PrometheusWebScript webScript = initWebScript(1);
        AtomicBoolean firstThreadIsBlocked = new AtomicBoolean(false);

        when(registry.scrape()).thenAnswer(answer -> {
            firstThreadIsBlocked.set(true);
            await().forever().until(() -> false);
            return TEST_SCRAPE_TEXT;
        });

        // Trigger a first response on a separate thread
        Runnable firstHttpRequest = () -> {
            WebScriptRequest request = mock(WebScriptRequest.class);
            WebScriptResponse response = mock(WebScriptResponse.class);
            try {
                webScript.execute(request, response);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.submit(firstHttpRequest);

        await().untilAtomic(firstThreadIsBlocked, is(true));

        webScript.execute(mockedRequest, mockedResponse);
        verify(mockedResponse).setStatus(503);

        executorService.shutdownNow();

    }

    private PrometheusWebScript initWebScript(int maxRequests) {
        PrometheusConfig config = new PrometheusConfig();
        config.setMaxRequests(maxRequests);
        return new PrometheusWebScript(registry, config);
    }

}