package eu.xenit.alfred.telemetry.config;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.config.MeterFilter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;

public class CommonTagFilterFactory extends AbstractFactoryBean<MeterFilter> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonTagFilterFactory.class);

    static final String PROP_KEY_PREFIX_COMMONTAG = "alfred.telemetry.tags.";

    private Properties globalProperties;

    public CommonTagFilterFactory(Properties globalProperties) {
        this.globalProperties = globalProperties;
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

    public static MeterFilter commonTagsIfNotExists(Iterable<Tag> tags) {
        return new MeterFilter() {
            @Override
            @Nonnull
            public Meter.Id map(@Nonnull Meter.Id id) {
                Tags allTags = Tags.of(id.getTags());

                StreamSupport.stream(tags.spliterator(), false)
                        .filter(t -> id.getTag(t.getKey()) == null)
                        .forEach(allTags::and);

                return new Meter.Id(id.getName(), allTags, id.getBaseUnit(), id.getDescription(), id.getType());
            }
        };
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

        for (final String propertyKey : globalProperties.stringPropertyNames()) {

            if (!propertyKey.startsWith(PROP_KEY_PREFIX_COMMONTAG)) {
                continue;
            }

            final String propertyValue = globalProperties.getProperty(propertyKey);

            if (propertyValue == null || propertyValue.trim().isEmpty()) {
                continue;
            }

            final String tagKey = propertyKey.replace(PROP_KEY_PREFIX_COMMONTAG, "");
            commonTags.put(tagKey, propertyValue);
        }

        return commonTags;
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
