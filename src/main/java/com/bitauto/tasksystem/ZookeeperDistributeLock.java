package com.bitauto.tasksystem;

import org.apache.curator.RetryPolicy;
import org.apache.curator.RetrySleeper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.framework.recipes.locks.InterProcessMultiLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.monitor.MonitorMBean;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

public class ZookeeperDistributeLock {

    private static final String LOCK_PATH = "/zkfile/lock";
    private static final String ZOOKEEPER_IP_PORT = "localhost:2181";
    private static Logger logger = LoggerFactory.getLogger(ZookeeperDistributeLock.class);

    /*
    分布式锁弊端：如果因进程终止等异常情况锁不释放，那么其它进程无法获取到锁。
     */
    public void lock() {
        /*
        创建一个zookeeper集群链接
         */
        String zookeeperConnectionString = ZOOKEEPER_IP_PORT;
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        /* 实例化替代方式
        RetryPolicy retryPolicy=new RetryPolicy() {
            public boolean allowRetry(int i, long l, RetrySleeper retrySleeper) {
                return false;
            }
        };
        */
        CuratorFramework client = CuratorFrameworkFactory.newClient(zookeeperConnectionString, retryPolicy);
        /*
        客户端必须启动，不用的时候关闭
         */
        client.start();
        /*
        直接调用Zookeeper
        一旦你有一个CuratorFramework实例，你可以直接用zookeeper提供的对象调用。
        Curator管理zookeeper的好处就是如果链接有问题会重试
        client.create().forPath("/my/path",mydata);
         */
        /*
        Distributed Lock 分布式锁
         */
        String lockPath = LOCK_PATH;
        String processName = getProcessName();
        logger.debug("调试信息如下：进程" + processName + "进入");
        logger.info("详细日志如下：进程" + processName + "进入");
        logger.error("错误日志如下：进程" + processName + "进入");
        InterProcessMutex lock = new InterProcessMutex(client, lockPath);
        long maxWait = 200;
        TimeUnit waitUnit = TimeUnit.MILLISECONDS;
        try {
            int pid = getProcessID();
            System.out.println("进程" + pid + "准备竞争锁");
            while (lock.acquire(maxWait, waitUnit)) {
                try {
                    //do some worke 查询任务并创建任务
                    System.out.println("进程" + pid + "----get lock--------");
                    Thread.sleep(10000);
                    Thread.sleep(20000);
                    System.out.println("end --------");
                } finally {
                    lock.release();//释放锁
                    System.out.println("进程" + pid + "----lock release -----");
                }
            }
        } catch (Exception ex) {
            System.out.println("-----exception--------");
            System.out.println(ex.getMessage());
            System.out.println("details:" + ex.getStackTrace());
            logger.error(ex.getMessage() + ex.getStackTrace());
        }
    }

    /**
     * 选举
     */
    public void leaderElection() {

        String name = getProcessName();
        System.out.println(name + "come in leaderElection");
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.newClient(ZOOKEEPER_IP_PORT, retryPolicy);
        LeaderSelectorListener listener = new LeaderSelectorListenerAdapter() {
            public void takeLeadership(CuratorFramework client) throws Exception {
                // this callback will get called when you are the leader
                // do whatever leader work you need to and only exit
                // this method when you want to relinquish leadership
            }
        };

        LeaderSelector selector = new LeaderSelector(client, LOCK_PATH, listener);
        selector.autoRequeue();  // not required, but this is behavior that you will probably expect
        selector.start();
        System.out.println(name + "end in leaderElection");
    }

    public void setListenter() {
        try {

            System.out.println("listener path:" + LOCK_PATH +"  zookeeper:"+ZOOKEEPER_IP_PORT);
            RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
            CuratorFramework client = CuratorFrameworkFactory.newClient(ZOOKEEPER_IP_PORT, retryPolicy);
            ExecutorService pool = Executors.newCachedThreadPool();
            PathChildrenCache childrenCache = new PathChildrenCache(client, LOCK_PATH, true);
            PathChildrenCacheListener childrenCacheListener = new PathChildrenCacheListener() {

                public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                    System.out.println("开始进行事件分析:-----");
                    ChildData data = event.getData();
                    switch (event.getType()) {
                        case CHILD_ADDED:
                            System.out.println("CHILD_ADDED : " + data.getPath() + "  数据:" + data.getData());
                            break;
                        case CHILD_REMOVED:
                            System.out.println("CHILD_REMOVED : " + data.getPath() + "  数据:" + data.getData());
                            break;
                        case CHILD_UPDATED:
                            System.out.println("CHILD_UPDATED : " + data.getPath() + "  数据:" + data.getData());
                            break;
                        default:
                            break;
                    }
                }
            };
            childrenCache.getListenable().addListener(childrenCacheListener);
            System.out.println("Register zk watcher successfully!");
            childrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);

        } catch (Exception ex) {
            System.out.println(ex.getMessage() + ex.getStackTrace());
        }
    }

    /**
     * 获取进程ID
     *
     * @return 进程id
     */
    public static final int getProcessID() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        //进程@账号   74688@lipp-9
        System.out.println(runtimeMXBean.getName());
        return Integer.valueOf(runtimeMXBean.getName().split("@")[0])
                .intValue();
    }

    public static final String getProcessName() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        return runtimeMXBean.getName();
    }
}
