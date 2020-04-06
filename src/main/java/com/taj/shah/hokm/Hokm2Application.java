package com.taj.shah.hokm;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;


@SpringBootApplication
public class Hokm2Application {

    private  BlockingQueue<String> sseQueue = new LinkedBlockingQueue();
    @Bean
    public BlockingQueue<String> sseInbound()
    {
        return sseQueue;
    }
    public static void main(String[] args) {
        SpringApplication.run(Hokm2Application.class, args);
    }

}
