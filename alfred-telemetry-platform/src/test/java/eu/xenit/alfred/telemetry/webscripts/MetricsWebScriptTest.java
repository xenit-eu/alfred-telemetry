package eu.xenit.alfred.telemetry.webscripts;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasEntry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import eu.xenit.alfred.telemetry.service.MeterRegistryService;
import java.util.Map;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

@ExtendWith(MockitoExtension.class)
class MetricsWebScriptTest {

    @Mock
    private MeterRegistryService service;

    @Test
    void testView() {
        when(service.getMeterNames()).thenReturn(new TreeSet<String>() {{
            add("test.another.meter");
            add("test.buffer.count");
        }});
        final MetricsWebScript webScript = new MetricsWebScript(service);

        final Map<String, Object> model =
                webScript.executeImpl(mock(WebScriptRequest.class), mock(Status.class), mock(Cache.class));

        assertThat(model, hasEntry(is("names"), any(Object.class)));
        @SuppressWarnings("unchecked")
        Iterable<String> names = (Iterable<String>) model.get("names");
        assertThat(names, contains("test.another.meter", "test.buffer.count"));
    }


}