package eu.xenit.alfred.telemetry.binder.care4alf;

import eu.xenit.alfred.telemetry.binder.TicketMetrics;
import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.MeterFilterReply;
import javax.annotation.Nonnull;

/**
 * {@link MeterFilter} which adapts the {@link TicketMetrics} to their Care4Alf legacy equivalents
 */
public class TicketMetricsMeterFilter implements MeterFilter {

    public final static String METRIC_CARE4ALF_NAME_TICKETS = "users.tickets";

    private static final MeterFilter FILTER_IGNORE_EXP_STATUS =
            MeterFilter.ignoreTags(TicketMetrics.METRIC_TAG_NAME_EXPIRATION_STATUS);

    @Override
    @Nonnull
    public Id map(@Nonnull Id id) {
        if (!TicketMetrics.METRIC_NAME_TICKETS.equals(id.getName())) {
            return id;
        }
        final String expirationStatus = id.getTag(TicketMetrics.METRIC_TAG_NAME_EXPIRATION_STATUS);
        if (!TicketMetrics.METRIC_TAG_VALUE_NON_EXPIRED.equals(expirationStatus)) {
            return id;
        }

        id = FILTER_IGNORE_EXP_STATUS.map(id);
        return id.withName(METRIC_CARE4ALF_NAME_TICKETS);
    }

    @Override
    @Nonnull
    public MeterFilterReply accept(@Nonnull Id id) {
        if (TicketMetrics.METRIC_NAME_TICKETS.equals(id.getName())
                && !TicketMetrics.METRIC_TAG_VALUE_NON_EXPIRED
                .equals(id.getTag(TicketMetrics.METRIC_TAG_NAME_EXPIRATION_STATUS))) {
            return MeterFilterReply.DENY;
        }

        return MeterFilterReply.NEUTRAL;
    }
}
