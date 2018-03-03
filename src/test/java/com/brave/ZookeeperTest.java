package com.brave;

import com.brave.config.ZookeeperConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by junzhang on 03/03/2018.
 */

@SpringBootTest
@RunWith(SpringRunner.class)
public class ZookeeperTest {

    @Autowired
    ZookeeperConfig zookeeperConfig;

    @Test
    public void testZookeeper(){
        zookeeperConfig.init();
    }
}
