package com.brave.controller;

import com.brave.config.ZookeeperConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by junzhang on 03/03/2018.
 */
@RestController
public class DemoController {

    @Autowired
    ZookeeperConfig zookeeperConfig;

    @GetMapping("/demo")
    public void demo() {
        zookeeperConfig.modify();
    }
}
