package com.github.karlnicholas.djsdist.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.karlnicholas.djsdist.model.Transaction;
import com.github.karlnicholas.djsdist.repository.TransactionRejectedRepository;

@RestController
@RequestMapping("/rejected")
public class RejectedRestController {
	private final TransactionRejectedRepository transactionRejectedRepository;
	public RejectedRestController(
			TransactionRejectedRepository transactionRejectedRepository 
	) {
		this.transactionRejectedRepository = transactionRejectedRepository;
	}
	@GetMapping("transactions")
	public Iterable<Transaction> listTransactions() {
		return transactionRejectedRepository.findAll();
	}
	@GetMapping("count")
	public Long countTransactions() {
		return transactionRejectedRepository.count();
	}
}
