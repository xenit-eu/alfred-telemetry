package eu.xenit.alfred.telemetry.integrationtesting;

import static io.restassured.RestAssured.preemptive;

import io.restassured.RestAssured;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RestAssuredTest {

    private static final Logger logger = LoggerFactory.getLogger(RestAssuredTest.class);

    private static final String ALFRESCO_USERNAME = "admin";
    private static final String ALFRESCO_PASSWORD = "admin";
    private static int expectedStatusCode = HttpStatus.SC_OK;

    @BeforeAll
    public static void initializeRestAssured() {
        logger.info("Initializing REST-Assured for smoke tests");

        final String baseURI = "http://" + System.getProperty("alfresco.host", "localhost");
        RestAssured.baseURI = baseURI;
        int port = Integer.parseInt(System.getProperty("alfresco.tcp.8080", "8080"));
        RestAssured.port = port;
        final String basePath = "/alfresco";
        RestAssured.basePath = basePath;

        final String alfrescoEdition = System.getProperty("alfrescoEdition");
        if(!"enterprise".equalsIgnoreCase(alfrescoEdition)) {
            expectedStatusCode = HttpStatus.SC_NOT_FOUND;
        }

        logger.info("REST-Assured initialized with following URI: {}:{}{}", baseURI, port, basePath);

        RestAssured.authentication = preemptive().basic(ALFRESCO_USERNAME, ALFRESCO_PASSWORD);
    }

    protected int getExpectedStatusCode() {
        return expectedStatusCode;
    }

}
