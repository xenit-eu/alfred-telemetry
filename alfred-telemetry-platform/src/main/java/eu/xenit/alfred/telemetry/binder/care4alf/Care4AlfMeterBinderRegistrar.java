package eu.xenit.alfred.telemetry.binder.care4alf;

import eu.xenit.alfred.telemetry.binder.MeterBinderRegistrar;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Care4AlfMeterBinderRegistrar extends MeterBinderRegistrar {

    private static final Logger LOGGER = LoggerFactory.getLogger(Care4AlfMeterBinderRegistrar.class);

    public Care4AlfMeterBinderRegistrar(MeterRegistry meterRegistry) {
        super(wrapRegistry(meterRegistry));
        this.filtersEnabledByDefault = false;
    }

    private static MeterRegistry wrapRegistry(final MeterRegistry originalRegistry) {
        final MeterRegistry wrapped = new CompositeMeterRegistry().add(originalRegistry);

        wrapped.config()
                .meterFilter(MeterFilter.commonTags(Tags.of("application", "c4a")))
                .meterFilter(new TicketMetricsMeterFilter());

        return wrapped;
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
