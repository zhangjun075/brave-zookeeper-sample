package com.brave.config;

import com.brave.util.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author junzhang
 * @date 02/03/2018
 */
@Slf4j
public class ZookeeperConfig implements Watcher {

    @Value("${zk.connect}")
    private String connect;

    @Value("${server.port}")
    private String port;

    private ZooKeeper zk = null;
    // 根节点
    private static String ROOT = "/ip";

    private int sessionTimeout = 30000;

    private static String localIp = IpUtil.getLocalIP();

    private AtomicInteger atomicInteger = new AtomicInteger(0);

    private static CountDownLatch connectedSemaphore = new CountDownLatch(1);

    /**
     * 初始化,创建根节点，同时创建注册自身节点。
     */
    @PostConstruct
    public void init() {
        try {
            // 连接zookeeper
            zk = new ZooKeeper(connect, sessionTimeout, this);
//            connectedSemaphore.await();
            Stat stat = zk.exists(ROOT, false);
            if (stat == null) {
                // 如果根节点不存在，则创建根节点
                zk.create(ROOT, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
            Stat stat1 = zk.exists(ROOT+"/" + IpUtil.getLocalIP() +"-" + port,this);
            if(stat1 == null) {
                zk.create(ROOT+"/" + IpUtil.getLocalIP() +"-" + port,null,ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }



    @Override public void process(WatchedEvent event) {
        if (Event.EventType.NodeDataChanged == event.getType() && event.getPath().equals(ROOT + "/" + IpUtil.getLocalIP() + "-" + port)) {
            log.info("success change znode: " + event.getPath());
            try {
                zk.getData(ROOT + "/" + IpUtil.getLocalIP() + "-" + port,true,null);
            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
//        log.info("......into .....");
//        log.info("event.getType={}",event.getType().toString());
//        if (Event.KeeperState.SyncConnected == event.getState()) {
//            log.info("connected....");
//            log.info("event.getType={}",event.getType().toString());
//            if (Event.EventType.None == event.getType() && null == event.getPath()) {
//                connectedSemaphore.countDown();
//            } else if (Event.EventType.NodeCreated == event.getType()) {
//                log.info("success create znode");
//
//            } else if (Event.EventType.NodeDataChanged == event.getType()) {
//                log.info("success change znode: " + event.getPath());
//                if(event.getPath().equals(ROOT+ "/" + IpUtil.getLocalIP() + "-" + port)){
//                    log.info("it's me.........");
//                }
//
//            } else if (Event.EventType.NodeDeleted == event.getType()) {
//                log.info("success delete znode");
//
//            } else if (Event.EventType.NodeChildrenChanged == event.getType()) {
//                log.info("NodeChildrenChanged");
//            }
//
//        }
    }


    public void modify(String value) {
        try {
            List<String> nodes = zk.getChildren(ROOT,true);
            nodes.stream().forEach(node -> {
                log.info(node);
                Stat stat = null;
                try {
                    stat = zk.exists(ROOT+"/" + node,this);
                    if(stat != null){
                        zk.setData(ROOT+"/" + node,value.getBytes(),-1);
                    }
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void test() {
        log.info("watcher trigger......");
    }
}
