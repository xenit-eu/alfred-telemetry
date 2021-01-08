package eu.xenit.alfred.telemetry.webscripts.console;

import static eu.xenit.alfred.telemetry.hamcrest.matchers.StringMatchers.isBoolean;
import static eu.xenit.alfred.telemetry.hamcrest.matchers.StringMatchers.isInteger;
import static eu.xenit.alfred.telemetry.webscripts.stubs.WebScriptTestInfrastructure.container;
import static eu.xenit.alfred.telemetry.webscripts.stubs.WebScriptTestInfrastructure.description;
import static eu.xenit.alfred.telemetry.webscripts.stubs.WebScriptTestInfrastructure.freemarkerProcess;
import static eu.xenit.alfred.telemetry.webscripts.stubs.WebScriptTestInfrastructure.request;
import static eu.xenit.alfred.telemetry.webscripts.stubs.WebScriptTestInfrastructure.templatePath;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThat;

import eu.xenit.alfred.telemetry.webscripts.console.AdminConsoleWebscriptResponseModel.TelemetryRegistryModel;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Properties;
import org.alfresco.repo.module.ModuleDetailsImpl;
import org.alfresco.repo.module.ModuleVersionNumber;
import org.alfresco.service.cmr.module.ModuleDetails;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

class AdminConsoleWebScriptTest {

    @Test
    void testObsoleteWebscriptModel() {
        Map<String, Object> model = adminConsoleWebscript().executeImpl(null, new Status(), new Cache());

        assertThat(getProperties(model, "alfrescoIntegration"), isValidAlfrescoIntegrationConfig());
        assertThat(model, hasKey("meters"));
    }

    @Test
    void model_telemetry() {
        Map<String, Object> model = adminConsoleWebscript().executeImpl(null, new Status(), new Cache());

        assertThat(model, hasEntry(is("telemetry"), is(instanceOf(AdminConsoleWebscriptResponseModel.class))));
        assertThat(model.get("telemetry"), is(notNullValue()));
    }

    @Test
    void model_telemetry_module() {
        AdminConsoleWebscriptResponseModel response = adminConsoleWebscript("5.6.7").createResponseModel();

        assertThat(response.getModule(), is(notNullValue()));
        assertThat(response.getModule().getId(), is("alfred-telemetry-platform"));
        assertThat(response.getModule().getVersion(), is("5.6.7"));
    }

    @Test
    void model_telemetry_registries_jmx() {
        AdminConsoleWebscriptResponseModel response = adminConsoleWebscript().createResponseModel();

        TelemetryRegistryModel jmx = response.getRegistries().get("jmx");
        assertThat(jmx.isEnabled(), is(true));
        assertThat(jmx.getProperties(), isValidJmxConfig());
    }

    @Test
    void model_telemetry_registries_prometheus() {
        AdminConsoleWebscriptResponseModel response = adminConsoleWebscript().createResponseModel();

        TelemetryRegistryModel jmx = response.getRegistries().get("prometheus");
        assertThat(jmx.isEnabled(), is(true));
        assertThat(jmx.getProperties(), isValidPrometheusConfig());
    }

    @Test
    void model_telemetry_registries_graphite() {
        AdminConsoleWebscriptResponseModel response = adminConsoleWebscript().createResponseModel();

        TelemetryRegistryModel jmx = response.getRegistries().get("graphite");
        assertThat(jmx.isEnabled(), is(true));
        assertThat(jmx.getProperties(), isValidGraphiteConfig());
    }


    @Test
    void model_telemetry_dependencies() {
        AdminConsoleWebscriptResponseModel response = adminConsoleWebscript().createResponseModel();

        assertThat(response.getDependencies(), is(notNullValue()));
    }

    @Test
    void model_telemetry_binders() {
        AdminConsoleWebscriptResponseModel response = adminConsoleWebscript().createResponseModel();

        assertThat(response.getBinder(), is(notNullValue()));
    }


    @Test
    void testRenderTemplate() throws IOException {
        AdminConsoleWebScript webscript = adminConsoleWebscript();

        WebScriptRequest request = request(webscript, "/enterprise/admin/alfred-telemetry");
        Map<String, Object> model = webscript.executeImpl(request, new Status(), new Cache());
        Map<String, Object> templateModel = webscript.createTemplateModel(request, null, model);

        String result = freemarkerProcess(templateModel, templatePath(webscript));

        assertThat(result, is(notNullValue()));
    }

    private static AdminConsoleWebScript adminConsoleWebscript() {
        return adminConsoleWebscript("1.2.3");
    }

    private static AdminConsoleWebScript adminConsoleWebscript(String version) {
        AdminConsoleWebScript webscript = new AdminConsoleWebScript(
                new SimpleMeterRegistry(),
                defaultProperties(),
                moduleDetails(version));

        String desc = "org/alfresco/enterprise/repository/admin/support-tools/alfred-telemetry-admin-console.get.desc.xml";
        webscript.init(container(), description(desc));
        return webscript;
    }



    @SuppressWarnings("unchecked")
    private static Map<String, String> getProperties(Map<String, Object> model, String topicName) {
        assertThat(model.get(topicName), notNullValue());
        return (Map<String, String>) model.get(topicName);
    }

    private Matcher<Map<String, String>> isValidAlfrescoIntegrationConfig() {
        return allOf(
                hasEntry(is("alfred.telemetry.alfresco-integration.enabled"), isBoolean()),
                hasEntry(is("alfred.telemetry.alfresco-integration.use-default-alfresco-registry"), isBoolean())
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

    private static ModuleDetails moduleDetails(String version) {
        return new ModuleDetailsImpl(
                AdminConsoleWebScript.MODULE_ID,
                new ModuleVersionNumber(version),
                "title",
                "description");
    }


}