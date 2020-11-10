package com.github.karlnicholas.djsdist.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.karlnicholas.djsdist.distributed.Grpcservices.WorkItemMessage;
import com.github.karlnicholas.djsdist.distributed.ServiceClients;
import com.github.karlnicholas.djsdist.model.Loan;
import com.github.karlnicholas.djsdist.model.Transaction;
import com.github.karlnicholas.djsdist.model.TransactionSubmitted;
import com.github.karlnicholas.djsdist.model.TransactionType;
import com.github.karlnicholas.djsdist.repository.LoanRepository;
import com.github.karlnicholas.djsdist.repository.TransactionOpenRepository;
import com.github.karlnicholas.djsdist.repository.TransactionSubmittedRepository;
import com.github.karlnicholas.djsdist.service.BusinessDateService;
import com.google.protobuf.ByteString;

@RestController
@RequestMapping("/loan")
public class LoanRestController {
	private final LoanRepository loanRepository;
	private final TransactionSubmittedRepository transactionSubmittedRepository;
	private final TransactionOpenRepository transactionOpenRepository;
	private final ServiceClients serviceClients;
	private final BusinessDateService businessDateService;
	
	public LoanRestController(
			LoanRepository loanRepository,  
			TransactionSubmittedRepository transactionSubmittedRepository,
			TransactionOpenRepository transactionOpenRepository,
			ServiceClients serviceClients,
			BusinessDateService businessDateService
	) {
		this.businessDateService = businessDateService;
		this.serviceClients = serviceClients;
		this.transactionOpenRepository = transactionOpenRepository;
		this.transactionSubmittedRepository = transactionSubmittedRepository;
		this.loanRepository = loanRepository;
	}
	@PostMapping
	public ResponseEntity<Transaction> loanFunding(@RequestBody TransactionSubmitted transactionSubmitted) {
		transactionSubmitted = transactionSubmittedRepository.save(transactionSubmitted);
		Map<String, ByteString> params = new HashMap<>();
		Map<String, ByteString> results = new HashMap<>();
		params.put("subject", ByteString.copyFromUtf8(transactionSubmitted.getId().toString()));
		params.put("businessDate", ByteString.copyFromUtf8(businessDateService.getBusinessDate().toString()));

		WorkItemMessage wim = serviceClients.validateAndProcessTransaction(WorkItemMessage.newBuilder().putAllParams(params).putAllResults(results).build());
		results.putAll(wim.getResultsMap());
		params.putAll(wim.getParamsMap());

		if ( TransactionType.valueOf(results.get("transactionType").toStringUtf8()) != TransactionType.LOAN_FUNDING ) {
			return ResponseEntity.badRequest().body(transactionSubmitted);
		} else if ( Boolean.valueOf( results.get("validated").toStringUtf8()) ) {
			wim = serviceClients.accountFunded(wim.toBuilder().putAllParams(params).putAllResults(results).build());
			params.putAll(wim.getParamsMap());
			results.putAll(wim.getResultsMap());
			params.put("subject", ByteString.copyFromUtf8("1"));
			wim = serviceClients.initialBillingCycle(wim.toBuilder().putAllParams(params).putAllResults(results).build());
			return ResponseEntity.accepted().body(transactionOpenRepository.fetchLatestBillingCycleForAccount(transactionSubmitted.getAccountId()));
		} else {
			return ResponseEntity.badRequest().body(transactionSubmitted);
		}
	}
	@GetMapping("transactions")
	public Iterable<Loan> listTransactions() {
		return loanRepository.findAll();
	}
	@GetMapping("count")
	public Long countTransactions() {
		return loanRepository.count();
	}
}
