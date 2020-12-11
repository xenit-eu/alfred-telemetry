package eu.xenit.alfred.telemetry.solr.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Util {

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

        // everything is enabled by default except graphite registry and tomcat metrics
        return ("ALFRED_TELEMETRY_EXPORT_GRAPHITE_ENABLED".equals(env)?
                false:
                ("METRICS_TOMCAT_ENABLED".equals(env) ? false:true));

    }
}
