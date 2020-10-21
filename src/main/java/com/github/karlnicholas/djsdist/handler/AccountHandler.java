package com.github.karlnicholas.djsdist.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.github.karlnicholas.djsdist.service.AccountClosedService;
import com.github.karlnicholas.djsdist.service.AccountClosedSummary;

@Component
public class AccountHandler {
	private static final Logger logger = LoggerFactory.getLogger(AccountHandler.class);
	private final AccountClosedService accountClosedService;

	public AccountHandler(
		AccountClosedService accountClosedService
	) {
		this.accountClosedService = accountClosedService;
	}
	
	public void accountClosedSummary() {
		AccountClosedSummary accountClosedSummary = accountClosedService.getAccountClosedSummary(1L);
	}

}
