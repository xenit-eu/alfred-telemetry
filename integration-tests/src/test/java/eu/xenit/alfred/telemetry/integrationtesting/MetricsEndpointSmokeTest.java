package eu.xenit.alfred.telemetry.integrationtesting;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

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

    static Stream<String> expectedMetersEnterprise() {
        return Stream.of(
                "license.valid",
                "license.users",
                "license.users.max",
                "license.docs.max",
                "license.days",
                "license.cluster.enabled",
                "license.encryption.enabled",
                "license.heartbeat.enabled"
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

    @Test
    @EnabledIfSystemProperty(named="alfrescoEdition",matches = "Enterprise")
    void metersEnterpriseListedInMetricsEndpoint() {
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

    @ParameterizedTest
    @MethodSource("expectedMetersEnterprise")
    @EnabledIfSystemProperty(named="alfrescoEdition",matches = "Enterprise")
    void meterEnterpriseOverview(String expectedMeter) {
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
