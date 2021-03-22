package eu.xenit.alfred.telemetry.solr.monitoring.binder;

import eu.xenit.alfred.telemetry.solr.util.Util;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map.Entry;
import java.util.Set;
import org.alfresco.solr.AlfrescoCoreAdminHandler;
import org.alfresco.solr.tracker.TrackerRegistry;
import org.apache.solr.common.util.NamedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolrFTSMetrics implements MeterBinder {

    private AlfrescoCoreAdminHandler coreAdminHandler;
    private MeterRegistry registry;

    private static final Logger logger = LoggerFactory.getLogger(SolrFTSMetrics.class);

    public SolrFTSMetrics(AlfrescoCoreAdminHandler coreAdminHandler) {
        this.coreAdminHandler = coreAdminHandler;
    }

    private void registerFTSMetrics() {
        logger.info("Registering FTS metrics");
        TrackerRegistry trackerRegistry = coreAdminHandler.getTrackerRegistry();

        while (trackerRegistry.getCoreNames().size() == 0) {
            logger.error("Solr did not start tracking yet, waiting 10sec");
            try {
                Thread.currentThread().sleep(10_000);
                trackerRegistry = coreAdminHandler.getTrackerRegistry();
            } catch (InterruptedException e) {
                logger.error("Fail to wait 10 sec", e);
            }
        }

        Set<String> coreNames = coreAdminHandler.getTrackerRegistry().getCoreNames();
        for (String coreName : coreNames) {
            Object server = coreAdminHandler.getInformationServers().get(coreName);
            NamedList<Object> report = new NamedList();
            Method method = null;
            try {
                Class<?> solrInformationServerClass = Class.forName("org.alfresco.solr.SolrInformationServer");
                Class<?>[] partypes = new Class[]{NamedList.class};
                if(Util.isVersionLowerThan2()) {
                    method = solrInformationServerClass.getMethod("addFTSStatusCounts", partypes);
                } else {
                    method = solrInformationServerClass.getMethod("addContentOutdatedAndUpdatedCounts", partypes);
                }
            } catch (ClassNotFoundException e) {
                logger.error("No class found for SolrInformationServer ", e);
            } catch (NoSuchMethodException e) {
                logger.error("No appropriate method to call on SolrInformationServer object ", e);
            }
            Object[] arglist = new Object[1];
            arglist[0] = report;
            try {
                method.invoke(server, arglist);
            } catch (IllegalAccessException e) {
                logger.error("Not allowed to call the FTS method on SolrInformationServer object ", e);
            } catch (InvocationTargetException e) {
                logger.error("Not allowed to call the FTS method on SolrInformationServer object ", e);
            }

            // Keys in ASS >= 2.0.0
            Tags tags = Tags.of("core", coreName, "state", "InSync");
            Gauge.builder("alfresco.fts", server, x -> getValueFromReport(server, "Node count whose content is in sync"))
                    .tags(tags)
                    .register(registry);

            tags = Tags.of("core", coreName, "state", "ToBeUpdated");
            Gauge.builder("alfresco.fts", server, x -> getValueFromReport(server, "Node count whose content needs to be updated"))
                    .tags(tags)
                    .register(registry);

            // Keys in ASS < 2.0.0
            tags = Tags.of("core", coreName, "state", "Clean");
            Gauge.builder("alfresco.fts", server, x -> getValueFromReport(server, "Node count with FTSStatus Clean"))
                    .tags(tags)
                    .register(registry);

            tags = Tags.of("core", coreName, "state", "Dirty");
            Gauge.builder("alfresco.fts", server, x -> getValueFromReport(server, "Node count with FTSStatus Dirty"))
                    .tags(tags)
                    .register(registry);

            tags = Tags.of("core", coreName, "state", "New");
            Gauge.builder("alfresco.fts", server, x -> getValueFromReport(server, "Node count with FTSStatus New"))
                    .tags(tags)
                    .register(registry);

        }
    }

    private long getValueFromReport(Object server, String key) {
        NamedList<Object> report = new NamedList();
        Method method = null;
        try {
            Class<?> solrInformationServerClass = Class.forName("org.alfresco.solr.SolrInformationServer");
            Class<?>[] partypes = new Class[]{NamedList.class};
            if(Util.isVersionLowerThan2()) {
                method = solrInformationServerClass.getMethod("addFTSStatusCounts", partypes);
            } else {
                method = solrInformationServerClass.getMethod("addContentOutdatedAndUpdatedCounts", partypes);
            }
        } catch (ClassNotFoundException e) {
            logger.error("No class found for SolrInformationServer ", e);
        } catch (NoSuchMethodException e) {
            logger.error("No appropriate method to call on SolrInformationServer object ", e);
        }
        Object[] arglist = new Object[1];
        arglist[0] = report;
        try {
            method.invoke(server, arglist);
        } catch (IllegalAccessException e) {
            logger.error("Not allowed to call the FTS method on SolrInformationServer object ", e);
        } catch (InvocationTargetException e) {
            logger.error("Not allowed to call the FTS method on SolrInformationServer object ", e);
        }

        for (Entry fts : report) {
            if (fts.getKey().equals(key)) {
                return Long.parseLong(fts.getValue().toString());
            }
        }
        return -1;
    }


    @Override
    public void bindTo(MeterRegistry registry) {
        this.registry = registry;
        registerFTSMetrics();
    }
}
