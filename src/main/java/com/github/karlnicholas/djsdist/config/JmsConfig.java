package com.github.karlnicholas.djsdist.config;

import javax.jms.Queue;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.jms.core.JmsTemplate;

@Configuration
@EnableJms
public class JmsConfig {
	
//	@Value("${activemq.broker-url}")
//	private String brokerUrl;
/*
	@Bean
	public ActiveMQConnectionFactory activeMQConnectionFactory() {
		ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
		activeMQConnectionFactory.setBrokerURL("vm://localhost?broker.persistent=false");
		activeMQConnectionFactory.setTrustAllPackages(true);
		return activeMQConnectionFactory;
	}
*/
	@Bean
	public ActiveMQConnectionFactory activeMQConnectionFactory() {
		ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
//		activeMQConnectionFactory.setBrokerURL("tcp://localhost:61616");
		activeMQConnectionFactory.setBrokerURL("vm://localhost?broker.persistent=false");
		activeMQConnectionFactory.setTrustAllPackages(true);
		return activeMQConnectionFactory;
	}
	@Bean(name = "jmsTopicListener")
	public DefaultJmsListenerContainerFactory jmsTopicListenerContainerFactory() {
		DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
		factory.setConnectionFactory(activeMQConnectionFactory());
		factory.setConcurrency("3");
		factory.setPubSubDomain(true);

		return factory;
	}

	@Bean({"jmsQueueListener"})
	public DefaultJmsListenerContainerFactory jmsQueueListenerContainerFactory() {
		DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
		factory.setConnectionFactory(activeMQConnectionFactory());
	    factory.setConcurrency("3");
		return factory;
	}

	@Bean
	public CachingConnectionFactory cachingConnectionFactory() {
		return new CachingConnectionFactory(activeMQConnectionFactory());
	}

	@Bean(name="jmsTopicTemplate")
	public JmsMessagingTemplate jmsTopicTemplate() {
		JmsTemplate jmsTemplate = new JmsTemplate(cachingConnectionFactory());
		jmsTemplate.setPubSubDomain(true);
		return new JmsMessagingTemplate(jmsTemplate); 
	}

	@Bean(name="jmsQueueTemplate")
	public JmsMessagingTemplate jmsQueueTemplate() {
		JmsTemplate jmsTemplate = new JmsTemplate(cachingConnectionFactory());
		return new JmsMessagingTemplate(jmsTemplate); 
	}
	@Bean("getdate.queue")
	public Queue getDateQueue() {
		return new ActiveMQQueue("getdate.queue");
	}
	@Bean("transactionsfound.queue")
	public Queue getTransactionsFoundQueue() {
		return new ActiveMQQueue("transactionsfound.queue");
	}
	@Bean("updatedate.topic")
	public Topic updateDateTopic() {
		return new ActiveMQTopic("updatedate.topic");
	}
	@Bean("dateupdated.topic")
	public Topic dateUpdatedTopic() {
		return new ActiveMQTopic("dateupdated.topic");
	}
}