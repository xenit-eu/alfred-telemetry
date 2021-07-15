package eu.xenit.alfred.telemetry.util;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.util.StringUtils;

/**
 * Inspiration: https://github.com/spring-projects/spring-boot/blob/47516b50c39bd6ea924a1f6720ce6d4a71088651/spring-boot-project/spring-boot/src/main/java/org/springframework/boot/convert/DurationStyle.java
 */
public class DurationUtil {

    private DurationUtil() {
        // private ctor to hide implicit public one
    }

    public static Duration parseDuration(String duration) {
        for (DurationParser parser : DurationParser.values()) {
            Matcher matcher = parser.getPattern().matcher(duration);
            if (matcher.matches()) {
                return parser.getConverter().apply(matcher);
            }
        }

        throw new IllegalArgumentException("'" + duration + "' is not a valid duration");
    }

    enum DurationParser {
        SIMPLE("^([+-]?\\d+)([a-zA-Z]{0,2})$", matcher -> {
            return Duration.of(Long.parseLong(matcher.group(1)), suffixToUnit(matcher.group(2)));
        }),
        ISO8601("^[+-]?P.*$", matcher -> {
            return Duration.parse(matcher.group());
        });

        private final Pattern pattern;
        private final Function<Matcher, Duration> converter;

        DurationParser(String pattern, Function<Matcher, Duration> converter) {
            this.pattern = Pattern.compile(pattern);
            this.converter = converter;
        }

        public Pattern getPattern() {
            return pattern;
        }

        public Function<Matcher, Duration> getConverter() {
            return converter;
        }

        private static ChronoUnit suffixToUnit(String suffix) {
            return suffixToUnit(suffix, ChronoUnit.MILLIS);
        }

        private static ChronoUnit suffixToUnit(String suffix, ChronoUnit defaultUnit) {
            if (!StringUtils.hasLength(suffix)) {
                return defaultUnit;
            }

            final String suffixLowerCase = suffix.trim().toLowerCase();

            switch (suffixLowerCase) {
                case "ns":
                    return ChronoUnit.NANOS;
                case "us":
                    return ChronoUnit.MICROS;
                case "ms":
                    return ChronoUnit.MILLIS;
                case "s":
                    return ChronoUnit.SECONDS;
                case "m":
                    return ChronoUnit.MINUTES;
                case "h":
                    return ChronoUnit.HOURS;
                case "d":
                    return ChronoUnit.DAYS;
                default:
                    throw new IllegalArgumentException("Unrecognized time suffix: '" + suffix + "'");
            }

        }
    }
}
