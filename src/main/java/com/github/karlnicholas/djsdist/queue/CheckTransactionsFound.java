package com.github.karlnicholas.djsdist.queue;

import javax.jms.Queue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Service;

import com.github.karlnicholas.djsdist.message.TransactionsFoundMessage;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CheckTransactionsFound {

	@Qualifier("jmsQueueTemplate")
	private JmsMessagingTemplate jmsQueueTemplate;
	@Autowired
	@Qualifier("transactionsfound.queue")
	private Queue transactionsFoundQueue;

	@JmsListener(destination = "transactionsfound.queue", containerFactory = "jmsQueueListener")
	public TransactionsFoundMessage getDate(TransactionsFoundMessage transactionsFoundMessage) {
		log.debug("transactionsfound.queue: {}", transactionsFoundMessage);
		transactionsFoundMessage.setTransactionsFound(Boolean.FALSE);
		return transactionsFoundMessage;
	}
}
