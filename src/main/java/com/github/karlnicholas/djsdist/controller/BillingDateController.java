package com.github.karlnicholas.djsdist.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.karlnicholas.djsdist.repository.TransactionOpenRepository;
import com.github.karlnicholas.djsdist.service.BusinessDateService;
import com.github.karlnicholas.djsdist.service.PostingReader;

@RestController
@RequestMapping("billingcycle")
public class BillingDateController {
	private final TransactionOpenRepository transactionOpenRepository;
	private final PostingReader postingReader;
	private final BusinessDateService businessDateService;

	public BillingDateController(
		TransactionOpenRepository transactionOpenRepository,
		PostingReader postingReader, 
		BusinessDateService businessDateService
	) {
		this.transactionOpenRepository = transactionOpenRepository;
		this.postingReader = postingReader;
		this.businessDateService = businessDateService;
	}
	@GetMapping
	public ResponseEntity<String> billingDate() throws Exception {
		return ResponseEntity.ok("Test"); 
	}
}
