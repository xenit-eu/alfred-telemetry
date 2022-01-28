package eu.xenit.alfred.telemetry.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyOrNullString;

import eu.xenit.alfred.telemetry.util.MicrometerModules;
import eu.xenit.alfred.telemetry.util.MicrometerModules.Version;
import org.junit.jupiter.api.Test;

public class VersionUtilTest {

    public static Version getMicrometerVersionFromGradle() {
        final String micrometerVersionFromGradle = System.getProperty("micrometerVersion");
        assertThat("micrometerVersion should have been passed by Gradle",
                micrometerVersionFromGradle,
                is(not(isEmptyOrNullString()))
        );
        return Version.fromString(micrometerVersionFromGradle);
    }

    @Test
    void retrieveVersionOfMicrometerModule_core() {
        assertThat(
                MicrometerModules.getMicrometerModuleVersion("micrometer-core"),
                is(equalTo(getMicrometerVersionFromGradle()))
        );
    }

    @Test
    void retrieveVersionOfMicrometerModule_jmxRegistry() {
        assertThat(
                MicrometerModules.getMicrometerModuleVersion("micrometer-registry-jmx"),
                is(equalTo(getMicrometerVersionFromGradle()))
        );
    }
}