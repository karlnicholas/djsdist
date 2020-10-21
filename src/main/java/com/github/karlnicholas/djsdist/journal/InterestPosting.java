package com.github.karlnicholas.djsdist.journal;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InterestPosting implements PostingFunctions {
	public static final long serialVersionUID = 1L;
	LocalDate date;
	BigDecimal amount;
	@Override
	public boolean inBillingCycle(BillingCyclePosting billingCycle) {
		return billingCycle.periodStartDate.compareTo(date) <= 0 && billingCycle.periodEndDate.compareTo(date) >= 0;
	}
	@Override
	public boolean validate() {
		// TODO Auto-generated method stub
		return true;
	}
	@Override
	public boolean backdated(BillingCyclePosting billingCycle) {
		return false;
	}
	@Override
	public LocalDate retrieveTransactionDate() {
		return date;
	}
}
