package eu.xenit.alfred.telemetry.webscripts;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.util.StringUtils;

public class AdminConsoleWebScript extends DeclarativeWebScript {

    private final static String PROP_PREFIX_REGISTRY = "alfred.telemetry.export.";
    private final static String PROP_PREFIX_ALF_INTEGRATION = "alfred.telemetry.alfresco-integration.";

    private Properties globalProperties;

    private MeterRegistry meterRegistry;

    public AdminConsoleWebScript(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>();

        this.includeRegistryProperties(model, "graphite");
        this.includeRegistryProperties(model, "jmx");
        this.includeRegistryProperties(model, "prometheus");

        this.includeAlfrescoIntegrationProps(model);

        this.includeSimpleMeterMetrics(model);

        return model;
    }

    private void includeRegistryProperties(Map<String, Object> model, final String registry) {
        Map<String, String> relevantProps = globalProperties.stringPropertyNames().stream()
                .filter(s -> s.startsWith(PROP_PREFIX_REGISTRY + registry))
                .collect(Collectors.toMap(s -> s, s -> globalProperties.getProperty(s)));

        model.put("registry" + StringUtils.capitalize(registry), relevantProps);
    }

    private void includeAlfrescoIntegrationProps(Map<String, Object> model) {
        Map<String, String> relevantProps = globalProperties.stringPropertyNames().stream()
                .filter(s -> s.startsWith(PROP_PREFIX_ALF_INTEGRATION))
                .collect(Collectors.toMap(s -> s, s -> globalProperties.getProperty(s)));

        model.put("alfrescoIntegration", relevantProps);
    }

    private void includeSimpleMeterMetrics(Map<String, Object> model) {
        if (meterRegistry == null) {
            return;
        }

        Set<Id> meterIds = new TreeSet<>(Comparator.comparing(Id::getName));
        this.collectIds(meterIds, meterRegistry);

        model.put("meters", meterIds);
    }

    private void collectIds(Set<Id> ids, MeterRegistry registry) {
        if (registry instanceof CompositeMeterRegistry) {
            ((CompositeMeterRegistry) registry).getRegistries().forEach((member) -> collectIds(ids, member));
        } else {
            registry.getMeters().stream().map(Meter::getId).forEach(ids::add);
        }
    }

    // <editor-fold desc="Getters and Setters">

    @SuppressWarnings("unused")
    public void setGlobalProperties(Properties globalProperties) {
        this.globalProperties = globalProperties;
    }

    // </editor-fold>
}
