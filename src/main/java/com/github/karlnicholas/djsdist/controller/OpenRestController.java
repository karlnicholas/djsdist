package com.github.karlnicholas.djsdist.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.karlnicholas.djsdist.model.TransactionOpen;
import com.github.karlnicholas.djsdist.repository.TransactionOpenRepository;

@RestController
@RequestMapping("/open")
public class OpenRestController {
	private final TransactionOpenRepository transactionOpenRepository;
	public OpenRestController(
			TransactionOpenRepository transactionOpenRepository 
	) {
		this.transactionOpenRepository = transactionOpenRepository;
	}
	@GetMapping("transactions")
	public Iterable<TransactionOpen> listTransactions() {
		return transactionOpenRepository.findAll();
	}
	@GetMapping("count")
	public Long countTransactions() {
		return transactionOpenRepository.count();
	}
}
