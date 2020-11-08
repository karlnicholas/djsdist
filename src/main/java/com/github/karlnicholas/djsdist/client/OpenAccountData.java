package com.github.karlnicholas.djsdist.client;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Observable;
import java.util.Observer;

import com.github.karlnicholas.djsdist.client.NotificationParameters.ACCOUNT_ACTIONS;

public class OpenAccountData extends AccountAndBillingCycle implements Observer {
	private LocalDate openDate;
	private Integer term;
	private BigDecimal principal;
	private BigDecimal rate;

	@Override
	public void update(Observable o, Object arg) {
		NotificationParameters notificationParameters = (NotificationParameters)arg;
		if ( notificationParameters.getAction().equals(ACCOUNT_ACTIONS.OPEN_ACCOUNT) 
				&& notificationParameters.getDate().isEqual(openDate)
		) {
			notificationParameters.getAccountHandler().createAccount(openDate, term, principal, rate, this);
		}
	}

}

/*
	else if ( billingCycle != null
	&& notificationParameters.getAction().equals(ACCOUNT_ACTIONS.BILLING)
	&& notificationParameters.getDate().isEqual(billingCycle.getPeriodEndDate())
	&& !billingCycle.getClosed().booleanValue()
	) {
	//System.out.println("billingCycle: " + billingCycle);
	billingCycle = notificationParameters.getAccountHandler().latestBillingCycle(account);
	if ( debug.booleanValue() ) System.out.println("billingCycle: " + billingCycle);
	}
*/