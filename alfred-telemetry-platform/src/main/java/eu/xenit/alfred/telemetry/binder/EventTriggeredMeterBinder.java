package eu.xenit.alfred.telemetry.binder;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.util.List;
import org.springframework.context.ApplicationEvent;

/**
 * {@link MeterBinder} for which the {@link #bindTo(MeterRegistry)} needs to be executed if a particular {@link
 * ApplicationEvent} (configured with {@link #triggeringEvents()}) takes place.
 *
 * @see MeterBinderRegistrar#onApplicationEvent(ApplicationEvent)
 */
public interface EventTriggeredMeterBinder extends MeterBinder {

    List<Class<? extends ApplicationEvent>> triggeringEvents();

    /**
     * Determines if the {@link #bindTo(MeterRegistry)} method should be automatically triggered on startup, or only
     * when particular {@link ApplicationEvent} occurs.
     *
     * @return {@code false} if the {@link #bindTo(MeterRegistry)} method only needs te be triggered if on of the {@link
     * #triggeringEvents()} take place. {@code true} if the {@link #bindTo(MeterRegistry)} method also needs to executed
     * on startup of the system.
     */
    default boolean triggerOnStartup() {
        return true;
    }

}
