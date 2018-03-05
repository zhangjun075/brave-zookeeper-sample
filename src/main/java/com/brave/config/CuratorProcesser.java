package com.brave.config;

import com.brave.util.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by junzhang on 05/03/2018.
 */
@Slf4j
@Component
public class CuratorProcesser {

    @Value("${zk.connect}")
    private String connect;

    @Value("${server.port}")
    private String port;

    private static String ROOT = "/ip";

    private CuratorFramework client = null;

    private static int BASE_SLEEP_TIME_MS = 1000;
    private static int MAX_RETRIES = 3;

    private ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

//    CuratorWatcher curatorWatcher = watchedEvent -> {
//        log.info("watchedEvent.getType()={}",watchedEvent.getType());
//        log.info("watchedEvent.getPath()={}",watchedEvent.getPath());
//    };

    @PostConstruct
    public void init() throws Exception {

        String subNode = ROOT + "/" + IpUtil.getLocalIP() + "-" + port;

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME_MS, MAX_RETRIES);
        client = CuratorFrameworkFactory.newClient(connect,retryPolicy);
        client.start();
        Stat stat = client.checkExists().forPath(ROOT);
        if(stat == null) {
            client.create().withMode(CreateMode.PERSISTENT).forPath(ROOT);
        }

        Stat stat1 = client.checkExists().forPath(subNode);
        if(stat1 == null) {
            client.create().withMode(CreateMode.EPHEMERAL).forPath(subNode,null);
        }

        /**
         * 监听数据节点的变化情况
         */
        final NodeCache nodeCache = new NodeCache(client, subNode, false);
        nodeCache.start(true);
        nodeCache.getListenable().addListener(() -> log.info("Node data is changed, new data: " +
            new String(nodeCache.getCurrentData().getData())),
            pool
        );

//        client.checkExists().usingWatcher(curatorWatcher).forPath(subNode);
    }


    public void modify(String value) {
        try {
            List<String> nodes = client.getChildren().forPath(ROOT);
            nodes.stream().forEach(node -> {
                String subNode = ROOT + "/" + node ;
                try {
                    client.setData().forPath(subNode,value.getBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
