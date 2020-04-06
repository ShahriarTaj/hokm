package com.taj.shah.hokm;

import com.taj.shah.hokm.com.taj.shah.model.SseEmitterImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Controller
public class SseEmitterController {

    @Autowired
    BlockingQueue<String> incomingQueue;

    Map<String, SseEmitterImpl> emitters = new LinkedHashMap<>();

    private ExecutorService nonBlockingService = Executors
            .newCachedThreadPool();

    @PostConstruct
    public void init() {
        Runnable task = () -> {
            while (true) {
                try {
                    String msg = incomingQueue.poll(30, TimeUnit.SECONDS);
                    if (msg != null) {
                        msg = (msg == null ? "What?" : msg);
                        for (SseEmitterImpl i : emitters.values()) {
                            //System.out.println("Sending Message: " + msg + " to " + i);
                            i.blockingQueue.add(msg);
                        }
                    }
                } catch (Exception ex) {
                    System.err.println(ex.getMessage());
                }
            }
        };

        Thread thread = new Thread(task);
        thread.start();
        System.err.println("Finished init " + this.getClass().getName());
    }


    @GetMapping("/sse/{playerName}/")
    public SseEmitter handleSse(@PathVariable("playerName") String playerNameNotEncoded) throws UnsupportedEncodingException {
        String playerName = URLEncoder.encode(playerNameNotEncoded, "UTF-8");
        SseEmitter emitter = null;
        synchronized (this) {
            if (!emitters.containsKey(playerName)) {
                emitters.put(playerName, new SseEmitterImpl(playerName, new LinkedBlockingQueue()));
            }
            emitter = emitters.get(playerName).handleSse();
        }
        return emitter;
    }


}