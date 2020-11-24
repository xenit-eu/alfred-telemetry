package eu.xenit.alfred.telemetry.binder.solr.sharding;

import io.micrometer.core.instrument.MeterRegistry;
import org.alfresco.repo.index.shard.ShardRegistry;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolrShardingMetricsFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolrShardingMetricsFactory.class);
    private Scheduler scheduler;
    private Trigger trigger;

    public SolrShardingMetricsFactory(ShardRegistry shardRegistry, MeterRegistry registry, Scheduler scheduler,
            String updateCron, Boolean flocIdEnabled) throws SchedulerException {

        this.scheduler = scheduler;
        JobBuilder jobBuilder = JobBuilder.newJob(SolrShardingMetricsScheduledJob.class);
        JobDataMap data = new JobDataMap();
        data.put(SolrShardingMetricsScheduledJob.SOLR_SHARDING_METRICS,
                new SolrShardingMetrics(shardRegistry, registry, flocIdEnabled));
        JobDetail jobDetail = jobBuilder.usingJobData(data).withIdentity("SolrShardingMetricsJob").build();

        Trigger trigger = TriggerBuilder.newTrigger().withSchedule(CronScheduleBuilder.cronSchedule(updateCron))
                .forJob(jobDetail.getKey()).build();

        scheduler.scheduleJob(jobDetail, trigger);
        this.trigger = trigger;
    }

    public void destroy() {
        try {
            scheduler.unscheduleJob(trigger.getKey());
        } catch (SchedulerException e) {
            LOGGER.error("Unable to unschedule job", e);
        }
    }

}
