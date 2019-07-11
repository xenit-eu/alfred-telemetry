package eu.xenit.alfred.telemetry.binder;

import java.util.Collections;
import java.util.List;
import org.alfresco.enterprise.repo.cluster.core.ClusterServiceInitialisedEvent;
import org.springframework.context.ApplicationEvent;

public class TestEventTriggeredMetrics extends BasicTestMetrics implements EventTriggeredMeterBinder {

    private boolean triggerOnStartup = true;

    @Override
    public List<Class<? extends ApplicationEvent>> triggeringEvents() {
        return Collections.singletonList(ClusterServiceInitialisedEvent.class);
    }

    @Override
    public boolean triggerOnStartup() {
        return triggerOnStartup;
    }

    public void setTriggerOnStartup(boolean triggerOnStartup) {
        this.triggerOnStartup = triggerOnStartup;
    }
}
