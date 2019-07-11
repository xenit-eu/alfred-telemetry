package eu.xenit.alfred.telemetry.webscripts;

import eu.xenit.alfred.telemetry.service.MeterRegistryService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class MetricsWebScript extends DeclarativeWebScript {

    private final MeterRegistryService meterRegistryService;

    public MetricsWebScript(MeterRegistryService meterRegistryService) {
        this.meterRegistryService = meterRegistryService;
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        final Map<String, Object> model = new HashMap<>();

        model.put("names", meterRegistryService.getMeterNames());

        return model;
    }
}
