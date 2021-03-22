package eu.xenit.alfred.telemetry.integrationtesting;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.isOneOf;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolrPrometheusEndpointTest extends RestAssuredTestSolr {
    private static final Logger logger = LoggerFactory.getLogger(SolrPrometheusEndpointTest.class);

    @Test
    void solrEndpoint() {
        try {
            Thread.currentThread().sleep(20000);
        } catch (InterruptedException e) {
            logger.error("Fail to wait 20 sec", e);
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
