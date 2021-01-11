package eu.xenit.alfred.telemetry.solr.util;

import java.util.*;
import java.util.stream.Collectors;

public class Util {
    // everything is enabled by default except graphite registry, tomcat and jetty metrics
    private static final Set<String> DEFAULT_DISABLED_CONFIGS;
    static {
        Set<String> configs = new HashSet<>();
        configs.add("ALFRED_TELEMETRY_EXPORT_GRAPHITE_ENABLED");
        configs.add("METRICS_TOMCAT_ENABLED");
        configs.add("METRICS_JETTY_ENABLED");
        DEFAULT_DISABLED_CONFIGS = Collections.unmodifiableSet(configs);
    }

    private Util() {
        // private ctor to hide implicit public one
    }

    public static List<String> parseList(final String listAsString) {
        return parseList(listAsString, ",");
    }

    public static List<String> parseList(final String listAsString, final String splitCharacter) {
        if (listAsString == null || listAsString.trim().isEmpty()) {
            return Collections.emptyList();
        }

        return Arrays.stream(listAsString.split(splitCharacter))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public static boolean isEnabled(String env) {
        if(System.getenv(env)!=null) {
            return Boolean.parseBoolean(System.getenv(env));
        }

        return (DEFAULT_DISABLED_CONFIGS.contains(env)?false:true);
    }
}
