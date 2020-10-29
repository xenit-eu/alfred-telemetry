package eu.xenit.alfred.telemetry.webscripts.stubs;

import static eu.xenit.alfred.telemetry.webscripts.stubs.ServerModelStub.enterprise_52;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.extensions.webscripts.DeclarativeRegistry.WEBSCRIPT_DESC_XML;

import eu.xenit.alfred.telemetry.webscripts.stubs.WebScriptRequestStub;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.dom4j.DocumentException;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.ClassPathStore;
import org.springframework.extensions.webscripts.Container;
import org.springframework.extensions.webscripts.Description;
import org.springframework.extensions.webscripts.DescriptionImpl;
import org.springframework.extensions.webscripts.FormatRegistry;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Runtime;
import org.springframework.extensions.webscripts.SearchPath;
import org.springframework.extensions.webscripts.ServerModel;
import org.springframework.extensions.webscripts.Store;
import org.springframework.extensions.webscripts.UriTemplate;
import org.springframework.extensions.webscripts.WebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.processor.FTLTemplateProcessor;

public class WebScriptTestInfrastructure {

    @Test
    void alfrescoRemoteApi_onTestRuntimeClasspath() throws IOException {
        assertThat(
                "check that 'org.alfresco:alfresco-remote-api' is available on the test-runtime-classpath",
                searchPath().hasDocument("org/alfresco/repository/admin/admin-template.ftl"),
                is(true));
    }

    private static String[] classpaths() {
        return new String[] { "alfresco/templates/webscripts" };
    }

    public static FTLTemplateProcessor freemarker() {
        FTLTemplateProcessor freemarker = new FTLTemplateProcessor();
        freemarker.setSearchPath(searchPath());
        freemarker.init();
        return freemarker;
    }

    public static Container container() {
        Container container = mock(Container.class);
        when(container.getSearchPath()).thenReturn(searchPath());
        when(container.getFormatRegistry()).thenReturn(new FormatRegistry());
        when(container.getTemplateParameters()).thenReturn(templateParams(enterprise_52()));
        return container;
    }

    public static Map<String, Object> templateParams(ServerModel serverModel) {
        Map<String, Object> params = new HashMap<String, Object>(8, 1.0f);
        params.put("server", serverModel);
        params.put("date", new Date());

        params.put("argreplace", new org.springframework.extensions.webscripts.ArgReplaceMethod());
        params.put("encodeuri", new org.springframework.extensions.webscripts.UrlEncodeMethod());
        params.put("dateCompare", new org.springframework.extensions.webscripts.DateCompareMethod());
        params.put("xmldate", new org.springframework.extensions.webscripts.ISO8601DateFormatMethod());
        params.put("jsonUtils", new org.springframework.extensions.webscripts.json.JSONUtils());
        params.put("stringUtils", new org.springframework.extensions.webscripts.ScriptableUtils());

        return Collections.unmodifiableMap(params);
    }

    public static WebScriptRequest request(WebScript webscript, String uri) {
        Match match = match(webscript, uri);

        Runtime runtime = mock(Runtime.class);
        when(runtime.getTemplateParameters()).thenReturn(new HashMap<>());

        return new WebScriptRequestStub(runtime, uri, match);
    }

    private static Match match(WebScript webScript, String uri) {
        // the webscript description contains a bunch of templates
        // we need to find the one that 'match's with 'uri'
        return Stream.of(webScript.getDescription().getURIs()).map(descriptorUri -> {
            UriTemplate template = new UriTemplate(descriptorUri);
            Map<String, String> vars = template.match(uri);
            if (vars != null) {

                int firstTokenIdx = template.getTemplate().indexOf('{');
                String staticTemplate = (firstTokenIdx == -1) ? template.getTemplate()
                        : template.getTemplate().substring(0, firstTokenIdx);

                return new Match(template.getTemplate(), vars, staticTemplate, webScript);
            }

            return null;
        })

                // returns null if there is no match with the UriTemplate
                .filter(Objects::nonNull)

                // we want to return the first match
                .findFirst()

                .orElseThrow(() -> new WebScriptException("URI does not match WebScript: " + uri));
    }

    public static SearchPath searchPath() {
        return searchPath(classpaths());
    }

    private static SearchPath searchPath(String... classpaths) {
        List<Store> stores = Stream.of(classpaths)
                .map(cp -> classpathStore(cp, false))
                .collect(Collectors.toList());

        SearchPath searchPath = new SearchPath();
        searchPath.setSearchPath(stores);

        searchPath.getStores().forEach(Store::init);
        return searchPath;
    }

    private static Store classpathStore(String classpath, boolean mustExists) {
        ClassPathStore store = new ClassPathStore();
        store.setClassPath(classpath);
        store.setMustExist(mustExists);

        // sigh, they require a full ApplicationContext, while they only need a `ResourceLoader`
        store.setApplicationContext(new GenericApplicationContext());
        store.init();

        return store;
    }

    public static Description description(String path) {
        return description("alfresco/templates/webscripts", path);
    }

    protected static Description description(String classpath, String path) {

        String id = path.substring(0, path.lastIndexOf(WEBSCRIPT_DESC_XML));
        String method = id.substring(id.lastIndexOf('.') + 1).toUpperCase();

        path = Paths.get(classpath, path).toString();
        try (InputStream stream = Description.class.getClassLoader().getResourceAsStream(path)) {
            DescriptionImpl description = DescriptionImpl.newInstance();
            description.setId(id);
            description.setMethod(method);
            description.parseDocument(stream);
            return description;
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String templatePath(AbstractWebScript webscript) {
        return templatePath(webscript, "ftl");
    }

    protected static String templatePath(AbstractWebScript webscript, String templateFormat) {
        return webscript.getDescription().getId()
                + "." + webscript.getDescription().getDefaultFormat()
                + "." + templateFormat;
    }

    public static String freemarkerProcess(Map<String, Object> templateModel, String templatePath) throws IOException {
        try (Writer writer = new StringWriter()) {
            freemarker().process(templatePath, templateModel, writer);

            return writer.toString();
        }
    }
}
