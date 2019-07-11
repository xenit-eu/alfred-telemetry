package eu.xenit.alfred.telemetry.binder;

public class NamedTestMetrics extends BasicTestMetrics implements NamedMeterBinder {

    private String name;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
