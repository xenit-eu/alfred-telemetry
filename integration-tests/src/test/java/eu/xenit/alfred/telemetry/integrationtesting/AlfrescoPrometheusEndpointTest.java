package eu.xenit.alfred.telemetry.integrationtesting;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.isOneOf;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

public class AlfrescoPrometheusEndpointTest extends RestAssuredTest {

    @Test
    void alfrescoEndpointExposesAlfredTelemetryMetrics() {
        ExtractableResponse<Response> response =
                given()
                        .log().ifValidationFails()
                        .when()
                        .get("s/prometheus")
                        .then()
                        .log().ifValidationFails()
                        .statusCode(isOneOf(200, 404))
                        .extract();

        if (response.statusCode() == 404) {
            return;
        }

        String responseBody = response.body().asString();

        assertThat(responseBody, containsString("alfred_telemetry_registries_total"));
        assertThat(responseBody, containsString("application=\"alfresco\""));
        assertThat(responseBody, containsString(",host=\""));
    }

}
