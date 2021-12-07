package org.eclipse.basyx.aas.registry.stream;

import java.io.IOException;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class LogConsumer {

    @KafkaListener(topics = "aas-registry", groupId = "log")
    public void consume(String message) throws IOException {
    	// just to log written events
    	// could be removed to optimize performance
        log.info("Event send -> " + message);

    }
}
