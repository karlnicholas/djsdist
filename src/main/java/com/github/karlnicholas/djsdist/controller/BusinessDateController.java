package com.github.karlnicholas.djsdist.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.karlnicholas.djsdist.handler.TransactionHandler;
import com.github.karlnicholas.djsdist.service.BusinessDateService;

@RestController
@RequestMapping("businessdate")
public class BusinessDateController {
	private final BusinessDateService businessDateService;
	private final TransactionHandler endOfDayHandler;
	public BusinessDateController(
		BusinessDateService businessDateService,
		TransactionHandler endOfDayHandler
	) {
		this.businessDateService = businessDateService;
		this.endOfDayHandler = endOfDayHandler;
	}
	@PostMapping("/{businessDate}")
	public ResponseEntity<Void> setBusinessDate(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate businessDate) {
		LocalDate priorBusinessDate = businessDateService.getBusinessDate();
		businessDateService.setBusinessDate(businessDate);
		endOfDayHandler.handleEndOfDay(priorBusinessDate);
		return ResponseEntity.ok().build();
	}
	
	
}
