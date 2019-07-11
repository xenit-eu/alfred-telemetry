package eu.xenit.alfred.telemetry.binder.care4alf;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import eu.xenit.alfred.telemetry.binder.TicketMetrics;
import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.Meter.Type;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.MeterFilterReply;
import org.junit.jupiter.api.Test;

class TicketMetricsMeterFilterTest {

    private MeterFilter filter = new TicketMetricsMeterFilter();

    @Test
    void map_usersTicketsCountNonExpired_to_usersTickets() {
        final Id id = new Id(TicketMetrics.METRIC_NAME_TICKETS,
                Tags.of(Tag.of(TicketMetrics.METRIC_TAG_NAME_EXPIRATION_STATUS,
                        TicketMetrics.METRIC_TAG_VALUE_NON_EXPIRED)),
                null,
                null,
                Type.GAUGE);

        final Id mappedId = filter.map(id);

        assertThat(mappedId.getName(), is(TicketMetricsMeterFilter.METRIC_CARE4ALF_NAME_TICKETS));
    }

    @Test
    void map_usersTicketsCountExpired_notMapped() {
        final Id id = new Id(TicketMetrics.METRIC_NAME_TICKETS,
                Tags.of(Tag.of(TicketMetrics.METRIC_TAG_NAME_EXPIRATION_STATUS,
                        TicketMetrics.METRIC_TAG_VALUE_EXPIRED)),
                null,
                null,
                Type.GAUGE);

        final Id mappedId = filter.map(id);

        assertThat(mappedId, is(id));
    }

    @Test
    void filter_usersTicketsCountNonExpired_notDenied() {
        final Id id = new Id(TicketMetrics.METRIC_NAME_TICKETS,
                Tags.of(Tag.of(TicketMetrics.METRIC_TAG_NAME_EXPIRATION_STATUS,
                        TicketMetrics.METRIC_TAG_VALUE_NON_EXPIRED)),
                null,
                null,
                Type.GAUGE);

        final MeterFilterReply reply = filter.accept(id);

        assertThat(reply, is(not(MeterFilterReply.DENY)));
    }

    @Test
    void filter_usersTicketsCountExpired_denied() {
        final Id id = new Id(TicketMetrics.METRIC_NAME_TICKETS,
                Tags.of(Tag.of(TicketMetrics.METRIC_TAG_NAME_EXPIRATION_STATUS,
                        TicketMetrics.METRIC_TAG_VALUE_EXPIRED)),
                null,
                null,
                Type.GAUGE);

        final MeterFilterReply reply = filter.accept(id);

        assertThat(reply, is(MeterFilterReply.DENY));
    }

}