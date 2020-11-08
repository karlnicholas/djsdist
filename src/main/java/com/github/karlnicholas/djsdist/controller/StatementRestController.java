package com.github.karlnicholas.djsdist.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.karlnicholas.djsdist.model.TransactionOpen;
import com.github.karlnicholas.djsdist.repository.TransactionOpenRepository;

@RestController
@RequestMapping("/statement")
public class StatementRestController {
	private static final Logger logger = LoggerFactory.getLogger(StatementRestController.class);
	private final TransactionOpenRepository transactionOpenRepository;
	public StatementRestController(
			TransactionOpenRepository transactionOpenRepository
	) {
		this.transactionOpenRepository = transactionOpenRepository;
	}

	@GetMapping("/{accountId}")
	public TransactionOpen getLatestBillingCycle(@PathVariable("accountId") Long accountId) {
		return transactionOpenRepository.fetchLatestBillingCycleForAccount(accountId);
	}
	@GetMapping("/{accountId}/{statementDate}")
	public TransactionOpen getBillingCycleForDate(@PathVariable("accountId") Long accountId, @PathVariable("statementDate") Long statementDate) {
		return transactionOpenRepository.fetchLatestBillingCycleForAccount(accountId);
	}
}
