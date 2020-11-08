package com.github.karlnicholas.djsdist.client;

import com.github.karlnicholas.djsdist.journal.BillingCyclePosting;
import com.github.karlnicholas.djsdist.model.Account;

import lombok.Data;

@Data
public class AccountAndBillingCycle {
	protected Account account;
	protected BillingCyclePosting billingCycle;
}
