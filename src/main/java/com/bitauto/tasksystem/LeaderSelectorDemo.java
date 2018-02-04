package com.bitauto.tasksystem;

import com.sun.org.apache.bcel.internal.generic.NEW;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ExistsBuilder;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.recipes.leader.Participant;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;

import java.util.ArrayList;
import java.util.List;

public class LeaderSelectorDemo {
    private static final String PATH = "/demo/leader";

    public static void main(String[] args) {

        List<CuratorSelectorClient> selectors = new ArrayList<CuratorSelectorClient>();
        List<CuratorFramework> clients = new ArrayList<CuratorFramework>();
        try {
            for (int i = 0; i < 10; i++) {
                CuratorFramework client = getClient();
                clients.add(client);
                final String name = "client#" + i;
                CuratorSelectorClient selectorClient=new CuratorSelectorClient(client,PATH,name);
                selectorClient.start();
                selectors.add(selectorClient);
            }
            //keep threading live
            Thread.sleep(Integer.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
        } finally
        {
            System.out.println("close client");
            for (CuratorFramework client : clients) {
                CloseableUtils.closeQuietly(client);
            }

            for (CuratorSelectorClient selector : selectors) {
                CloseableUtils.closeQuietly(selector);
            }

        }
    }

    private static CuratorFramework getClient() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString("127.0.0.1:2181")
                .retryPolicy(retryPolicy)
                .sessionTimeoutMs(6000)
                .connectionTimeoutMs(3000)
                .namespace("demo")
                .build();
        client.start();
        return client;
    }
}
