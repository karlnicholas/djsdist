package com.github.karlnicholas.djsdist.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.karlnicholas.djsdist.model.BillingCycle;
import com.github.karlnicholas.djsdist.repository.BillingCycleRepository;

@RestController
@RequestMapping("/billing")
public class BillingCycleController {
	private final BillingCycleRepository billingCycleRepository;
	public BillingCycleController(
			BillingCycleRepository billingCycleRepository 
	) {
		this.billingCycleRepository = billingCycleRepository;
	}
	@GetMapping("transactions")
	public Iterable<BillingCycle> listTransactions() {
		return billingCycleRepository.findAll();
	}
	@GetMapping("count")
	public Long countTransactions() {
		return billingCycleRepository.count();
	}
}
