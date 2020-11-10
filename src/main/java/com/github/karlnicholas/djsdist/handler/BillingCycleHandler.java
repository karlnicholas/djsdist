package com.github.karlnicholas.djsdist.handler;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.github.karlnicholas.djsdist.distributed.ServiceClients;
import com.github.karlnicholas.djsdist.model.Transaction;
import com.github.karlnicholas.djsdist.distributed.Grpcservices.WorkItemMessage;
import com.github.karlnicholas.djsdist.repository.TransactionClosedRepository;
import com.github.karlnicholas.djsdist.repository.TransactionOpenRepository;
import com.github.karlnicholas.djsdist.service.BusinessDateService;
import com.google.protobuf.ByteString;

@Component
public class BillingCycleHandler {
	private static final Logger logger = LoggerFactory.getLogger(BillingCycleHandler.class);
	private final ServiceClients serviceClients;
	private final BusinessDateService businessDateService;
	private final TransactionOpenRepository transactionOpenRepository;
	private final TransactionClosedRepository transactionClosedRepository;

	public BillingCycleHandler(
		ServiceClients serviceClients, 
		BusinessDateService businessDateService, 
		TransactionOpenRepository transactionOpenRepository, 
		TransactionClosedRepository transactionClosedRepository
	) {
		this.serviceClients = serviceClients;
		this.businessDateService = businessDateService;
		this.transactionOpenRepository = transactionOpenRepository; 
		this.transactionClosedRepository = transactionClosedRepository;
	}
	
	public void fetchLatestBillingCycleForAccount() {
		Long accountId = new Long(1L);
		Transaction latestBillingCycle = transactionOpenRepository.fetchLatestBillingCycleForAccount(accountId);
		if ( latestBillingCycle == null ) {
			latestBillingCycle = transactionClosedRepository.fetchLatestBillingCycleForAccount(accountId);
		};
	}

	public void billingDate() {
/*		
		List<TransactionOpen> billingCycles = transactionOpenRepository.fetchLatestBillingCycles();
		LocalDate billingDate = businessDateService.getBusinessDate();
		List<Long> result = billingCycles.stream()
			.filter(transaction->postingReader.readValue(transaction, BillingCyclePosting.class).getPeriodEndDate().compareTo(billingDate)==0)
			.map(transaction->{
				queueService.queueNewTransactionPost(transaction.getAccountId(), transaction.getId(), "billingcycle");
				return transaction.getAccountId();
			})
			.collect(Collectors.toList());
		return ResponseEntity.ok(result);
*/		 
	}
	
	public void handleBillingCycle() {
		logger.info("handleBillingCycle");

		Map<String, ByteString> params = new HashMap<>();
		Map<String, ByteString> results = new HashMap<>();
		params.put("billingdate", ByteString.copyFromUtf8(businessDateService.getBusinessDate().toString()));

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
		

	}
		
}
