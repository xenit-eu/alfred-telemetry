package eu.xenit.alfred.telemetry.registry.graphite;

import eu.xenit.alfred.telemetry.registry.AbstractRegistryConfig;
import eu.xenit.alfred.telemetry.util.DurationUtil;
import eu.xenit.alfred.telemetry.util.StringUtils;
import java.time.Duration;
import java.util.List;

@SuppressWarnings({"unused", "WeakerAccess"})
public class GraphiteConfig extends AbstractRegistryConfig {

    private String host;
    private int port;
    private Duration step;
    private List<String> tagsAsPrefix;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Duration getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = DurationUtil.parseDuration(step);
    }

    public List<String> getTagsAsPrefix() {
        return tagsAsPrefix;
    }

    public void setTagsAsPrefix(String tagsAsPrefix) {
        this.tagsAsPrefix = StringUtils.parseList(tagsAsPrefix);
    }
}
