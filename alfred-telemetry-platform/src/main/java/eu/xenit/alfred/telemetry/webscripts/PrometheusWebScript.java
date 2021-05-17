package eu.xenit.alfred.telemetry.webscripts;

import eu.xenit.alfred.telemetry.registry.prometheus.PrometheusConfig;
import eu.xenit.alfred.telemetry.util.PrometheusRegistryUtil;
import io.micrometer.core.instrument.MeterRegistry;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.util.ClassUtils;

public class PrometheusWebScript extends AbstractWebScript {

    private final int maxRequests;
    private final Semaphore semaphore;

    private final MeterRegistry meterRegistry;

    public PrometheusWebScript(MeterRegistry meterRegistry, PrometheusConfig prometheusConfig) {
        this.meterRegistry = meterRegistry;
        this.maxRequests = prometheusConfig.getMaxRequests();
        this.semaphore = new Semaphore(maxRequests);
    }

    private static boolean prometheusAvailableOnClasspath() {
        return ClassUtils.isPresent("io.micrometer.prometheus.PrometheusMeterRegistry",
                PrometheusWebScript.class.getClassLoader());
    }

    @Override
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException {

        if (!prometheusAvailableOnClasspath()) {
            throw new WebScriptException(404, "micrometer-prometheus-registry not available on the classpath");
        }

        if (!PrometheusRegistryUtil.isOrContainsPrometheusRegistry(meterRegistry)) {
            throw new WebScriptException(404, "The global MeterRegistry doesn't contain a PrometheusMeterRegistry");
        }

        if (!semaphore.tryAcquire()) {
            throw new WebScriptException(503, "Max number of active requests (" + maxRequests + ") reached");
        }

        try {
            executeInternal(webScriptResponse);
        } finally {
            semaphore.release();
        }
    }

    private void executeInternal(WebScriptResponse response) throws IOException {
        response.setStatus(200);
        writeTextToResponse(PrometheusRegistryUtil.extractPrometheusScrapeData(meterRegistry), response);
    }

    private void writeTextToResponse(final String text, final WebScriptResponse response) throws IOException {
        response.setContentType("text/plain");
        response.setContentEncoding("UTF-8");
        response.setHeader("length", String.valueOf(text.getBytes().length));
        response.getOutputStream().write(text.getBytes());
    }
}
