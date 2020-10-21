package com.github.karlnicholas.djsdist.journal;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class LoanFundingPosting implements PostingFunctions {
	public static final long serialVersionUID = 1L;
	// funding specific 
	private BigDecimal principal;
	private LocalDate inceptionDate;
	private BigDecimal interestRate;
	private Integer termMonths;
	@Override
	public boolean inBillingCycle(BillingCyclePosting billingCycle) {
		return false;
	}
	@Override
	public boolean validate() {
		return principal != null && inceptionDate != null && interestRate != null && termMonths != null;
	}
	@Override
	public boolean backdated(BillingCyclePosting billingCycle) {
		return false;
	}
	@Override
	public LocalDate retrieveTransactionDate() {
		return inceptionDate;
	}
}
