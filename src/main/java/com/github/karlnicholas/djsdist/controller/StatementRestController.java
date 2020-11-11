package com.github.karlnicholas.djsdist.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.karlnicholas.djsdist.model.TransactionOpen;
import com.github.karlnicholas.djsdist.repository.BillingCycleRepository;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/statement")
public class StatementRestController {
	private final BillingCycleRepository billingCycleRepository;
	public StatementRestController(
			BillingCycleRepository billingCycleRepository
	) {
		this.billingCycleRepository = billingCycleRepository;
	}

	@GetMapping("/{accountId}")
	public TransactionOpen getLatestBillingCycle(@PathVariable("accountId") Long accountId) {
		return billingCycleRepository.fetchLatestBillingCycleForAccount(accountId);
	}
	@GetMapping("/{accountId}/{statementDate}")
	public TransactionOpen getBillingCycleForDate(@PathVariable("accountId") Long accountId, @PathVariable("statementDate") Long statementDate) {
		return billingCycleRepository.fetchLatestBillingCycleForAccount(accountId);
	}
}
