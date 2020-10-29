package eu.xenit.alfred.telemetry.webscripts.stubs;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.surf.util.InputStreamContent;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Runtime;
import org.springframework.extensions.webscripts.WebScriptRequestURLImpl;

public class WebScriptRequestStub extends WebScriptRequestURLImpl {


    private final Map<String, List<String>> headers;
    private final InputStream content;

    public WebScriptRequestStub(Runtime runtime, String scriptUrl, Match serviceMatch) {
        this(runtime, scriptUrl, serviceMatch, defaultHeaders(), defaultRequestContent());
    }

    public WebScriptRequestStub(Runtime runtime, String scriptUrl, Match serviceMatch,
            Map<String, List<String>> headers, InputStream inputStream) {
        super(runtime, scriptUrl, serviceMatch);

        this.headers = headers;
        this.content = inputStream;
    }

    private static Map<String, List<String>> defaultHeaders() {
        return new HashMap<>();
    }

    private static InputStream defaultRequestContent() {
        return new ByteArrayInputStream(new byte[0]);
    }

    @Override
    public String getServerPath() {
        return "http://localhost:8080";
    }

    @Override
    public String[] getHeaderNames() {
        return this.headers.keySet().toArray(new String[0]);
    }

    @Override
    public String getHeader(String name) {
        return this.headers.getOrDefault(name, Collections.emptyList())
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public String[] getHeaderValues(String name) {
        return this.headers.getOrDefault(name, Collections.emptyList())
                .toArray(new String[0]);
    }

    @Override
    public Content getContent() {
        return new InputStreamContent(this.content, getContentType(), null);
    }

    @Override
    public String getAgent() {
        throw new UnsupportedOperationException();
    }
}
