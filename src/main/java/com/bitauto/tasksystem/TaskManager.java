package com.bitauto.tasksystem;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 任务管理，开启系统任务
 */
public class TaskManager {
    private static SchedulerFactory gSchedulerFactory = new StdSchedulerFactory();
    private static String JOB_GROUP_NAME = "EXTJWEB_JOBGROUP_NAME";
    private static String TRIGGER_GROUP_NAME = "EXTJWEB_TRIGGERGROUP_NAME";

    public static void  startDeaultSystemJob(int repeatSecond){
        System.out.println("init default job");
        try {
            String jobName="systemjob";
            Scheduler sched = gSchedulerFactory.getScheduler();
            // 任务名，任务组，任务执行类
            JobDetail jobDetail = JobBuilder.newJob(MyJob.class).withIdentity(jobName,JOB_GROUP_NAME).build();
            //可以传递参数
            jobDetail.getJobDataMap().put("param", "railsboy");
            // 触发器   触发器名,触发器组
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(jobName,TRIGGER_GROUP_NAME)
                    .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(repeatSecond))
                    .forJob(jobName,JOB_GROUP_NAME)
                    .build();
            // 触发器时间设定
            sched.scheduleJob(jobDetail, trigger);
            // 启动
            if (!sched.isShutdown()) {
                sched.start();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage()+e.getStackTrace());
            throw new RuntimeException(e);
        }
    }
    /*
    *添加一个简单任务
    *@param jobName 任务名称
    * @param cls 执行任务类
    * @param count 执行次数
    * @param  hours 间隔小时数
     */
    public static void addSimpleJob(String jobName, Class cls, int count,int hours) {
        try {
            Scheduler sched = gSchedulerFactory.getScheduler();
            // 任务名，任务组，任务执行类
            JobDetail jobDetail = JobBuilder.newJob(cls).withIdentity(jobName,JOB_GROUP_NAME).build();
            //可以传递参数
            jobDetail.getJobDataMap().put("param", "railsboy");
            // 触发器   触发器名,触发器组
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(jobName,TRIGGER_GROUP_NAME)
                    .withSchedule(SimpleScheduleBuilder.repeatHourlyForTotalCount(count,hours))
                    .forJob(jobName,JOB_GROUP_NAME)
                    .build();
            // 触发器时间设定
            sched.scheduleJob(jobDetail, trigger);
            // 启动
            if (!sched.isShutdown()) {
                sched.start();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * 添加一个定时任务，使用默认的任务组名，触发器名，触发器组名
     * @param jobName 任务名
     * @param cls 任务
     * @param time 时间设置
     */
    @SuppressWarnings("rawtypes")
    public static void addJob(String jobName, Class cls, String time) {
        try {
            Scheduler sched = gSchedulerFactory.getScheduler();
            // 任务名，任务组，任务执行类
            JobDetail jobDetail = JobBuilder.newJob(cls).withIdentity(jobName,JOB_GROUP_NAME).build();
            //可以传递参数
            jobDetail.getJobDataMap().put("param", "railsboy");
            // 触发器   触发器名,触发器组
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(jobName,TRIGGER_GROUP_NAME)
                    .withSchedule(CronScheduleBuilder.cronSchedule(time))
                    .forJob(jobName,JOB_GROUP_NAME)
                    .build();
            //  new CronTrigger(new TriggerKey(jobName, TRIGGER_GROUP_NAME) );
            // 触发器时间设定
            sched.scheduleJob(jobDetail, trigger);
            // 启动
            if (!sched.isShutdown()) {
                sched.start();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * 修改一个任务的触发时间(使用默认的任务组名，触发器名，触发器组名)
     * @param jobName
     * @param time
     */
    @SuppressWarnings("rawtypes")
    public static void modifyJobTime(String jobName, String time) {
        try {
            Scheduler sched = gSchedulerFactory.getScheduler();
            CronTrigger trigger = (CronTrigger) sched.getTrigger(new TriggerKey(jobName,TRIGGER_GROUP_NAME));
            if (trigger == null) {
                return;
            }
            String oldTime = trigger.getCronExpression();
            if (!oldTime.equalsIgnoreCase(time)) {
                JobDetail jobDetail = sched.getJobDetail(new JobKey(jobName,JOB_GROUP_NAME));
                Class objJobClass = jobDetail.getJobClass();
                removeJob(jobName);
                addJob(jobName, objJobClass, time);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 移除一个任务(使用默认的任务组名，触发器名，触发器组名)
     * @param jobName
     */
    public static void removeJob(String jobName) {
        try {
            Scheduler sched = gSchedulerFactory.getScheduler();
            sched.pauseTrigger(new TriggerKey(jobName, TRIGGER_GROUP_NAME));// 停止触发器
            sched.unscheduleJob(new TriggerKey(jobName, TRIGGER_GROUP_NAME));// 移除触发器
            sched.deleteJob(new JobKey(jobName, JOB_GROUP_NAME));// 删除任务
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 启动所有定时任务
     */
    public static void startJobs() {
        try {
            Scheduler sched = gSchedulerFactory.getScheduler();
            sched.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 关闭所有定时任务
     */
    public static void shutdownJobs() {
        try {
            Scheduler sched = gSchedulerFactory.getScheduler();
            if (!sched.isShutdown()) {
                sched.shutdown();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
