package eu.xenit.alfred.telemetry.binder;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import javax.annotation.Nonnull;
import org.alfresco.repo.security.authentication.TicketComponent;

public class TicketMetrics implements MeterBinder {

    public final static String METRIC_NAME_TICKETS = "users.tickets.count";

    public final static String METRIC_TAG_NAME_EXPIRATION_STATUS = "status";
    public final static String METRIC_TAG_VALUE_EXPIRED = "expired";
    public final static String METRIC_TAG_VALUE_NON_EXPIRED = "valid";

    private TicketComponent ticketComponent;

    public TicketMetrics(TicketComponent ticketComponent) {
        this.ticketComponent = ticketComponent;
    }

    @Override
    public void bindTo(@Nonnull MeterRegistry registry) {
        Gauge.builder(METRIC_NAME_TICKETS, ticketComponent, TicketMetrics::getNonExpiredTicketsCount)
                .description("The total number of users with a non expired ticket")
                .tag(METRIC_TAG_NAME_EXPIRATION_STATUS, METRIC_TAG_VALUE_NON_EXPIRED)
                .register(registry);

        Gauge.builder(METRIC_NAME_TICKETS, ticketComponent, TicketMetrics::getExpiredTicketsCount)
                .description("The total number of users with an expired ticket")
                .tag(METRIC_TAG_NAME_EXPIRATION_STATUS, METRIC_TAG_VALUE_EXPIRED)
                .register(registry);
    }

    private static int getNonExpiredTicketsCount(final TicketComponent ticketComponent) {
        return ticketComponent.getUsersWithTickets(true).size();
    }

    private static long getExpiredTicketsCount(final TicketComponent ticketComponent) {
        return ticketComponent.getUsersWithTickets(false).size()
                - ticketComponent.getUsersWithTickets(true).size();
    }
}
