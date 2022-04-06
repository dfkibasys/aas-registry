package org.eclipse.basyx.aas.registry.service.tests.integration;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.basyx.aas.registry.events.RegistryEvent;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class BaseEventListener {

	private LinkedBlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

	@Autowired
	private ObjectMapper mapper;

	public boolean offer(String message) {
		return messageQueue.offer(message);
	}

	public void reset() {
		try {
			while (messageQueue.poll(1, TimeUnit.SECONDS) != null)
				;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new EventListenerException(e);
		}
	}

	public void assertNoAdditionalMessage() {
		try {
			String message = messageQueue.poll(1, TimeUnit.SECONDS);
			if (message != null) {
				throw new EventListenerException("Got additional message: " + message);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new EventListenerException(e);
		}
	}

	public RegistryEvent poll() {
		try {
			String message = messageQueue.poll(5, TimeUnit.SECONDS);
			if (message == null) {
				throw new EventListenerException("timeout");
			}
			return mapper.readValue(message, RegistryEvent.class);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new EventListenerException(e);
		} catch (JsonProcessingException e) {
			throw new EventListenerException(e);
		}
	}
	
	public static final class EventListenerException extends RuntimeException {

		private static final long serialVersionUID = 1L;
		
		public EventListenerException(Throwable e) {
			super(e);
		}

		public EventListenerException(String msg) {
			super(msg);
		}
	}
}
