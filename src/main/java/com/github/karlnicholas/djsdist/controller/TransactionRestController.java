package com.github.karlnicholas.djsdist.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.karlnicholas.djsdist.distributed.Grpcservices.WorkItemMessage;
import com.github.karlnicholas.djsdist.distributed.ServiceClients;
import com.github.karlnicholas.djsdist.handler.TransactionHandler;
import com.github.karlnicholas.djsdist.model.Transaction;
import com.github.karlnicholas.djsdist.model.TransactionOpen;
import com.github.karlnicholas.djsdist.model.TransactionSubmitted;
import com.github.karlnicholas.djsdist.model.TransactionType;
import com.github.karlnicholas.djsdist.repository.TransactionOpenRepository;
import com.github.karlnicholas.djsdist.repository.TransactionSubmittedRepository;
import com.github.karlnicholas.djsdist.service.BusinessDateService;
import com.google.protobuf.ByteString;

@RestController
@RequestMapping("/transaction")
public class TransactionRestController {
	private static final Logger logger = LoggerFactory.getLogger(TransactionRestController.class);
	private final TransactionSubmittedRepository transactionSubmittedRepository;
	private final TransactionOpenRepository transactionOpenRepository;
	private final BusinessDateService businessDateService;
	private final TransactionHandler transactionHandler;
	private final ServiceClients serviceClients; 
	public TransactionRestController(
			TransactionSubmittedRepository transactionSubmittedRepository, 
			TransactionOpenRepository transactionOpenRepository, 
			BusinessDateService businessDateService,
			TransactionHandler transactionHandler, 
			ServiceClients serviceClients
	) {
		this.transactionSubmittedRepository = transactionSubmittedRepository;
		this.transactionOpenRepository = transactionOpenRepository;
		this.businessDateService = businessDateService;
		this.transactionHandler = transactionHandler;
		this.serviceClients = serviceClients;
	}
	@PostMapping
	public ResponseEntity<?> handlePost(@RequestBody TransactionSubmitted transactionSubmitted) {
		try {
			LocalDate businessDate = businessDateService.getBusinessDate();
			transactionSubmitted.setBusinessDate(businessDate);
			transactionSubmitted.setAsNew();
			logger.info("post saving: " + transactionSubmitted);
			transactionSubmittedRepository.save(transactionSubmitted);
			transactionHandler.asynchHandleTransaction(transactionSubmitted.getId(), businessDate);
			return ResponseEntity.accepted().build();
		} catch ( Exception e ) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}

//		queueNewTransactionPost(transactionSubmitted.getAccount().getId(), transactionSubmitted.getId(), "transaction");

	}
	@PostMapping("loanfunding")
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
/*	
	public Mono<ServerResponse> playPost(ServerRequest serverRequest) {
			QueueEntry queueEntry = QueueEntry.builder()
					.queueId(queueId.getAndIncrement())
					.action("playpost")
					.accountId("ACCOUNTID")
					.transactionId("TRANSACTIONID")
					.serverWebExchange(serverRequest.exchange())
					.httpMethod("POST")
					.build();
			subjectQueueManager.addQueueEntry("ACCOUNTID", queueEntry);
			return serverRequest.exchange().getResponse().setComplete()
					.flatMap(v->ServerResponse.accepted().build())
					.doOnError(e->logger.error("Exception: " + e.getMessage()))
					.onErrorResume(e->ServerResponse.badRequest().bodyValue(e.getMessage()));
	}
	public SubjectQueueManager getSubjectQueueManager() {
		return subjectQueueManager;
	}
	@GetMapping("/get/{action}/{subject}")
	public DeferredResult<ResponseEntity<?>> handleGet(
			@PathVariable("action") String action, 
			@PathVariable("subject") String subject, 
			HttpServletRequest request, 
			HttpServletResponse response 
	) {
		
	    DeferredResult<ResponseEntity<?>> output = new DeferredResult<>();
	    
	    return output;
	}
*/
	@GetMapping
	public Iterable<TransactionSubmitted> listTransactions() {
		return transactionSubmittedRepository.findAll();
	}
	@GetMapping("count")
	public Long countTransactions() {
		return transactionSubmittedRepository.count();
	}
}
