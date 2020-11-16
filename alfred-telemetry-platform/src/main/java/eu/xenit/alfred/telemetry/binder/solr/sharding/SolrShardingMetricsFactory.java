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

public class SolrShardingMetricsFactory {

    private Scheduler scheduler;
    private JobDetail jobDetail;
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
        this.jobDetail = jobDetail;
        this.trigger = trigger;
    }

    public void destroy() throws SchedulerException {
        scheduler.unscheduleJob(trigger.getKey());
    }

}
