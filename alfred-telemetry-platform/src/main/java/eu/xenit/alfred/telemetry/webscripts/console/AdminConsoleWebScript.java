package eu.xenit.alfred.telemetry.webscripts.console;

import com.google.common.annotations.VisibleForTesting;
import eu.xenit.alfred.telemetry.util.MicrometerModules;
import eu.xenit.alfred.telemetry.webscripts.console.AdminConsoleWebscriptResponseModel.AlfredTelemetryModule;
import eu.xenit.alfred.telemetry.webscripts.console.AdminConsoleWebscriptResponseModel.TelemetryBinderConfigModel;
import eu.xenit.alfred.telemetry.webscripts.console.AdminConsoleWebscriptResponseModel.TelemetryDependencyModel;
import eu.xenit.alfred.telemetry.webscripts.console.AdminConsoleWebscriptResponseModel.TelemetryRegistryModel;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.module.ModuleDetails;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

public class AdminConsoleWebScript extends DeclarativeWebScript {

    private final static String PROP_PREFIX_REGISTRY = "alfred.telemetry.export.";
    private final static String PROP_PREFIX_BINDER = "alfred.telemetry.binder.";
    private final static String PROP_PREFIX_ALF_INTEGRATION = "alfred.telemetry.alfresco-integration.";

    public final static String MODULE_ID = "alfred-telemetry-platform";

    private final MeterRegistry meterRegistry;
    private final Properties globalProperties;
    private final ModuleDetails moduleDetails;

    public AdminConsoleWebScript(MeterRegistry meterRegistry, Properties globalProperties,
            ModuleDetails moduleDetails) {
        this.meterRegistry = meterRegistry;
        this.globalProperties = globalProperties;
        this.moduleDetails = moduleDetails;
    }

    public AdminConsoleWebScript(MeterRegistry meterRegistry, Properties globalProperties,
            ServiceRegistry serviceRegistry) {
        this(meterRegistry, globalProperties, serviceRegistry.getModuleService().getModule(MODULE_ID));
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>();

        model.put("telemetry", this.createResponseModel());

        this.includeAlfrescoIntegrationProps(model);

        this.includeSimpleMeterMetrics(model);

        return model;
    }

    @VisibleForTesting
    AdminConsoleWebscriptResponseModel createResponseModel() {
        AlfredTelemetryModule module = new AlfredTelemetryModule(
                moduleDetails.getId(),
                moduleDetails.getModuleVersionNumber().toString());

        Map<String, TelemetryDependencyModel> dependencies = getDependenciesModel();
        Map<String, TelemetryRegistryModel> registries = getRegistriesModel();
        Map<String, TelemetryBinderConfigModel> binders = new HashMap<>();

        return new AdminConsoleWebscriptResponseModel(module, registries, dependencies, binders);
    }

    private Map<String, TelemetryDependencyModel> getDependenciesModel() {
        return Stream.of(
                    new TelemetryDependencyModel("micrometer", MicrometerModules.getMicrometerCoreVersion().toString())
            ).collect(Collectors.toMap(TelemetryDependencyModel::getId, Function.identity()));
    }

    private Map<String, TelemetryRegistryModel> getRegistriesModel() {
        return Stream.of("graphite", "jmx", "prometheus")
                .map(registry -> new TelemetryRegistryModel(registry, PROP_PREFIX_REGISTRY,
                        globalProperties.stringPropertyNames().stream()
                                .filter(s -> s.startsWith(PROP_PREFIX_REGISTRY + registry))
                                .collect(Collectors.toMap(s -> s, globalProperties::getProperty))))
                .collect(Collectors.toMap(TelemetryRegistryModel::getId, Function.identity()));
    }

    private void includeAlfrescoIntegrationProps(Map<String, Object> model) {
        Map<String, String> relevantProps = globalProperties.stringPropertyNames().stream()
                .filter(s -> s.startsWith(PROP_PREFIX_ALF_INTEGRATION))
                .collect(Collectors.toMap(s -> s, globalProperties::getProperty));

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

    @VisibleForTesting
    Map<String, Object> createTemplateModel(WebScriptRequest req, WebScriptResponse res, Map<String, Object> model) {
        return this.createTemplateParameters(req, res, model);
    }

}
