package eu.xenit.alfred.telemetry.binder.care4alf;

import eu.xenit.alfred.telemetry.binder.TicketMetrics;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.MeterFilterReply;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 * {@link MeterFilter} which adapts the {@link TicketMetrics} to their Care4Alf legacy equivalents
 */
public class TicketMetricsMeterFilter implements MeterFilter {

    public final static String METRIC_CARE4ALF_NAME_TICKETS = "users.tickets";

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
        return new Meter.Id(
                METRIC_CARE4ALF_NAME_TICKETS,
                removeTags(id.getTags(), TicketMetrics.METRIC_TAG_NAME_EXPIRATION_STATUS),
                id.getBaseUnit(),
                id.getDescription(),
                id.getType()
        );
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

    private static List<Tag> removeTags(final List<Tag> tags, final String... tagKeys) {
        return tags.stream()
                .filter(t -> {
                    for (String tagKey : tagKeys) {
                        if (t.getKey().equals(tagKey)) {
                            return false;
                        }
                    }
                    return true;
                }).collect(Collectors.toList());
    }
}
