package eu.xenit.alfred.telemetry.binder.solr.sharding;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class SolrShardingMetricsScheduledJob extends QuartzJobBean {

    public static final String SOLR_SHARDING_METRICS = "solrShardingMetrics";

    @Override
    protected void executeInternal(JobExecutionContext context) {
        JobDataMap jobData = context.getJobDetail().getJobDataMap();

        // Extract the Job executer to use
        Object executerObj = jobData.get(SOLR_SHARDING_METRICS);
        if (executerObj == null || !(executerObj instanceof SolrShardingMetrics)) {
            throw new AlfrescoRuntimeException(
                    "ScheduledJob data must contain valid 'solrShardingMetrics' reference");
        }

        final SolrShardingMetrics solrShardingMetrics = (SolrShardingMetrics) executerObj;

        AuthenticationUtil.runAs(() -> {
            solrShardingMetrics.updateMetrics();
            return null;
        }, AuthenticationUtil.getSystemUserName());
    }
}
