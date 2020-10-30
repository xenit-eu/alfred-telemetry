package eu.xenit.alfred.telemetry.webscripts.console;

import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class AdminConsoleWebscriptResponseModel {

    private final AlfredTelemetryModule module;
    private final Map<String, TelemetryRegistryModel> registries;
    private final Map<String, TelemetryDependencyModel> dependencies;
    private final Map<String, TelemetryBinderConfigModel> binders;

    public AdminConsoleWebscriptResponseModel(
            AlfredTelemetryModule module,
            Map<String, TelemetryRegistryModel> registries,
            Map<String, TelemetryDependencyModel> dependencies,
            Map<String, TelemetryBinderConfigModel> binders) {
        this.module = module;
        this.registries = registries;
        this.dependencies = dependencies;
        this.binders = binders;
    }

    public AlfredTelemetryModule getModule() {
        return module;
    }

    public Map<String, TelemetryRegistryModel> getRegistries() {
        return registries;
    }

    public Map<String, TelemetryDependencyModel> getDependencies() {
        return this.dependencies;
    }

    public Map<String, TelemetryBinderConfigModel> getBinder() {
        return this.binders;
    }

    public static class TelemetryRegistryModel {
        private final String id;
        private final String prefix;
        private final Map<String, String> properties;

        public TelemetryRegistryModel(String id, String prefix, Map<String, String> properties) {
            this.id = id;
            this.prefix = prefix;
            this.properties = properties;
        }

        public String getId() {
            return id;
        }

        public Map<String, String> getProperties() {
            return properties;
        }

        public boolean isEnabled() {
            String property = this.getProperty("enabled", "false");
            return Boolean.parseBoolean(property);
        }

        public String getProperty(String name) {
            return this.properties.get(prefixed(name));
        }

        public String getProperty(String name, String defaultValue) {
            return this.properties.getOrDefault(prefixed(name), defaultValue);
        }

        private String prefixed(String name) {
            return this.prefix + this.id + "." + name;
        }
    }
    public static class AlfredTelemetryModule {

        private final String id;
        private final String version;

        public AlfredTelemetryModule(String id, String version) {
            this.id = id;
            this.version = version;
        }

        public String getId() {
            return id;
        }

        public String getVersion() {
            return version;
        }
    }

    public static class TelemetryDependencyModel {
        private final String id;
        private final String version;

        public TelemetryDependencyModel(String id, String version) {
            this.id = id;
            this.version = version;
        }

        public String getId() {
            return id;
        }

        public String getVersion() {
            return version;
        }
    }

    public static class TelemetryBinderConfigModel {

        private final Properties properties;
        private final String prefix;
        private final String name;

        public TelemetryBinderConfigModel(String name, String prefix, Properties properties) {
            this.name = Objects.requireNonNull(name, "Argument 'name' is required");;
            this.prefix = Objects.requireNonNull(prefix, "Argument 'prefix' is required");;
            this.properties =  Objects.requireNonNull(properties, "Argument 'properties' is required");;
        }

        public String getName() {
            return name;
        }

        public boolean isEnabled() {
            String property = this.getProperty("enabled", "false");
            return Boolean.parseBoolean(property);
        }

        public String getProperty(String name) {
            return this.properties.getProperty(prefixed(this.prefix, name));
        }
        public String getProperty(String name, String defaultValue) {
            return this.properties.getProperty(prefixed(this.prefix, name), defaultValue);
        }

        private static String prefixed(String prefix, String name) {
            return prefix + "." + name;
        }


    }
}
