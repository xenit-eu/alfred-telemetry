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

    // This must stay the only call to this endpoint in the class: it doubles as a regression test
    // for a bug where the FIRST call returned a 500 because binding SolrSnapshotMetrics threw a
    // ClassCastException on the replication handler (fixed in ALFREDOPS-838 / v0.9.2). A second
    // call would silently pass even if snapshot metric registration were broken again, since the
    // handler only attempts to bind these metrics once per Solr core lifetime.
    @Test
    void solrEndpoint() {
        try {
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
            assertThat(responseBody, containsString("application=\"solr\""));
            assertThat(responseBody, containsString(",host=\""));
            assertThat(responseBody, containsString("snapshot_status"));
        } catch(Exception e) {
            throw e;
        }
    }

}
