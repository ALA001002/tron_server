package com.bigo.tronserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication
@EnableJpaRepositories("com.bigo.tronserver.dao")
@EntityScan(basePackages={"com.bigo.tronserver.entity"})
@EnableScheduling
public class TronServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TronServerApplication.class, args);
    }

}
