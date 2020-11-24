package eu.xenit.alfred.telemetry.integrationtesting;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.isOneOf;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

public class SolrPrometheusEndpointTest extends RestAssuredTestSolr {

    @Test
    void solrEndpoint() {
        try {
            Thread.currentThread().sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ExtractableResponse<Response> response =
                given()
                        .log().ifValidationFails()
                        .when()
                        .get("/alfresco/metrics?wt=dummy")
                        .then()
                        .log().ifValidationFails()
                        .statusCode(isOneOf(200))
                        .extract();

        String responseBody = response.body().asString();
        assertThat(responseBody, containsString("alfresco_nodes"));
    }

}
