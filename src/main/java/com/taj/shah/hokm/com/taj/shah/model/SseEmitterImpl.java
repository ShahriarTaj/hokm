package com.taj.shah.hokm.com.taj.shah.model;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SseEmitterImpl {

    private ExecutorService nonBlockingService = Executors
            .newCachedThreadPool();

    public String name;
    public BlockingQueue<String> blockingQueue;
    public SseEmitterImpl(String name, BlockingQueue<String> incomingQueue){
        this.blockingQueue = incomingQueue;
        this.name = name;
    }

    public SseEmitter handleSse() {
        SseEmitter emitter = new SseEmitter();
        nonBlockingService.execute(() -> {
            try {
                String msg = blockingQueue.poll(30, TimeUnit.SECONDS);
                    if ( msg != null){
                        //System.out.println("Message: " + msg);
                        emitter.send(msg);
                    }
//                msg = (msg == null ? "What?" : msg);
//                System.out.println("Message: " + msg);
//                emitter.send(msg);
                emitter.complete();
            } catch (Exception ex) {
                emitter.completeWithError(ex);
                //ex.printStackTrace();
            }
        });
        return emitter;
    }

}
