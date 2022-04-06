package org.eclipse.basyx.aas.registry.service.events;

import org.eclipse.basyx.aas.registry.events.RegistryEvent;
import org.eclipse.basyx.aas.registry.events.RegistryEventSink;
import org.slf4j.Marker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
@ConditionalOnProperty(prefix = "events", name = "sink", havingValue = "log")
public class RegistryEventLogSink implements RegistryEventSink {

	@Autowired
	private MappingJackson2HttpMessageConverter converter;

	@Override
	public void consumeEvent(RegistryEvent evt) {
		try {
			ObjectMapper objectMapper = converter.getObjectMapper();
			String msg = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(evt);
			log.info("Event sent -> " + msg);
		} catch (JsonProcessingException e) {
			log.error(Marker.ANY_MARKER, "Failed to proecess json ", e);
		}
	}

}