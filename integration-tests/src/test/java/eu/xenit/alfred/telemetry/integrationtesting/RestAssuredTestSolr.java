package eu.xenit.alfred.telemetry.integrationtesting;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RestAssuredTestSolr {

    private static final Logger logger = LoggerFactory.getLogger(RestAssuredTestSolr.class);

    @BeforeAll
    public static void initializeRestAssured() {
        logger.info("Initializing REST-Assured for smoke tests");

        final String baseURI = "http://" + System.getProperty("solr.host", "localhost");
        RestAssured.baseURI = baseURI;
        int port = Integer.parseInt(System.getProperty("solr.tcp.8080", "8080"));
        RestAssured.port = port;
        final String solrFlavor = System.getProperty("solrFlavor","/solr");
        final String basePath = ("solr4".equals(solrFlavor))? "/solr4" : "/solr";
        RestAssured.basePath = basePath;

        logger.info("REST-Assured initialized with following URI: {}:{}{}", baseURI, port, basePath);
    }

}
