package eu.xenit.alfred.telemetry.binder;

import com.google.common.base.CaseFormat;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.config.MeterFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

public class MeterBinderRegistrar implements InitializingBean, ApplicationContextAware, ApplicationListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(MeterBinderRegistrar.class);

    protected static final String PROP_BINDER_PREFIX = "alfred.telemetry.binder.";
    protected static final String PROP_BINDER_SUFFIX_ENABLED = ".enabled";

    /* CONFIGURATION */
    private boolean enabled = true;
    protected boolean filtersEnabledByDefault = true;
    private Properties properties = new Properties();

    /* DEPENDENCIES */
    private MeterRegistry meterRegistry;
    private ApplicationContext ctx;

    private List<EventTriggeredMeterBinder> eventTriggeredMeterBinders = new ArrayList<>();

    public MeterBinderRegistrar(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void afterPropertiesSet() {
        if (!enabled) {
            getLogger().info("Registrar '{}' is disabled globally and will register no metrics",
                    this.getClass().getCanonicalName());
            return;
        }

        this.addBinders(meterRegistry);
    }

    private void addBinders(final MeterRegistry registry) {
        ctx.getBeansOfType(MeterBinder.class).values().stream()
                .filter(this::isBinderEnabled)
                .filter(this::eventTriggeredMeterBinderFilter)
                .forEach(binder -> {
                    binder.bindTo(registry);
                    getLogger().debug("{}#bindTo executed", binder.getClass().getCanonicalName());
                });
    }

    @Override
    public void onApplicationEvent(@Nonnull ApplicationEvent event) {
        eventTriggeredMeterBinders.forEach(binder -> this.processApplicationEvent(event, binder));
    }

    private void processApplicationEvent(final ApplicationEvent event, final EventTriggeredMeterBinder binder) {
        if (binder.triggeringEvents().stream().anyMatch(clazz -> clazz.isAssignableFrom(event.getClass()))) {
            binder.bindTo(meterRegistry);
            getLogger().debug("{}#bindTo executed because of ApplicationEvent '{}'",
                    binder.getClass().getCanonicalName(), event.getClass().getCanonicalName());
        }
    }

    private boolean eventTriggeredMeterBinderFilter(final MeterBinder binder) {
        if (binder instanceof EventTriggeredMeterBinder) {
            eventTriggeredMeterBinders.add((EventTriggeredMeterBinder) binder);
            getLogger().debug("Registered MeterBinder '{}' to be triggered by Spring ApplicationEvents",
                    binder.getClass().getCanonicalName());

            return ((EventTriggeredMeterBinder) binder).triggerOnStartup();
        }

        return true;
    }

    private boolean isBinderEnabled(final MeterBinder binder) {
        final String propertyKey = getEnabledPropertyKey(binder);
        final boolean enabled = Boolean
                .parseBoolean(properties.getProperty(propertyKey, Boolean.toString(filtersEnabledByDefault)));
        getLogger()
                .trace("'{}' enabled? Property: '{}', value: '{}'", binder.getClass().getCanonicalName(), propertyKey,
                        enabled);
        if (!enabled) {
            getLogger()
                    .debug("Metrics binder '{}' is disabled, see property '{}'", binder.getClass().getCanonicalName(),
                            propertyKey);
        }
        return enabled;
    }

    private String getEnabledPropertyKey(final MeterBinder binder) {
        String binderName = null;
        if (binder instanceof NamedMeterBinder) {
            binderName = ((NamedMeterBinder) binder).getName();
        }
        if (binderName == null || binderName.trim().isEmpty()) {
            binderName = getMeterNameFromClass(binder.getClass());
        }

        return getBinderPropertyPrefix() + binderName + PROP_BINDER_SUFFIX_ENABLED;
    }

    protected String getBinderPropertyPrefix() {
        return PROP_BINDER_PREFIX;
    }

    static String getMeterNameFromClass(Class<?> clazz) {
        final String classNameWithoutMetrics = clazz.getSimpleName().replace("Metrics", "");
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, classNameWithoutMetrics);
    }

    /* Getters and Setters */

    protected Logger getLogger() {
        return LOGGER;
    }

    @Override
    public void setApplicationContext(@Nonnull ApplicationContext applicationContext) throws BeansException {
        ctx = applicationContext;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }
}
