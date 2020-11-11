package com.github.karlnicholas.djsdist.queue;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;

import javax.jms.Topic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DateHolder {

	private AtomicReference<LocalDate> currentDate;
	@Autowired
	@Qualifier("jmsTopicTemplate")
	private JmsMessagingTemplate jmsTopicTemplate;
	@Autowired
	@Qualifier("dateupdated.topic")
	private Topic dateUpdatedTopic;
	public DateHolder(	) {
		currentDate = new AtomicReference<>(LocalDate.now());
	}
/*	
	@JmsListener(destination = "getdate.queue", containerFactory = "jmsQueueListener")
	public CurrentDateMessage getDate(String message) {
		LOGGER.info("getdate.queue");
		return CurrentDateMessage.builder().date(currentDate.get()).build();
	}
*/	
	@JmsListener(destination = "updatedate.topic", containerFactory = "jmsTopicListener")
	public void updateDate(LocalDate localDate) {
		log.debug("updatedate.topic: {}", localDate);
		currentDate.set(localDate);
		jmsTopicTemplate.send(dateUpdatedTopic, MessageBuilder.withPayload(currentDate.get()).build());
	}

	@JmsListener(destination = "getdate.queue", containerFactory = "jmsQueueListener")
	public LocalDate getDate(LocalDate localDate) {
		log.debug("getdate.queue: {}", currentDate.get());
		
		return currentDate.get();
	}
}
