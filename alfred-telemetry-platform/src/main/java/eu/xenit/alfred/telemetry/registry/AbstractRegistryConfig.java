package eu.xenit.alfred.telemetry.registry;

public abstract class AbstractRegistryConfig {

    private boolean enabled;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
