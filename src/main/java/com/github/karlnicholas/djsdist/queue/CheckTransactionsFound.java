package com.github.karlnicholas.djsdist.queue;

import javax.jms.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Service;

import com.github.karlnicholas.djsdist.message.TransactionsFoundMessage;

@Service
public class CheckTransactionsFound {
	private static final Logger LOGGER = LoggerFactory.getLogger(CheckTransactionsFound.class);

	@Qualifier("jmsQueueTemplate")
	private JmsMessagingTemplate jmsQueueTemplate;
	@Autowired
	@Qualifier("transactionsfound.queue")
	private Queue transactionsFoundQueue;

	@JmsListener(destination = "transactionsfound.queue", containerFactory = "jmsQueueListener")
	public TransactionsFoundMessage getDate(TransactionsFoundMessage transactionsFoundMessage) {
		LOGGER.info("transactionsfound.queue: " + transactionsFoundMessage);
		transactionsFoundMessage.setTransactionsFound(Boolean.FALSE);
		return transactionsFoundMessage;
	}
}
