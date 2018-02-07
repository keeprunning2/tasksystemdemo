package com.bitauto.tasksystem;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import sun.util.calendar.BaseCalendar;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 我的系统任务 10s检查一下系统
 */
public class MyJob implements org.quartz.Job {

    public MyJob() {
    }

    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobKey key = jobExecutionContext.getJobDetail().getKey();
        JobDataMap dataMap = jobExecutionContext.getMergedJobDataMap();
        System.out.println("任务正在执行" + key.getName() + "，当前时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        //运行jar包程序“textencode.jar”，需要运行那个改成那个jar包名称即可
        try {
            Runtime.getRuntime().exec("java -jar " + dataMap.get("taskname"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
