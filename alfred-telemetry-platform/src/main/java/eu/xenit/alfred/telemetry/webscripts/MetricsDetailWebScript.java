package eu.xenit.alfred.telemetry.webscripts;

import eu.xenit.alfred.telemetry.service.MeterRegistryService;
import eu.xenit.alfred.telemetry.util.MeterRegistryUtil;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Statistic;
import io.micrometer.core.instrument.Tag;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.apache.http.HttpStatus;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class MetricsDetailWebScript extends DeclarativeWebScript {

    private final MeterRegistryService meterRegistryService;

    public MetricsDetailWebScript(MeterRegistryService meterRegistryService) {
        this.meterRegistryService = meterRegistryService;
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        final Map<String, Object> model = new HashMap<>();

        final String requiredMetricName = extractRequiredMetricName(req);
        final List<Tag> tags = extractTags(req);

        Collection<Meter> meters = meterRegistryService.findFirstMatchingMeters(requiredMetricName, tags);
        if (meters.isEmpty()) {
            throw new WebScriptException(HttpStatus.SC_NOT_FOUND, "'" + requiredMetricName + "' meter not found");
        }
        Map<Statistic, Double> samples = MeterRegistryUtil.getSamples(meters);
        Map<String, Set<String>> availableTags = MeterRegistryUtil.getAvailableTags(meters);
        tags.forEach((t) -> availableTags.remove(t.getKey()));
        Meter.Id meterId = meters.iterator().next().getId();

        model.put("name", requiredMetricName);
        model.put("description", meterId.getDescription());
        model.put("baseUnit", meterId.getBaseUnit());
        model.put("measurements", asList(samples, Sample::new));
        model.put("availableTags", asList(availableTags, AvailableTag::new));

        return model;
    }

    private String extractRequiredMetricName(WebScriptRequest request) {
        final String ret = request.getServiceMatch().getTemplateVars().get("meterName");
        if (ret == null || ret.isEmpty()) {
            throw new WebScriptException(HttpStatus.SC_NOT_FOUND, "Include a meter name in the path");
        }
        return ret;
    }

    private List<Tag> extractTags(WebScriptRequest request) {
        return parseTags(request.getParameterValues("tag"));
    }

    private static List<Tag> parseTags(String[] tags) {
        if (tags == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(tags).map(MetricsDetailWebScript::parseTag).collect(Collectors.toList());
    }

    private static Tag parseTag(String tag) {
        String[] parts = tag.split(":", 2);
        if (parts.length != 2) {
            throw new WebScriptException(HttpStatus.SC_UNPROCESSABLE_ENTITY,
                    "Each tag parameter must be in the form 'key:value' but was: " + tag);
        }
        return Tag.of(parts[0], parts[1]);
    }

    private static <K, V, T> List<T> asList(Map<K, V> map, BiFunction<K, V, T> mapper) {
        return map.entrySet().stream().map((entry) -> mapper.apply(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /* MODEL */

    /**
     * A set of tags for further dimensional drilldown and their potential values.
     */
    public static final class AvailableTag {

        private final String tag;

        private final Set<String> values;

        AvailableTag(String tag, Set<String> values) {
            this.tag = tag;
            this.values = values;
        }

        public String getTag() {
            return this.tag;
        }

        public Set<String> getValues() {
            return this.values;
        }

    }

    /**
     * A measurement sample combining a {@link Statistic statistic} and a value.
     */
    public static final class Sample {

        private final Statistic statistic;

        private final Double value;

        Sample(Statistic statistic, Double value) {
            this.statistic = statistic;
            this.value = value;
        }

        public Statistic getStatistic() {
            return this.statistic;
        }

        public Double getValue() {
            return this.value;
        }

        @Override
        public String toString() {
            return "MeasurementSample{" + "statistic=" + this.statistic + ", value=" + this.value + '}';
        }

    }
}
