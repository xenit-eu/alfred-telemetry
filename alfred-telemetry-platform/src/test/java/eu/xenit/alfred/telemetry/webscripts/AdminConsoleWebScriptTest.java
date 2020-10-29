package eu.xenit.alfred.telemetry.webscripts;

import static eu.xenit.alfred.telemetry.hamcrest.matchers.StringMatchers.isBoolean;
import static eu.xenit.alfred.telemetry.hamcrest.matchers.StringMatchers.isInteger;
import static eu.xenit.alfred.telemetry.webscripts.stubs.WebScriptTestInfrastructure.container;

import static eu.xenit.alfred.telemetry.webscripts.stubs.WebScriptTestInfrastructure.description;
import static eu.xenit.alfred.telemetry.webscripts.stubs.WebScriptTestInfrastructure.freemarkerProcess;
import static eu.xenit.alfred.telemetry.webscripts.stubs.WebScriptTestInfrastructure.request;
import static eu.xenit.alfred.telemetry.webscripts.stubs.WebScriptTestInfrastructure.templatePath;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThat;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Properties;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

class AdminConsoleWebScriptTest {

    @Test
    void testWebscriptModel() {
        AdminConsoleWebScript webscript = new AdminConsoleWebScript(new SimpleMeterRegistry(), defaultProperties());
        Map<String, Object> model = webscript.executeImpl(null, new Status(), new Cache());

        assertThat(getProperties(model, "registryPrometheus"), isValidPrometheusConfig());
        assertThat(getProperties(model, "registryJmx"), isValidJmxConfig());
        assertThat(getProperties(model, "registryGraphite"), isValidGraphiteConfig());

        assertThat(getProperties(model, "alfrescoIntegration"), isValidAlfrescoIntegrationConfig());
        assertThat(model, hasKey("meters"));
    }

    @Test
    void testRenderTemplate() throws IOException {
        AdminConsoleWebScript webscript = new AdminConsoleWebScript(new SimpleMeterRegistry(), defaultProperties());

        String desc = "org/alfresco/enterprise/repository/admin/support-tools/alfred-telemetry-admin-console.get.desc.xml";
        webscript.init(container(), description(desc));

        WebScriptRequest request = request(webscript, "/enterprise/admin/alfred-telemetry");
        Map<String, Object> model = webscript.executeImpl(request, new Status(), new Cache());
        Map<String, Object> templateModel = webscript.createTemplateModel(request, null, model);

        String result = freemarkerProcess(templateModel, templatePath(webscript));

        assertThat(result, is(notNullValue()));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> getProperties(Map<String, Object> model, String topicName) {
        assertThat(model.get(topicName), notNullValue());
        return (Map<String, String>) model.get(topicName);
    }

    private Matcher<Map<String, String>> isValidAlfrescoIntegrationConfig() {
        return allOf(
                hasEntry(is("alfred.telemetry.alfresco-integration.enabled"), isBoolean()),
                hasEntry(is("alfred.telemetry.alfresco-integration.include-default-alfresco-registry"), isBoolean())
        );
    }

    private Matcher<Map<String, String>> isValidPrometheusConfig() {
        return allOf(
                hasEntry(is("alfred.telemetry.export.prometheus.enabled"), isBoolean())
        );
    }

    private Matcher<Map<String, String>> isValidJmxConfig() {
        return allOf(
                hasEntry(is("alfred.telemetry.export.jmx.enabled"), isBoolean())
        );
    }

    private Matcher<Map<String, String>> isValidGraphiteConfig() {
        return allOf(
                hasEntry(is("alfred.telemetry.export.graphite.enabled"), isBoolean()),
                hasEntry(is("alfred.telemetry.export.graphite.step"), isInteger()),
                hasEntry(is("alfred.telemetry.export.graphite.host"), not(isEmptyString())),
                hasEntry(is("alfred.telemetry.export.graphite.port"), isInteger()),
                hasKey(is("alfred.telemetry.export.graphite.tags-as-prefix"))
        );
    }


    private static Properties defaultProperties() {
        Properties properties = new Properties();
        try (InputStream stream = AdminConsoleWebScript.class.getClassLoader()
                .getResourceAsStream("alfresco/module/alfred-telemetry-platform/alfresco-global.properties")) {
            assertThat(stream, notNullValue());
            properties.load(stream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return properties;
    }


}