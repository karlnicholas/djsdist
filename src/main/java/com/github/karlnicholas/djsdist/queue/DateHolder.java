package com.github.karlnicholas.djsdist.queue;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;

import javax.jms.Topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class DateHolder {
	private static final Logger LOGGER = LoggerFactory.getLogger(DateHolder.class);

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
		LOGGER.info("updatedate.topic: " + localDate);
		currentDate.set(localDate);
		jmsTopicTemplate.send(dateUpdatedTopic, MessageBuilder.withPayload(currentDate.get()).build());
	}

	@JmsListener(destination = "getdate.queue", containerFactory = "jmsQueueListener")
	public LocalDate getDate(LocalDate localDate) {
		LOGGER.info("getdate.queue: " + currentDate.get());
		
		return currentDate.get();
	}
}
