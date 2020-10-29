package eu.xenit.alfred.telemetry.hamcrest.matchers;

import org.hamcrest.Matcher;

public class StringMatchers {

    /**
     * Creates a matcher that matches if the examined object is a case insensitive string representation of a boolean.
     * <p/>
     * For example:
     * <pre>assertThat("true", isBoolean())</pre>
     */
    public static Matcher<String> isBoolean() {
        return IsBooleanStringMatcher.isBoolean();
    }

    /**
     * Creates a matcher that matches if the examined object is a string that can be parsed as an integer.
     * <p/>
     * For example:
     * <pre>assertThat("8080", isInteger())</pre>
     */
    public static Matcher<String> isInteger() {
        return new IsIntegerStringMatcher();
    }
}
