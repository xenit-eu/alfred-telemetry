package eu.xenit.alfred.telemetry.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class StringUtils {

    private StringUtils() {
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
}
