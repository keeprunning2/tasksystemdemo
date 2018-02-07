package com.bitauto.tasksystem;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.Properties;

public class Application {

    public static void main(String[] args) {
        try {
            //1. read zookeeper config
            LogHelper.logger.debug("1. read zookeeper config");
            Properties props = new Properties();
            props.load(Application.class.getClassLoader().getResourceAsStream("zkconfig.properties"));
            String zk_ip_port = props.getProperty("zk.ip_port");
            String leaderpath = props.getProperty("zk.leaderpath");
            System.out.println(zk_ip_port);
            if (zk_ip_port == null || zk_ip_port.length() < 1) {
                LogHelper.logger.error("zookeeper ip and port is null ");
            }

            //2. start leader selector
            LogHelper.logger.debug("2. start leader selector");
            String name = getLocalIP();
            CuratorSelectorClient selectorClient = new CuratorSelectorClient(zk_ip_port, leaderpath, name, "demo");
            selectorClient.start();
            //3.  listener the zknode and new task timer in takeLeadership  method

            System.out.println("in waiting ...");
            Thread.sleep(Integer.MAX_VALUE);

            //TaskManager.startDeaultSystemJob(2);

        } catch (Exception ex) {
            System.out.println(ex.getMessage() + ex.getStackTrace());
        }
    }


    private static String getLocalIP() {
        InetAddress ia = null;
        try {
            ia = ia.getLocalHost();

            String localname = ia.getHostName();
            String localip = ia.getHostAddress();
            System.out.println("本机名称是：" + localname);
            System.out.println("本机的ip是 ：" + localip);
            return localip;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "127.0.0.1";
        }
    }
}
