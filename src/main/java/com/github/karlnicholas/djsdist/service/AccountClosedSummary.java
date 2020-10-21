package com.github.karlnicholas.djsdist.service;

import java.math.BigDecimal;

import com.github.karlnicholas.djsdist.model.AccountClosed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class AccountClosedSummary {
	private AccountClosed account;
	private BigDecimal loans;
	private BigDecimal payments;
	private BigDecimal latefees;
	private BigDecimal interest;
	private Integer paymentCount;
	private Integer billingCycles;
}
