package eu.xenit.alfred.telemetry.hamcrest.matchers;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class IsBooleanStringMatcher extends TypeSafeMatcher<String> {

    @Override
    protected boolean matchesSafely(String item) {
        return "true".equalsIgnoreCase(item) || "false".equalsIgnoreCase(item);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("boolean string");
    }

    /**
     * Creates a matcher that matches if the examined object is a case insensitive string representation of a boolean.
     * <p/>
     * For example:
     * <pre>assertThat("true", isBoolean())</pre>
     */
    @Factory
    static Matcher<String> isBoolean() {
        return new IsBooleanStringMatcher();
    }
}
