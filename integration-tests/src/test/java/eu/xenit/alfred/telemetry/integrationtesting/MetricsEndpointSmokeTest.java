package eu.xenit.alfred.telemetry.integrationtesting;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class MetricsEndpointSmokeTest extends RestAssuredTest {

    static Stream<String> expectedMeters() {
        return Stream.of(
                "jvm.buffer.count",
                "jvm.buffer.memory.used",
                "jvm.buffer.total.capacity",
                "jvm.classes.loaded",
                "jvm.classes.unloaded",
                "jvm.gc.live.data.size",
                "jvm.gc.max.data.size",
                "jvm.gc.memory.allocated",
                "jvm.gc.memory.promoted",
                "jvm.memory.committed",
                "jvm.memory.max",
                "jvm.memory.used",
                "jvm.threads.daemon",
                "jvm.threads.live",
                "jvm.threads.peak",
                "process.uptime",
                "process.start.time",
                "system.load.average.1m",
                "system.cpu.usage",
                "system.cpu.count",
                "process.cpu.usage",
                "process.files.open",
                "process.files.max",
                "jdbc.connections.usage",
                "jdbc.connections.count",
                "jdbc.connections.max",
                "jdbc.connections.min",
                "users.tickets.count"
        );
    }

    @Test
    void metersListedInMetricsEndpoint() {
        final List<String> availableMeters = given()
                .log().ifValidationFails()
                .when()
                .get("s/alfred/telemetry/metrics")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("names");

        expectedMeters().forEach(expected ->
                assertThat(
                        "The metrics endpoint should contain meter '" + expected + "'",
                        availableMeters, hasItem(expected))
        );
    }

    @ParameterizedTest
    @MethodSource("expectedMeters")
    void meterOverview(String expectedMeter) {
        given()
                .log().ifValidationFails()
                .when()
                .pathParam("meterName", expectedMeter)
                .get("s/alfred/telemetry/metrics/{meterName}")
                .then()
                .log().ifValidationFails()
                .statusCode(200);
    }

}
