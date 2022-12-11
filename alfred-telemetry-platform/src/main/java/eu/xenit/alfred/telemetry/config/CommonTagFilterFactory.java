package eu.xenit.alfred.telemetry.config;

import eu.xenit.alfred.telemetry.util.PrometheusRegistryUtil;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.MeterFilter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.alfresco.enterprise.metrics.MetricsController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;

public class CommonTagFilterFactory extends AbstractFactoryBean<MeterFilter> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonTagFilterFactory.class);

    static final String PROP_KEY_PREFIX_COMMONTAG = "alfred.telemetry.tags.";
    static final String PROP_KEY_EXPORT_PROMETHEUS = "alfred.telemetry.export.prometheus.enabled";

    private final Properties globalProperties;

    public CommonTagFilterFactory(Properties globalProperties,
                                  MetricsController metricsController) {
        this.globalProperties = globalProperties;
        addCommonTagsTo(metricsController);
    }

    @Override
    public Class<?> getObjectType() {
        return MeterFilter.class;
    }

    @Override
    @Nonnull
    protected MeterFilter createInstance() {
        return commonTagsIfNotExists(getCommonTags());
    }

    public static MeterFilter commonTagsIfNotExists(Iterable<Tag> tagsToAdd) {
        return new MeterFilter() {
            @Override
            @Nonnull
            public Meter.Id map(@Nonnull Meter.Id id) {
                Id ret = id;
                for (Tag tagToAdd : tagsToAdd) {
                    if (ret.getTag(tagToAdd.getKey()) == null) {
                        ret = ret.withTag(tagToAdd);
                    }
                }
                return ret;
            }
        };
    }

    private void addCommonTagsTo(MetricsController metricsController) {
        LOGGER.debug("addCommonTagsTo >>>>>>");
        if(metricsController == null) {
            LOGGER.debug("metricsController is null");
            return;
        }

        MeterRegistry registry = metricsController.getRegistry();
        if(registry == null) {
            LOGGER.debug("registry is null");
            return;
        }

        PrometheusMeterRegistry prometheusMeterRegistry =
                PrometheusRegistryUtil.tryToExtractPrometheusRegistry(registry);
        if(!globalProperties.containsKey(PROP_KEY_EXPORT_PROMETHEUS)) {
            LOGGER.debug("global props does not contain {}", PROP_KEY_EXPORT_PROMETHEUS);
            return;
        }

        String enableExport = globalProperties.getProperty(PROP_KEY_EXPORT_PROMETHEUS);
        if(Boolean.FALSE.toString().equalsIgnoreCase(enableExport)) {
            LOGGER.debug("{} is not enabled", PROP_KEY_EXPORT_PROMETHEUS);
            return;
        }

        prometheusMeterRegistry.config().commonTags(getCommonTags());
        LOGGER.debug("addCommonTagsTo <<<<<<");
    }

    private Iterable<Tag> getCommonTags() {
        Hashtable<String, String> commonTags = extractCommonTagsFromProperties();
        if (!commonTags.containsKey("host")) {
            commonTags.put("host", tryToRetrieveHostName());
        }
        commonTags.put("application", "alfresco");
        return toTags(commonTags);
    }

    private Hashtable<String, String> extractCommonTagsFromProperties() {
        Hashtable<String, String> commonTags = new Hashtable<>();
        globalProperties.stringPropertyNames()
                .forEach(propKey -> this.addCommonTagTo(propKey, commonTags));
        return commonTags;
    }

    private void addCommonTagTo(String propertyKey, Hashtable<String, String> commonTags) {
        if (!propertyKey.startsWith(PROP_KEY_PREFIX_COMMONTAG)) {
            return;
        }
        final String propertyValue = globalProperties.getProperty(propertyKey);
        if (propertyValue == null || propertyValue.trim().isEmpty()) {
            return;
        }
        final String tagKey = propertyKey.replace(PROP_KEY_PREFIX_COMMONTAG, "");
        commonTags.put(tagKey, propertyValue);
    }

    private String tryToRetrieveHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            LOGGER.warn("Unable to retrieve name of local host", e);
            return "unknown-host";
        }

    }

    private static Iterable<Tag> toTags(Hashtable<String, String> tagsAsTable) {
        return tagsAsTable.entrySet().stream()
                .map(e -> Tag.of(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }
}
