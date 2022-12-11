package eu.xenit.alfred.telemetry.binder.dbcp;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TelemetryBasicDataSourceTest {

    static Stream<UsageSampleData> usageSampleData() {
        return Stream.of(
                new UsageSampleData(0D, 0D, 0D),
                new UsageSampleData(-1D, 0D, -1D),
                new UsageSampleData(4D, 2D, 0.5),
                new UsageSampleData(2D, 2D, 1D)
        );
    }

    @ParameterizedTest
    @MethodSource("usageSampleData")
    void testGetUsage(UsageSampleData data) {
        TelemetryBasicDataSource dataSource = mock(TelemetryBasicDataSource.class);
        when(dataSource.getMaxActive()).thenReturn(data.getMaxActive());
        when(dataSource.getNumActive()).thenReturn(data.getNumActive());
        assertEquals(TelemetryBasicDataSource.getUsage(dataSource), data.getExpected());
    }

    private static class UsageSampleData {
        private final double maxActive;
        private final double numActive;
        private final double expected;

        UsageSampleData(double maxActive, double numActive, double expected) {
            this.maxActive = maxActive;
            this.numActive = numActive;
            this.expected = expected;
        }

        private double getMaxActive() {
            return this.maxActive;
        }

        private double getNumActive() {
            return this.numActive;
        }

        private double getExpected() {
            return this.expected;
        }
    }
}
