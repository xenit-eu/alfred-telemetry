package eu.xenit.alfred.telemetry.hamcrest.matchers;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class IsIntegerStringMatcher extends TypeSafeMatcher<String> {

    @Override
    protected boolean matchesSafely(String item) {
        try {
            Integer.valueOf(item);
            return true;
        } catch (NumberFormatException nfe)
        {
            return false;
        }
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("integer string");
    }

    /**
     * Creates a matcher that matches if the examined object is a string that can be parsed as an integer.
     * <p/>
     * For example:
     * <pre>assertThat("8080", isInteger())</pre>
     */
    @Factory
    static Matcher<String> isInteger() {
        return new IsIntegerStringMatcher();
    }
}
