package com.github.karlnicholas.djsdist.journal;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingCyclePosting implements PostingFunctions {
	LocalDate periodStartDate;
	BigDecimal principal;
	BigDecimal fixedMindue;
	Boolean deliquent;
	Boolean closed;
	Integer termsRemaining;
	LocalDate mindueDate;
	LocalDate periodEndDate;
	@Override
	public boolean inBillingCycle(BillingCyclePosting billingCycle) {
		return false;
	}
	@Override
	public boolean validate() {
		return true;
	}
	@Override
	public boolean backdated(BillingCyclePosting billingCycle) {
		return false;
	}
	@Override
	public LocalDate retrieveTransactionDate() {
		return periodStartDate;
	}
}
