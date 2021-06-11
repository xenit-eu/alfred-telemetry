package eu.xenit.alfred.telemetry.webscripts;

import eu.xenit.alfred.telemetry.registry.prometheus.PrometheusConfig;
import eu.xenit.alfred.telemetry.util.PrometheusRegistryUtil;
import io.micrometer.core.instrument.MeterRegistry;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Semaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.http.HttpStatus;
import org.springframework.util.ClassUtils;

public class PrometheusWebScript extends AbstractWebScript {

    private static final Logger logger = LoggerFactory.getLogger(PrometheusWebScript.class);

    private final int maxRequests;
    private final int suppressMaxRequestsFailuresDuringUptimeMinutes;
    private final RuntimeMXBean runtimeMXBean;
    private final Semaphore semaphore;

    private final MeterRegistry meterRegistry;

    public PrometheusWebScript(MeterRegistry meterRegistry, PrometheusConfig prometheusConfig) {
        this(meterRegistry, ManagementFactory.getRuntimeMXBean(), prometheusConfig);
    }

    public PrometheusWebScript(MeterRegistry meterRegistry, RuntimeMXBean runtimeMXBean,
            PrometheusConfig prometheusConfig) {
        this.meterRegistry = meterRegistry;
        this.maxRequests = prometheusConfig.getMaxRequests();
        this.suppressMaxRequestsFailuresDuringUptimeMinutes = prometheusConfig
                .getSuppressMaxRequestsFailuresDuringUptimeMinutes();
        this.runtimeMXBean = runtimeMXBean;
        this.semaphore = new Semaphore(maxRequests);
    }

    @Override
    public void execute(WebScriptRequest request, WebScriptResponse response) throws IOException {

        if (!prometheusAvailableOnClasspath()) {
            setStatusCodeAndWriteResponse(response, HttpStatus.NOT_FOUND,
                    "micrometer-prometheus-registry not available on the classpath");
            return;
        }

        if (!PrometheusRegistryUtil.isOrContainsPrometheusRegistry(meterRegistry)) {
            setStatusCodeAndWriteResponse(response, HttpStatus.NOT_FOUND,
                    "The global MeterRegistry doesn't contain a PrometheusMeterRegistry");
            return;
        }

        if (!semaphore.tryAcquire()) {
            final String message = "Max number of active requests (" + maxRequests + ") reached";
            logMaxRequestsViolation(message);
            setStatusCodeAndWriteResponse(response, HttpStatus.SERVICE_UNAVAILABLE, message);
            return;
        }

        try {
            executeInternal(response);
        } finally {
            semaphore.release();
        }
    }

    private static boolean prometheusAvailableOnClasspath() {
        return ClassUtils.isPresent("io.micrometer.prometheus.PrometheusMeterRegistry",
                PrometheusWebScript.class.getClassLoader());
    }

    private void executeInternal(WebScriptResponse response) throws IOException {
        setStatusCodeAndWriteResponse(response, HttpStatus.OK,
                PrometheusRegistryUtil.extractPrometheusScrapeData(meterRegistry));
    }

    private void setStatusCodeAndWriteResponse(WebScriptResponse response, HttpStatus status, String message)
            throws IOException {
        response.setStatus(status.value());
        writeResponse(response, message);
    }

    private void writeResponse(final WebScriptResponse response, final String text) throws IOException {
        response.setContentType("text/plain");
        response.setContentEncoding("UTF-8");
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        response.setHeader("length", String.valueOf(bytes.length));
        response.getOutputStream().write(bytes);
    }

    private void logMaxRequestsViolation(String message) {
        if (uptimeAtLeast(suppressMaxRequestsFailuresDuringUptimeMinutes)) {
            logger.error(message);
        } else {
            logger.debug(message);
        }
    }

    private boolean uptimeAtLeast(int minutes) {
        return runtimeMXBean.getUptime() > (1000L * 60 * minutes);
    }
}
