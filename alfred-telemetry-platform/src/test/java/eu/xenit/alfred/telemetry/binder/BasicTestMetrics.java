package eu.xenit.alfred.telemetry.binder;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.nullValue;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;

public class BasicTestMetrics implements MeterBinder {

    private AtomicInteger bindToExecutions = new AtomicInteger(0);

    static final String COUNTER_NAME = "BasicTestMetricsCounter";

    @Override
    public void bindTo(@Nonnull MeterRegistry registry) {
        bindToExecutions.getAndIncrement();

        Counter.builder(COUNTER_NAME)
                .register(registry);
    }

    int getBindToExecutions() {
        assertThat(bindToExecutions, is(not(nullValue())));
        return bindToExecutions.get();
    }

}
