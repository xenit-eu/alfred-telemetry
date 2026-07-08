package eu.xenit.alfred.telemetry.integrationtesting;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

class AdminConsoleEndpointTest extends RestAssuredTest {

    @Test
    void adminConsolePageRenders() {
        // The /enterprise/admin/ prefix is a legacy namespace — the admin console is accessible
        // on both community and enterprise editions in ACS 23.x+.
        ExtractableResponse<Response> response =
                given()
                        .log().ifValidationFails()
                        .when()
                        .get("s/enterprise/admin/alfred-telemetry")
                        .then()
                        .log().ifValidationFails()
                        .statusCode(HttpStatus.SC_OK)
                        .extract();

        // Verify the page rendered successfully, including the help link which requires
        // the documentationUrl FreeMarker method (introduced in ACS 25.3 admin-template.ftl).
        // A missing or misconfigured bean would cause a FreeMarker TemplateException here.
        String body = response.body().asString();
        assertThat(body, containsString("alfred-telemetry"));
    }

}
