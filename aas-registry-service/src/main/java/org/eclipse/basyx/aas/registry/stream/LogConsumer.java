package org.eclipse.basyx.aas.registry.stream;

import java.io.IOException;


import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import lombok.extern.java.Log;

@Service
@Log 
public class LogConsumer {


    @KafkaListener(topics = "aas-registry", groupId = "log")
    public void consume(String message) throws IOException {
        log.info(String.format("#### -> Consumed message -> %s", message));
        
    }
}
