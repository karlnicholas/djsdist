package com.github.karlnicholas.djsdist.client;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Observable;
import java.util.Observer;

import com.github.karlnicholas.djsdist.client.NotificationParameters.ACCOUNT_ACTIONS;
import com.github.karlnicholas.djsdist.journal.BillingCyclePosting;
import com.github.karlnicholas.djsdist.model.Account;

import lombok.Data;

@Data
public class OpenAccountData implements Observer {
	private Account account;
	private BillingCyclePosting billingCycle;
	private LocalDate openDate;
	private Integer term;
	private BigDecimal principal;
	private BigDecimal rate;
	private Boolean debug = false;

	@Override
	public void update(Observable o, Object arg) {
		NotificationParameters notificationParameters = (NotificationParameters)arg;
		if ( notificationParameters.getAction().equals(ACCOUNT_ACTIONS.OPEN_ACCOUNT) 
				&& notificationParameters.getDate().isEqual(openDate)
		) {
			account = notificationParameters.getAccountHandler().makeAccount(openDate, term, principal, rate);
			billingCycle = notificationParameters.getAccountHandler().updateBillingCycle(account);
		}
		else if ( billingCycle != null
				&& notificationParameters.getAction().equals(ACCOUNT_ACTIONS.BILLING)
				&& notificationParameters.getDate().isEqual(billingCycle.getPeriodEndDate())
				&& !billingCycle.getClosed().booleanValue()
		) {
//System.out.println("billingCycle: " + billingCycle);
			billingCycle = notificationParameters.getAccountHandler().updateBillingCycle(account);
if ( debug.booleanValue() ) System.out.println("billingCycle: " + billingCycle);
		}
	}

}