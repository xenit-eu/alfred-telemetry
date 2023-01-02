package eu.xenit.alfred.telemetry.solr.monitoring.registry;

import java.time.Duration;

public class GraphiteConfig implements io.micrometer.graphite.GraphiteConfig  {

    private String host;
    private int port;
    private int step;
    private String[] tagsAsPrefix;

    public GraphiteConfig(String host, int port, int step, String[] tagsAsPrefix) {
        this.host = host;
        this.port = port;
        this.step = step;
        this.tagsAsPrefix = tagsAsPrefix;
    }

    @Override
    public String host() {
        return this.host;
    }

    @Override
    public int port() {
        return this.port;
    }

    @Override
    public Duration step() {
        return Duration.ofSeconds(this.step);
    }

    @Override
    public String get(String key) {
        return null;
    }

    @Override
    public String[] tagsAsPrefix() {
        return this.tagsAsPrefix;
    }
}
