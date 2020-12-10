package eu.xenit.alfred.telemetry.solr.monitoring.registry;

import eu.xenit.alfred.telemetry.solr.util.Util;
import java.util.List;

public class MyGraphiteConfig {

    private String host;
    private int port;
    private int step;
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

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public List<String> getTagsAsPrefix() {
        return tagsAsPrefix;
    }

    public void setTagsAsPrefix(String tagsAsPrefix) {
        this.tagsAsPrefix = Util.parseList(tagsAsPrefix);
    }
}
