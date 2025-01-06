package eu.xenit.alfred.telemetry.binder.solr.sharding;

import org.alfresco.repo.index.shard.Floc;
import org.alfresco.repo.index.shard.Shard;
import org.alfresco.repo.index.shard.ShardRegistry;
import org.alfresco.repo.index.shard.ShardState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolrShardingMetricsContainerTest {

    @Mock
    private ShardRegistry shardRegistry;

    private SolrShardingMetricsContainer container;

    @BeforeEach
    void setup() {
        container = new SolrShardingMetricsContainer(shardRegistry);
    }

    @Test
    void refresh() {
        Floc floc = new Floc();
        Map<Floc, Map<Shard, Set<ShardState>>> flocs = new HashMap<>();
        flocs.put(floc, new HashMap<>());
        when(shardRegistry.getFlocs()).thenReturn(flocs);

        container.refresh();
        assertThat(container.getFlocs(), contains(floc));
    }

}