package com.github.karlnicholas.djsdist.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.karlnicholas.djsdist.model.Loan;
import com.github.karlnicholas.djsdist.repository.LoanRepository;

@RestController
@RequestMapping("/loan")
public class LoanRestController {
	private final LoanRepository loanRepository;
	public LoanRestController(
			LoanRepository loanRepository 
	) {
		this.loanRepository = loanRepository;
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
