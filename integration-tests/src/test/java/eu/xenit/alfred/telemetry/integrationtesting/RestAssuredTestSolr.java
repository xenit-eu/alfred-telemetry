package eu.xenit.alfred.telemetry.integrationtesting;

import io.restassured.RestAssured;
import io.restassured.config.RestAssuredConfig;
import io.restassured.config.SSLConfig;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public abstract class RestAssuredTestSolr {

    private static final Logger logger = LoggerFactory.getLogger(RestAssuredTestSolr.class);

    private static final String sslCertType = "PKCS12";
    private static final String sslCertPath = "alfresco-client.p12";
    private static final String sslCertPassword = "alfresco";
    private static final char[] sslCertPasswordChars = sslCertPassword.toCharArray();

    @BeforeAll
    public static void initializeRestAssured() {
        logger.info("Initializing REST-Assured for smoke tests");

        RestAssured.config = sslConfig();

        final String baseURI = "https://" + System.getProperty("solr.host", "localhost");
        RestAssured.baseURI = baseURI;
        int port = Integer.parseInt(System.getProperty("solr.tcp.8443", "8443"));
        RestAssured.port = port;
        final String solrFlavor = System.getProperty("solrFlavor","/solr");
        final String basePath = ("solr4".equals(solrFlavor))? "/solr4" : "/solr";
        RestAssured.basePath = basePath;

        logger.info("REST-Assured initialized with following URI: {}:{}{}", baseURI, port, basePath);
    }

    private static RestAssuredConfig sslConfig() {
        try {
            File storeFile = new File(RestAssuredTestSolr.class.getClassLoader().getResource(sslCertPath).getFile());
            KeyStore store = loadKeyStore(
                    sslCertType,
                    storeFile,
                    sslCertPasswordChars);
            SSLConfig sslConfig = RestAssuredConfig.config()
                    .getSSLConfig()
                    .allowAllHostnames()
                    .trustStore(store)
                    .keyStore(storeFile, sslCertPassword);
            return RestAssured.config().sslConfig(sslConfig);
        } catch (Exception e) {
            logger.error("Error opening the key file", e);
            return null;
        }
    }

    private static KeyStore loadKeyStore(String keystoreType, File file, char[] password)
            throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        KeyStore keyStore = KeyStore.getInstance(keystoreType);
        try(InputStream stream = new FileInputStream(file)) {
            keyStore.load(stream, password);
        }
        return keyStore;
    }
}
