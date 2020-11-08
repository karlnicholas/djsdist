package com.github.karlnicholas.djsdist.handler;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.github.karlnicholas.djsdist.distributed.ServiceClients;
import com.github.karlnicholas.djsdist.distributed.Grpcservices.WorkItemMessage;
import com.github.karlnicholas.djsdist.model.TransactionType;
import com.github.karlnicholas.djsdist.repository.TransactionOpenRepository;
import com.github.karlnicholas.djsdist.repository.TransactionSubmittedRepository;
import com.google.protobuf.ByteString;

@Component
public class TransactionHandler {
	private static final Logger logger = LoggerFactory.getLogger(TransactionHandler.class);
	private final ServiceClients serviceClients;
	private final TransactionSubmittedRepository transactionSubmittedRepository;
	private final TransactionOpenRepository transactionOpenRepository;
	public TransactionHandler(
			TransactionSubmittedRepository transactionSubmittedRepository, 
			TransactionOpenRepository transactionOpenRepository, 
			ServiceClients serviceClients
	) {
		this.transactionSubmittedRepository = transactionSubmittedRepository;
		this.transactionOpenRepository = transactionOpenRepository;
		this.serviceClients = serviceClients;
	}
	@Async
	public void handleEndOfDay(LocalDate priorBusinessDate) {
		transactionSubmittedRepository.findByBusinessDate(priorBusinessDate)
		.stream().forEach(transactionSubmitted->{
		    System.out.println("Execute method asynchronously. " 
		  	      + Thread.currentThread().getName()
		  	      + transactionSubmitted
		  	      );
		    handleTransaction(transactionSubmitted.getId(), priorBusinessDate);
			logger.info("handleBillingCycle");

		});

		transactionOpenRepository.fetchBillingCyclesForDate(priorBusinessDate).stream().forEach(billingCyclePosting->{
			Map<String, ByteString> params = new HashMap<>();
			Map<String, ByteString> results = new HashMap<>();
			params.put("billingdate", ByteString.copyFromUtf8(priorBusinessDate.toString()));
			params.put("subject", ByteString.copyFromUtf8(billingCyclePosting.getAccountId().toString()));
			
			WorkItemMessage wim = serviceClients.accountDueDate(WorkItemMessage.newBuilder().putAllParams(params).putAllResults(results).build());
			params.putAll(wim.getParamsMap());
			results.putAll(wim.getResultsMap());
			serviceClients.accountInterest(wim.toBuilder().putAllParams(params).putAllResults(results).build());
			params.putAll(wim.getParamsMap());
			results.putAll(wim.getResultsMap());
			serviceClients.accountBillingCycle(wim.toBuilder().putAllParams(params).putAllResults(results).build());
			params.putAll(wim.getParamsMap());
			results.putAll(wim.getResultsMap());
			serviceClients.accountClosing(wim.toBuilder().putAllParams(params).putAllResults(results).build());
			params.putAll(wim.getParamsMap());
			results.putAll(wim.getResultsMap());
		});

	}
	
	@Async
	public void asynchHandleTransaction(Long id, LocalDate businessDate) {
		    handleTransaction(id, businessDate);
	}

	private void handleTransaction(Long id, LocalDate priorBusinessDate) {
		logger.info("handleTransaction");

		Map<String, ByteString> params = new HashMap<>();
		Map<String, ByteString> results = new HashMap<>();
		params.put("subject", ByteString.copyFromUtf8(id.toString()));
		params.put("businessDate", ByteString.copyFromUtf8(priorBusinessDate.toString()));

		WorkItemMessage wim = serviceClients.validateAndProcessTransaction(WorkItemMessage.newBuilder().putAllParams(params).putAllResults(results).build());
		results.putAll(wim.getResultsMap());
		params.putAll(wim.getParamsMap());

		Boolean validated = Boolean.valueOf( results.get("validated").toStringUtf8());
		if ( validated.booleanValue() ) {
			if ( TransactionType.valueOf(results.get("transactionType").toStringUtf8()) == TransactionType.LOAN_FUNDING ) {
				wim =  serviceClients.accountFunded(wim.toBuilder().putAllParams(params).putAllResults(results).build());
				params.putAll(wim.getParamsMap());
				results.putAll(wim.getResultsMap());
				params.put("subject", ByteString.copyFromUtf8("1"));
				serviceClients.initialBillingCycle(wim.toBuilder().putAllParams(params).putAllResults(results).build());
			};
		}

	}
}
