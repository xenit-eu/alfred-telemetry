package eu.xenit.alfred.telemetry.binder.care4alf;

import eu.xenit.alfred.telemetry.binder.MeterBinderRegistrar;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Care4AlfMeterBinderRegistrar extends MeterBinderRegistrar {

    private static final Logger LOGGER = LoggerFactory.getLogger(Care4AlfMeterBinderRegistrar.class);

    public Care4AlfMeterBinderRegistrar(MeterRegistry meterRegistry) {
        super(new CompositeMeterRegistry().add(meterRegistry));
        this.filtersEnabledByDefault = false;
    }

    @Override
    protected void addFilters(MeterRegistry registry) {
        super.addFilters(registry);
        registry.config()
                .meterFilter(MeterFilter.replaceTagValues("application", s -> "c4a"))
                .meterFilter(new TicketMetricsMeterFilter());
    }

    @Override
    protected String getBinderPropertyPrefix() {
        return super.getBinderPropertyPrefix() + "care4alf.";
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
}
