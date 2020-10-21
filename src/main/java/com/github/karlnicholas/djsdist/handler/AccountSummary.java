package com.github.karlnicholas.djsdist.handler;

import java.math.BigDecimal;

import com.github.karlnicholas.djsdist.model.Account;

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
public class AccountSummary {
	private Account account;
	private BigDecimal loans;
	private BigDecimal payments;
	private BigDecimal latefees;
	private BigDecimal interest;
	private Integer paymentCount;
}
