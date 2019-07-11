package eu.xenit.alfred.telemetry.util;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class VersionUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(VersionUtil.class);

    public static final String MODULE_MICROMETER_CORE = "micrometer-core";

    private VersionUtil() {
        // private ctor to hide implicit public one
    }

    public static Version getMicrometerCoreVersion() {
        return getMicrometerModuleVersion(MODULE_MICROMETER_CORE);
    }

    /**
     * Relies on the fact that Micrometer includes a /META-INF/${module-name}.properties file in it's artifacts.
     * <p>
     * This method parses the 'Implementation-Version' property of the /META-INF/${module-name}.properties file into a
     * {@link Version} object. If the properties file is not available on the classpath, or the properties file doesn't
     * contain a parseable version,
     *
     * @param moduleName the name of the micrometer module for which we want to retrieve the version, e.g.
     * micrometer-core, micrometer-registry-jmx, ... .
     * @return {@code null} if the version could not be determined, the {@link Version} of the Micrometer module
     * otherwise.
     */
    @Nullable
    public static Version getMicrometerModuleVersion(final String moduleName) {
        try {
            return extractVersionFromResource(
                    new PathMatchingResourcePatternResolver()
                            .getResource("classpath:/META-INF/" + moduleName + ".properties"));
        } catch (IOException e) {
            LOGGER.warn("Unable to retrieve the version of module '{}'", moduleName, e);
            return null;
        }
    }

    private static Version extractVersionFromResource(final Resource resource) throws IOException {
        final Properties p = new Properties();
        p.load(resource.getInputStream());

        final String implementationVersion = p.getProperty("Implementation-Version");
        if (implementationVersion == null) {
            throw new IllegalArgumentException(
                    "No 'Implementation-Version' found in file '" + resource.getFile().getAbsolutePath() + "'");
        }
        return Version.fromString(implementationVersion);
    }

    public static class Version {

        private final int major;
        private final int minor;
        private final int patch;

        private final String versionString;

        private Version(int major, int minor, int patch, String versionString) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
            this.versionString = versionString;
        }

        public static Version fromString(@Nonnull final String versionString) {
            final String[] parts = versionString.split("\\.");

            int major = parts.length > 0 ? Integer.valueOf(parts[0]) : 0;
            int minor = parts.length > 1 ? Integer.valueOf(parts[1]) : 0;
            int patch = parts.length > 2 ? Integer.valueOf(parts[2]) : 0;

            return new Version(major, minor, patch, versionString);
        }

        public boolean isCompatible(final @Nonnull Version other) {
            return (this.major == other.major) && (this.minor == other.minor);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Version version = (Version) o;
            return major == version.major &&
                    minor == version.minor &&
                    patch == version.patch;
        }

        @Override
        public int hashCode() {
            return Objects.hash(major, minor, patch);
        }

        @Override
        public String toString() {
            return versionString;
        }
    }

}
