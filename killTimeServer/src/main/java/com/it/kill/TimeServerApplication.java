package com.it.kill;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class TimeServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TimeServerApplication.class,args);
    }
}
