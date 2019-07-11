package eu.xenit.alfred.telemetry.webscripts;

import eu.xenit.alfred.telemetry.util.PrometheusRegistryUtil;
import io.micrometer.core.instrument.MeterRegistry;
import java.io.IOException;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.util.ClassUtils;

public class PrometheusWebScript extends AbstractWebScript {

    private MeterRegistry meterRegistry;

    public PrometheusWebScript(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
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

        webScriptResponse.setStatus(200);
        writeTextToResponse(PrometheusRegistryUtil.extractPrometheusScrapeData(meterRegistry), webScriptResponse);
    }

    private void writeTextToResponse(final String text, final WebScriptResponse response) throws IOException {
        response.setContentType("text/plain");
        response.setContentEncoding("UTF-8");
        response.setHeader("length", String.valueOf(text.getBytes().length));
        response.getOutputStream().write(text.getBytes());
    }
}
