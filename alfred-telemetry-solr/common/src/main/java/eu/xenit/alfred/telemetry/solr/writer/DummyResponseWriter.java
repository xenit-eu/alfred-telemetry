package eu.xenit.alfred.telemetry.solr.writer;

import java.io.IOException;
import java.io.Writer;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.QueryResponseWriter;
import org.apache.solr.response.SolrQueryResponse;

public class DummyResponseWriter implements QueryResponseWriter {

    @Override
    public void write(Writer writer, SolrQueryRequest request, SolrQueryResponse response) throws IOException {
        writer.write(response.getValues().get("allMetrics").toString());
    }

    @Override
    public String getContentType(SolrQueryRequest request, SolrQueryResponse response) {
        return null;
    }

    @Override
    public void init(NamedList args) {

    }
}
