package com.github.karlnicholas.djsdist.controller;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.karlnicholas.djsdist.handler.TransactionHandler;
import com.github.karlnicholas.djsdist.model.TransactionSubmitted;
import com.github.karlnicholas.djsdist.repository.TransactionSubmittedRepository;
import com.github.karlnicholas.djsdist.service.BusinessDateService;

@RestController
@RequestMapping("/transaction")
public class TransactionRestController {
	private static final Logger logger = LoggerFactory.getLogger(TransactionRestController.class);
	private final TransactionSubmittedRepository transactionSubmittedRepository;
	private final BusinessDateService businessDateService;
	private final TransactionHandler transactionHandler;
	public TransactionRestController(
			TransactionSubmittedRepository transactionSubmittedRepository, 
			BusinessDateService businessDateService,
			TransactionHandler transactionHandler 
	) {
		this.transactionSubmittedRepository = transactionSubmittedRepository;
		this.businessDateService = businessDateService;
		this.transactionHandler = transactionHandler;
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
