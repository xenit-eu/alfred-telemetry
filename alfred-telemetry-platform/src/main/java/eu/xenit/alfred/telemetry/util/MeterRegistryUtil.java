package eu.xenit.alfred.telemetry.util;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Statistic;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

public class MeterRegistryUtil {

    public static Collection<Meter> findFirstMatchingMeters(MeterRegistry registry, String name, Iterable<Tag> tags) {
        if (registry instanceof CompositeMeterRegistry) {
            return findFirstMatchingMeters((CompositeMeterRegistry) registry, name, tags);
        }
        return registry.find(name).tags(tags).meters();
    }

    public static Map<Statistic, Double> getSamples(Collection<Meter> meters) {
        Map<Statistic, Double> samples = new LinkedHashMap<>();
        meters.forEach((meter) -> mergeMeasurements(samples, meter));
        return samples;
    }

    public static Map<String, Set<String>> getAvailableTags(Collection<Meter> meters) {
        Map<String, Set<String>> availableTags = new HashMap<>();
        meters.forEach((meter) -> mergeAvailableTags(availableTags, meter));
        return availableTags;
    }

    /* PRIVATE METHODS */

    private static Collection<Meter> findFirstMatchingMeters(CompositeMeterRegistry composite, String name,
            Iterable<Tag> tags) {
        return composite.getRegistries().stream().map((registry) -> findFirstMatchingMeters(registry, name, tags))
                .filter((matching) -> !matching.isEmpty()).findFirst().orElse(Collections.emptyList());
    }

    private static void mergeMeasurements(Map<Statistic, Double> samples, Meter meter) {
        meter.measure().forEach((measurement) -> samples.merge(measurement.getStatistic(), measurement.getValue(),
                mergeFunction(measurement.getStatistic())));
    }

    private static BiFunction<Double, Double, Double> mergeFunction(Statistic statistic) {
        return Statistic.MAX.equals(statistic) ? Double::max : Double::sum;
    }

    private static void mergeAvailableTags(Map<String, Set<String>> availableTags, Meter meter) {
        meter.getId().getTags().forEach((tag) -> {
            Set<String> value = Collections.singleton(tag.getValue());
            availableTags.merge(tag.getKey(), value, MeterRegistryUtil::merge);
        });
    }

    private static <T> Set<T> merge(Set<T> set1, Set<T> set2) {
        Set<T> result = new HashSet<>(set1.size() + set2.size());
        result.addAll(set1);
        result.addAll(set2);
        return result;
    }

}
